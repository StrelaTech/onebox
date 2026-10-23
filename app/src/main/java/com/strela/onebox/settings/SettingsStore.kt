package com.strela.onebox.settings

import android.content.Context
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.OneBoxStrings
import com.strela.onebox.i18n.stringsFor
import com.strela.onebox.log.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Тема оформления: тёмная, светлая или как в системе. */
enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

/**
 * Настройки приложения (язык, тема и параметры подключения) в SharedPreferences.
 * Значения доступны как StateFlow — Compose перерисовывается при изменении.
 */
object SettingsStore {

    private const val PREFS = "settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_THEME = "theme"
    private const val KEY_BLOCK_ADS = "block_ads"
    private const val KEY_AUTO_CONNECT = "auto_connect"

    private val _language = MutableStateFlow(AppLanguage.RU)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _blockAds = MutableStateFlow(true)
    val blockAds: StateFlow<Boolean> = _blockAds.asStateFlow()

    private val _autoConnect = MutableStateFlow(false)
    val autoConnect: StateFlow<Boolean> = _autoConnect.asStateFlow()

    /** Инициализация: читает настройки из prefs. Идемпотентна. */
    fun init(context: Context) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _language.value = AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null))
        _theme.value = ThemeMode.fromKey(prefs.getString(KEY_THEME, null))
        _blockAds.value = prefs.getBoolean(KEY_BLOCK_ADS, true)
        _autoConnect.value = prefs.getBoolean(KEY_AUTO_CONNECT, false)
        AppLog.i(
            "SettingsStore: язык=${_language.value.code}, тема=${_theme.value.key}, " +
                "блокировка рекламы=${_blockAds.value}, автоподключение=${_autoConnect.value}"
        )
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        if (_language.value == language) return
        _language.value = language
        prefs(context).edit().putString(KEY_LANGUAGE, language.code).apply()
        AppLog.i("SettingsStore: выбран язык ${language.code}")
    }

    fun setTheme(context: Context, theme: ThemeMode) {
        if (_theme.value == theme) return
        _theme.value = theme
        prefs(context).edit().putString(KEY_THEME, theme.key).apply()
        AppLog.i("SettingsStore: выбрана тема ${theme.key}")
    }

    fun setBlockAds(context: Context, enabled: Boolean) {
        if (_blockAds.value == enabled) return
        _blockAds.value = enabled
        prefs(context).edit().putBoolean(KEY_BLOCK_ADS, enabled).apply()
        AppLog.i("SettingsStore: блокировка рекламы ${if (enabled) "включена" else "выключена"}")
    }

    fun setAutoConnect(context: Context, enabled: Boolean) {
        if (_autoConnect.value == enabled) return
        _autoConnect.value = enabled
        prefs(context).edit().putBoolean(KEY_AUTO_CONNECT, enabled).apply()
        AppLog.i("SettingsStore: автоподключение ${if (enabled) "включено" else "выключено"}")
    }

    /** Возвращает все настройки к значениям по умолчанию. */
    fun reset(context: Context) {
        _theme.value = ThemeMode.SYSTEM
        _language.value = AppLanguage.RU
        _blockAds.value = true
        _autoConnect.value = false
        prefs(context).edit()
            .putString(KEY_THEME, _theme.value.key)
            .putString(KEY_LANGUAGE, _language.value.code)
            .putBoolean(KEY_BLOCK_ADS, _blockAds.value)
            .putBoolean(KEY_AUTO_CONNECT, _autoConnect.value)
            .apply()
        AppLog.i("SettingsStore: настройки сброшены к значениям по умолчанию")
    }

    /** Тексты для текущего языка — для мест вне Compose (сервис, хранилище профилей). */
    fun strings(): OneBoxStrings = stringsFor(_language.value)

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
