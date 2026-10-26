package com.strela.onebox

import android.app.Application
import com.strela.onebox.engine.EngineBootstrap
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.subscription.SubscriptionStore

/**
 * Точка инициализации процесса: любой новый процесс (Activity, VpnService
 * при always-on, свежий процесс после смерти) получает собранную среду
 * до старта любого компонента — здесь инициализируются лог, хранилища
 * и регистрируются ядра в прослойке.
 *
 * Раньше та же четвёрка вызовов дублировалась в MainActivity и
 * TunVpnService; с выносом VpnService в :vpn она обязана жить здесь,
 * иначе цикл: TunVpnService (:vpn) → EngineBootstrap (:engine) →
 * TunVpnServiceBridge (:vpn).
 */
class OneBoxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLog.init(this)
        SettingsStore.init(this)
        SubscriptionStore.init(this)
        EngineBootstrap.ensure(this)
    }
}
