package com.strela.onebox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.strela.onebox.log.AppLog

/**
 * Автозапуск после перезагрузки: BOOT_COMPLETED (и QUICKBOOT_POWERON у части
 * прошивок). Процесс уже проинициализирован OneBoxApplication, поэтому здесь
 * только решение «поднимать VPN / Kill Switch или нет».
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, ACTION_QUICKBOOT_POWERON -> {
                AppLog.i("BootReceiver: получен ${intent.action}")
                StartupConnections.onBoot(context)
                StartupRefresh.onBoot(context)
            }
        }
    }

    private companion object {
        const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }
}
