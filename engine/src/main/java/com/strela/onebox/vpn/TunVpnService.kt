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
import com.strela.onebox.engine.R
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.xray.XrayController

/**
 * VPN-сервис: поднимает tun-интерфейс и отдаёт его fd ядру xray (tun-инбаунд).
 *
 * Петля маршрутизации исключена через addDisallowedApplication(наш пакет):
 * трафик самого приложения (исходящие dial'ы ядра) идёт мимо туннеля.
 */
class TunVpnService : VpnService() {

    private var tunPfd: ParcelFileDescriptor? = null
    private var tunFd = -1
    private var fdDetached = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // сервис может стартовать в свежем процессе без Activity — инициализируем всё сами
        AppLog.init(applicationContext)
        SettingsStore.init(applicationContext)
        SubscriptionStore.init(applicationContext)
        XrayController.init(applicationContext)
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
     *  3) если ядро зависло — рвём tun-fd принудительно, чтобы интерфейс умер,
     *     а не «когда ядро освободится»;
     *  4) в любом случае закрываем ресурсы и останавливаем сервис.
     */
    private fun shutdown() {
        AppLog.i("shutdown: жёсткое отключение туннеля")
        stopForeground(STOP_FOREGROUND_REMOVE)
        Thread {
            val stopped = XrayController.stopBlocking()
            if (!stopped) forceCloseTun()
            stopVpn()
        }.start()
    }

    /**
     * Физически рвёт tun, когда ядро зависло: fd после detachFd() принадлежит
     * нам (ядро лишь читает его), поэтому закрытие здесь убивает интерфейс,
     * не дожидаясь остановки ядра.
     */
    private fun forceCloseTun() {
        if (tunFd < 0) return
        AppLog.w("forceCloseTun: ядро зависло — закрываю tun-fd $tunFd напрямую")
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

        // Если ядро уже поднято — гасим всё и выходим (без блокировки главного потока)
        if (XrayController.isRunning) {
            AppLog.w("connect: ядро уже запущено, останавливаю")
            shutdown()
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
            XrayController.onVpnError(e.message ?: "establish() failed")
            stopVpn()
            return
        }

        if (pfd == null) {
            // null = пользователь отменил или есть другой активный VPN
            AppLog.w("establish() вернул null (отмена/чужой VPN)")
            XrayController.onVpnRevoked()
            stopVpn()
            return
        }

        val fd = pfd.detachFd()
        tunFd = fd
        fdDetached = true
        AppLog.i("connect: tun поднят, fd=$fd")
        tunPfd = pfd
        XrayController.startTun(fd) { message ->
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

    private fun stopVpn() {
        AppLog.i("stopVpn: закрываю tun и гашу сервис")
        // tunPfd после detachFd() дескриптором уже не владеет — владение передано ядру,
        // которое закрывает fd при StopInstance. Для страховки (если startTun упал)
        // закрываем только валидный, ещё не отданный дескриптор.
        if (fdDetached) {
            AppLog.d("stopVpn: tun-fd принадлежит ядру, закрывать не нужно")
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
        XrayController.onVpnRevoked()
        // туннель система уже сняла — гасим ядро и сервис по тому же
        // жёсткому пути: с таймаутом и принудительным разрывом tun-fd
        shutdown()
    }

    override fun onDestroy() {
        AppLog.i("onDestroy: гашу ядро (короткий таймаут — главный поток не блокируем)")
        // последний рубеж: даже если shutdown() не успел, остановка уходит в
        // stopExecutor и доделается в фоне; здесь ждём недолго, иначе зависшее
        // ядро заблокирует главный поток (ANR) и не даст сервису умереть
        XrayController.stopBlocking(DESTROY_STOP_TIMEOUT_MS)
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
