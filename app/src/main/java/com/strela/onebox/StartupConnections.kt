package com.strela.onebox

import android.content.Context
import android.net.VpnService
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.subscription.SubscriptionStore

/**
 * Подключение на старте приложения и после загрузки устройства.
 *
 * Android не даёт поднять VPN без выданного разрешения: если
 * [VpnService.prepare] возвращает Intent, мы не показываем диалог из фона
 * (после boot это невозможно) и просто пишем в лог. При обычном запуске
 * приложения разрешение запрашивает MainActivity.
 */
object StartupConnections {

    /** Запуск при открытии приложения. */
    fun onLaunch(context: Context) {
        connect(context, autoConnect = SettingsStore.autoConnect.value, source = "launch")
    }

    /** Запуск после BOOT_COMPLETED. */
    fun onBoot(context: Context) {
        connect(context, autoConnect = SettingsStore.autoConnectOnBoot.value, source = "boot")
    }

    private fun connect(context: Context, autoConnect: Boolean, source: String) {
        if (VpnConnector.isRunning) {
            AppLog.d("StartupConnections($source): VPN уже запущен")
            return
        }
        val killSwitch = SettingsStore.killSwitch.value
        if (!autoConnect && !killSwitch) return

        if (VpnService.prepare(context) != null) {
            AppLog.i("StartupConnections($source): нет разрешения VPN, автозапуск пропущен")
            return
        }

        if (autoConnect && hasProfile()) {
            AppLog.i("StartupConnections($source): автоподключение VPN")
            VpnConnector.start(context)
            return
        }
        if (killSwitch) {
            AppLog.i("StartupConnections($source): включаю Kill Switch")
            VpnConnector.startKillSwitch(context)
        }
    }

    /** Есть ли выбранный сервер, JSON-подписка или пользовательский конфиг. */
    private fun hasProfile(): Boolean {
        val subscription = SubscriptionStore.selectedSubscription()
        val hasSubscriptionProfile = subscription != null &&
            (subscription.isJson || subscription.selectedServer != null)
        return hasSubscriptionProfile || VpnConnector.hasUserConfig
    }
}
