package com.strela.onebox.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import com.strela.onebox.api.ConnectionMode
import com.strela.onebox.api.PingState
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.api.VpnStatus
import com.strela.onebox.vpn.R
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * VPN-сервис: поднимает tun-интерфейс и отдаёт его fd активному ядру через
 * прослойку [VpnConnector] (tun-инбаунд). Сервис нейтрален к ядру:
 * xray или будущие ядра для него неразличимы.
 *
 * Петля маршрутизации исключена через addDisallowedApplication(наш пакет):
 * трафик самого приложения (исходящие dial'ы ядра) идёт мимо туннеля.
 */
class TunVpnService : VpnService() {

    private var tunPfd: ParcelFileDescriptor? = null
    private var tunFd = -1
    private var fdDetached = false

    /** Живёт, пока поднят foreground: обновляет уведомление по статусу/пингу/серверу. */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var notificationJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Инициализация процесса (лог, хранилища, ядра) гарантирована
        // OneBoxApplication — она выполняется до любого компонента.
        AppLog.i("onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> shutdown()
            else -> {}
        }
        return START_NOT_STICKY
    }

    /**
     * Жёсткое выключение по кнопке «Отключить»:
     *  1) сразу снимаем foreground — уведомление «VPN активен» гаснет мгновенно;
     *  2) в фоне гасим ядро с таймаутом — зависший Close() не держит туннель;
     *  3) рвём tun-fd принудительно всегда: ядро fd само не закрывает никогда
     *     (AndroidTun.Close() в xray-core — заглушка, а tun-хендлер не вызывается
     *     при Instance.Close()) — если fd не закрыть здесь, туннель остаётся
     *     поднятым до смерти процесса;
     *  4) в любом случае закрываем ресурсы и останавливаем сервис.
     */
    private fun shutdown() {
        AppLog.i("shutdown: жёсткое отключение туннеля")
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationJob?.cancel()
        Thread {
            val stopped = VpnConnector.stopBlocking()
            if (!stopped) AppLog.w("shutdown: ядро не остановилось за таймаут")
            forceCloseTun()
            stopVpn()
        }.start()
    }

    /**
     * Рвёт tun, закрывая fd напрямую: fd после detachFd() формально принадлежит
     * ядру, но оно его не закрывает (AndroidTun.Close() в xray-core — заглушка),
     * поэтому именно сервис закрывает fd при любом выключении — не дожидаясь,
     * «когда ядро освободится».
     */
    private fun forceCloseTun() {
        if (tunFd < 0) return
        AppLog.w("forceCloseTun: закрываю tun-fd $tunFd напрямую")
        try {
            ParcelFileDescriptor.adoptFd(tunFd).close()
        } catch (e: Exception) {
            AppLog.w("forceCloseTun: ошибка закрытия fd", e)
        }
        tunFd = -1
        fdDetached = false
        tunPfd = null
    }

    private fun connect() {
        AppLog.i("connect: поднимаю foreground-уведомление")
        startInForeground()
        startNotificationUpdates()

        // Если ядро уже поднято — гасим всё и выходим (без блокировки главного потока)
        if (VpnConnector.isRunning) {
            AppLog.w("connect: ядро уже запущено, останавливаю")
            shutdown()
            return
        }

        val mode = VpnConnector.connectionMode() ?: ConnectionMode.TUN

        // Режим Proxy — туннель не поднимается вовсе: ядро держит только
        // локальный socks-порт, пользователь направляет на него трафик сам.
        if (mode == ConnectionMode.PROXY) {
            AppLog.i("connect: режим Proxy — без establish()")
            VpnConnector.startProxy { message ->
                AppLog.e("connect: ядро не стартовало (Proxy): $message")
                stopVpn()
            }
            return
        }

        AppLog.i("connect: вызываю establish()")
        val pfd = try {
            Builder()
                .setSession("OneBox")
                .addAddress("172.19.0.1", 32)
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("1.1.1.1")
                .setMtu(1500)
                .apply {
                    // исключаем себя из туннеля — иначе dial'ы ядра зациклятся
                    try {
                        addDisallowedApplication(packageName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        AppLog.w("addDisallowedApplication: пакет не найден", e)
                    }
                }
                .establish()
        } catch (e: Exception) {
            AppLog.e("establish() бросил исключение", e)
            VpnConnector.onVpnError(e.message ?: "establish() failed")
            stopVpn()
            return
        }

        if (pfd == null) {
            // null = пользователь отменил или есть другой активный VPN
            AppLog.w("establish() вернул null (отмена/чужой VPN)")
            VpnConnector.onVpnRevoked()
            stopVpn()
            return
        }

        val fd = pfd.detachFd()
        tunFd = fd
        fdDetached = true
        AppLog.i("connect: tun поднят, fd=$fd")
        tunPfd = pfd
        VpnConnector.startTun(fd) { message ->
            AppLog.e("connect: ядро не стартовало: $message")
            closeDetachedTun()
            stopVpn()
        }
    }

    /** Закрывает tun-fd, если ядро не успело забрать его себе (ошибка старта). */
    private fun closeDetachedTun() {
        if (tunFd < 0) return
        try {
            ParcelFileDescriptor.adoptFd(tunFd).close()
        } catch (e: Exception) {
            AppLog.w("closeDetachedTun: ошибка закрытия fd", e)
        }
        tunFd = -1
        fdDetached = false
    }

    private fun startInForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        val strings = SettingsStore.strings()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    strings.vpnChannelName,
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
            .setSmallIcon(R.drawable.ic_stat_vpn)
            .setContentTitle("OneBox")
            .setContentText(strings.vpnActiveNotification)
        if (contentIntent != null) builder.setContentIntent(contentIntent)
        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Обновляемое уведомление (п.13 ТЗ): имя сервера, пинг и режим подключения.
     * Статус ядра, пинги и активный профиль приходят из прослойки; каждое
     * изменение перерисовывает уведомление одного и того же id.
     */
    private fun startNotificationUpdates() {
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {
            val statusFlow = VpnConnector.status
            val pingsFlow = VpnConnector.pings
            combine(statusFlow, pingsFlow) { status, pings -> status to pings }
                .collectLatest { (status, pings) ->
                    if (status is VpnStatus.Stopped) return@collectLatest
                    val strings = SettingsStore.strings()
                    val profile = VpnConnector.activeProfile()
                    val serverName = profile?.server?.name
                        ?: profile?.let { strings.configMode }
                        ?: "OneBox"

                    val title = when (status) {
                        is VpnStatus.Starting -> strings.notificationConnectingTitle
                        is VpnStatus.Running -> strings.notificationRunning
                        is VpnStatus.Error -> "OneBox · ${strings.statusErrorShort}"
                        VpnStatus.Stopped -> return@collectLatest
                    }

                    val pingText = when (val state = pings[profile?.server?.id]) {
                        is PingState.Ok -> strings.notificationPing(strings.pingMs(state.millis))
                        else -> strings.notificationPingNa
                    }
                    val mode = when (VpnConnector.connectionMode()) {
                        ConnectionMode.TUN -> strings.notificationModeTun
                        ConnectionMode.PROXY -> strings.notificationModeProxy
                        ConnectionMode.TUN_PLUS_PROXY -> strings.notificationModeTunProxy
                        null -> strings.notificationModeTun
                    }

                    updateNotification(
                        id = NOTIFICATION_ID,
                        title = title,
                        text = "${strings.notificationServer(serverName)} · $pingText · $mode",
                    )
                }
        }
    }

    private suspend fun updateNotification(id: Int, title: String, text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
            .setSmallIcon(R.drawable.ic_stat_vpn)
            .setContentTitle(title)
            .setContentText(text)
        if (contentIntent != null) builder.setContentIntent(contentIntent)
        runCatching { manager.notify(id, builder.build()) }
    }

    private fun stopVpn() {
        AppLog.i("stopVpn: закрываю tun и гашу сервис")
        notificationJob?.cancel()
        // Собственно fd всегда закрывается раньше через forceCloseTun()/
        // closeDetachedTun() — здесь остаётся страховка: закрыть валидный,
        // ещё не отданный ядру дескриптор (startTun упал до detach и т.п.).
        if (fdDetached) {
            AppLog.d("stopVpn: tun-fd уже порван напрямую")
        } else {
            try {
                tunPfd?.close()
            } catch (e: Exception) {
                AppLog.w("stopVpn: ошибка закрытия tun", e)
            }
        }
        tunPfd = null
        tunFd = -1
        fdDetached = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() {
        AppLog.w("onRevoke: система отозвала VPN")
        VpnConnector.onVpnRevoked()
        // туннель система уже сняла — гасим ядро и сервис по тому же
        // жёсткому пути: с таймаутом и принудительным разрывом tun-fd
        shutdown()
    }

    override fun onDestroy() {
        AppLog.i("onDestroy: гашу ядро (короткий таймаут — главный поток не блокируем)")
        notificationJob?.cancel()
        serviceScope.cancel()
        // последний рубеж: даже если shutdown() не успел, остановка уходит в
        // stopExecutor и доделается в фоне; здесь ждём недолго, иначе зависшее
        // ядро заблокирует главный поток (ANR) и не даст сервису умереть
        VpnConnector.stopBlocking(DESTROY_STOP_TIMEOUT_MS)
        // ядро tun-fd само не закрывает — если shutdown() не успел, делаем это здесь
        forceCloseTun()
        if (!fdDetached) {
            try {
                tunPfd?.close()
            } catch (_: Exception) {
            }
        }
        tunPfd = null
        tunFd = -1
        fdDetached = false
        super.onDestroy()
    }

    companion object {
        const val ACTION_CONNECT = "com.strela.onebox.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.strela.onebox.vpn.DISCONNECT"
        private const val CHANNEL_ID = "onebox_vpn"
        private const val NOTIFICATION_ID = 1

        /** Лимит ожидания остановки ядра в onDestroy, мс (главный поток). */
        private const val DESTROY_STOP_TIMEOUT_MS = 500L
    }
}
