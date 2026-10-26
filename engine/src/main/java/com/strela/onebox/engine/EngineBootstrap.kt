package com.strela.onebox.engine

import android.content.Context
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.profile.StoreProfileSource
import com.strela.onebox.vpn.TunVpnServiceBridge
import com.strela.onebox.xray.XrayEngine

/**
 * Сборка исполнительной среды: регистрирует ядра и VpnService-мост
 * в прослойке [VpnConnector] и инициализирует активное ядро.
 *
 * Вызывается из OneBoxApplication — в каждом новом процессе (Activity
 * или VpnService при always-on). Идемпотентен.
 *
 * Приложение решает, какие ядра включать: новое ядро добавляется строкой
 * в [ensure] и своим идентификатором при выборе активного.
 */
object EngineBootstrap {

    @Volatile
    private var started = false

    private val lock = Any()

    fun ensure(context: Context) {
        if (started) return
        synchronized(lock) {
            if (started) return
            started = true
            val app = context.applicationContext
            // Ядра: «будущее ядро» (sing-box и т.п.) регистрируется рядом.
            VpnConnector.register(XrayEngine)
            // Системный VpnService-мост.
            VpnConnector.registerService(TunVpnServiceBridge)
            // Активное ядро: пока единственное зарегистрированное.
            VpnConnector.init(app, StoreProfileSource(app))
        }
    }
}
