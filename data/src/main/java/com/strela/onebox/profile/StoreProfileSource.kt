package com.strela.onebox.profile

import android.content.Context
import com.strela.onebox.api.PingOptions
import com.strela.onebox.api.ProfileSelection
import com.strela.onebox.api.ProfileSource
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.subscription.SubscriptionStore

/**
 * Реализация [ProfileSource] на хранилищах приложения: активная подписка
 * (JSON или выбранный сервер) и настройки. Ядра читают профиль через
 * интерфейс из :api — сами хранилища им не видны.
 */
class StoreProfileSource(context: Context) : ProfileSource {

    private val app = context.applicationContext

    /**
     * Сохранённый пользователем «сырой» конфиг (JSON-режим). Ключи и файл
     * прежние (prefs «xray», ключ «config») — пользовательские данные с
     * предыдущих версий приложения сохраняются.
     */
    @Volatile
    private var userConfigJson: String? = readUserConfig(app)

    override fun activeProfile(): ProfileSelection? {
        val subscription = SubscriptionStore.selectedSubscription() ?: return null
        // JSON-подписка — готовый конфиг ядра, приоритетнее пересборки.
        if (subscription.isJson) return ProfileSelection(configJson = subscription.json)
        // Обычная подписка — выбранный сервер: ядро соберёт конфиг из него.
        subscription.selectedServer?.let { return ProfileSelection(server = it) }
        // Подписка есть, серверов нет (или все удалены) — не профиль вовсе.
        return null
    }

    override fun blockAds(): Boolean = SettingsStore.blockAds.value

    override fun pingOptions(): PingOptions = SettingsStore.pingOptions.value

    override fun userConfig(): String? = userConfigJson

    override fun saveUserConfig(json: String) {
        userConfigJson = json
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONFIG, json)
            .apply()
        AppLog.i("StoreProfileSource: сохранён пользовательский JSON-конфиг")
    }

    override fun clearUserConfig() {
        userConfigJson = null
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CONFIG)
            .apply()
        AppLog.i("StoreProfileSource: пользовательский JSON-конфиг удалён")
    }

    private fun readUserConfig(context: Context): String? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CONFIG, null)
            ?.takeIf { it.isNotBlank() }
        return raw
    }

    companion object {
        private const val PREFS = "xray"
        private const val KEY_CONFIG = "config"
    }
}
