package com.strela.onebox.vpn

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.api.VpnServiceBridge
import com.strela.onebox.log.AppLog
import com.strela.onebox.vpn.TunVpnService

/**
 * Реализация моста [VpnServiceBridge] прослойки: поднимает и гасит
 * [TunVpnService]. Ядра и UI собственно сервис не знают — только
 * прослойку ([VpnConnector.start]/[VpnConnector.stop]).
 */
object TunVpnServiceBridge : VpnServiceBridge {

    override fun start(context: Context) {
        AppLog.i("TunVpnServiceBridge: запуск VpnService")
        val intent = Intent(context, TunVpnService::class.java)
            .setAction(TunVpnService.ACTION_CONNECT)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop(context: Context) {
        AppLog.i("TunVpnServiceBridge: останавливаю VpnService")
        val intent = Intent(context, TunVpnService::class.java)
            .setAction(TunVpnService.ACTION_DISCONNECT)
        try {
            context.startService(intent)
        } catch (e: Exception) {
            // Интент не доставлен (сервис убит / старт из фона запрещён) —
            // глушим ядро напрямую, чтобы туннель гарантированно упал.
            AppLog.w("TunVpnServiceBridge: интент не доставлен, останавливаю ядро напрямую", e)
            Thread { VpnConnector.stopBlocking() }.start()
        }
    }
}
