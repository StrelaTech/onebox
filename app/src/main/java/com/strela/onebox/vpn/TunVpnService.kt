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
import com.strela.onebox.log.AppLog
import com.strela.onebox.MainActivity
import com.strela.onebox.R
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
    private var fdDetached = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // сервис может стартовать в свежем процессе без Activity — открываем файл сами
        AppLog.init(applicationContext)
        SettingsStore.init(applicationContext)
        AppLog.i("onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> shutdown()
            else -> {}
        }
        return START_NOT_STICKY
    }

    /**
     * Полное выключение: сначала синхронно гасим ядро (оно отпускает tun-fd),
     * затем снимаем foreground, закрываем дескрипторы и останавливаем сервис.
     * В фоновом потоке — чтобы не блокировать главный.
     */
    private fun shutdown() {
        AppLog.i("shutdown: останавливаю ядро и сервис")
        Thread {
            XrayController.stopBlocking()
            stopVpn()
        }.start()
    }

    private fun connect() {
        AppLog.i("connect: поднимаю foreground-уведомление")
        startInForeground()

        // Если ядро уже поднято — гасим всё и выходим
        if (XrayController.isRunning) {
            AppLog.w("connect: ядро уже запущено, останавливаю")
            XrayController.stopBlocking()
            stopVpn()
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
        fdDetached = true
        AppLog.i("connect: tun поднят, fd=$fd")
        tunPfd = pfd
        XrayController.startTun(fd)
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
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("OneBox")
            .setContentText(strings.vpnActiveNotification)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

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
        fdDetached = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() {
        AppLog.w("onRevoke: система отозвала VPN")
        XrayController.onVpnRevoked()
        // ядро отпускает tun-fd синхронно, затем гасим сервис
        Thread {
            XrayController.stopBlocking()
            stopVpn()
        }.start()
    }

    override fun onDestroy() {
        AppLog.i("onDestroy: гарантированно гашу ядро")
        // сервис умирает — ядро запускать без tun нельзя, останавливаем принудительно
        if (XrayController.isRunning) {
            XrayController.stopBlocking()
        }
        if (!fdDetached) {
            try {
                tunPfd?.close()
            } catch (_: Exception) {
            }
        }
        tunPfd = null
        fdDetached = false
        super.onDestroy()
    }

    companion object {
        const val ACTION_CONNECT = "com.strela.onebox.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.strela.onebox.vpn.DISCONNECT"
        private const val CHANNEL_ID = "onebox_vpn"
        private const val NOTIFICATION_ID = 1
    }
}
