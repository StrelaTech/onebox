package com.strela.onebox.settings

import android.content.Context
import com.strela.onebox.api.PingMethod
import com.strela.onebox.api.PingOptions
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
 * Настройки приложения (язык, тема, размер текста и параметры подключения)
 * в SharedPreferences. Значения доступны как StateFlow — Compose перерисовывается
 * при изменении. Иконка приложения одна — стандартная, вариантов больше нет.
 */
object SettingsStore {

    /** Множители размера текста: мелкий, обычный, крупный, очень крупный. */
    const val FONT_SCALE_SMALL = 0.85f
    const val FONT_SCALE_NORMAL = 1f
    const val FONT_SCALE_LARGE = 1.15f
    const val FONT_SCALE_HUGE = 1.3f
    private const val PREFS = "settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_THEME = "theme"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_BLOCK_ADS = "block_ads"
    private const val KEY_AUTO_CONNECT = "auto_connect"
    private const val KEY_PING_METHOD = "ping_method"
    private const val KEY_PING_TIMEOUT = "ping_timeout"
    private const val KEY_PING_URL = "ping_url"

    private val _language = MutableStateFlow(AppLanguage.RU)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    /** Множитель размера текста для всей типографики (1f — обычный). */
    private val _fontScale = MutableStateFlow(FONT_SCALE_NORMAL)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _blockAds = MutableStateFlow(true)
    val blockAds: StateFlow<Boolean> = _blockAds.asStateFlow()

    private val _autoConnect = MutableStateFlow(false)
    val autoConnect: StateFlow<Boolean> = _autoConnect.asStateFlow()

    private val _pingOptions = MutableStateFlow(PingOptions())
    val pingOptions: StateFlow<PingOptions> = _pingOptions.asStateFlow()

    /** Инициализация: читает настройки из prefs. Идемпотентна. */
    fun init(context: Context) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _language.value = AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null))
        _theme.value = ThemeMode.fromKey(prefs.getString(KEY_THEME, null))
        _fontScale.value = prefs.getFloat(KEY_FONT_SCALE, FONT_SCALE_NORMAL)
            .coerceIn(FONT_SCALE_SMALL, FONT_SCALE_HUGE)
        _blockAds.value = prefs.getBoolean(KEY_BLOCK_ADS, true)
        _autoConnect.value = prefs.getBoolean(KEY_AUTO_CONNECT, false)
        _pingOptions.value = PingOptions(
            method = PingMethod.fromKey(prefs.getString(KEY_PING_METHOD, null)),
            timeoutMs = prefs.getLong(KEY_PING_TIMEOUT, PingOptions.DEFAULT_TIMEOUT_MS)
                .coerceIn(100L, 30_000L),
            testUrl = prefs.getString(KEY_PING_URL, null)
                ?.takeIf { it.isNotBlank() }
                ?: PingOptions.DEFAULT_TEST_URL,
        )
        AppLog.i(
            "SettingsStore: язык=${_language.value.code}, тема=${_theme.value.key}, " +
                "масштаб текста=${_fontScale.value}, блокировка рекламы=${_blockAds.value}, " +
                "автоподключение=${_autoConnect.value}"
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

    fun setFontScale(context: Context, scale: Float) {
        val value = scale.coerceIn(FONT_SCALE_SMALL, FONT_SCALE_HUGE)
        if (_fontScale.value == value) return
        _fontScale.value = value
        prefs(context).edit().putFloat(KEY_FONT_SCALE, value).apply()
        AppLog.i("SettingsStore: выбран масштаб текста=$value")
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

    fun setPingMethod(context: Context, method: PingMethod) {
        if (_pingOptions.value.method == method) return
        _pingOptions.value = _pingOptions.value.copy(method = method)
        prefs(context).edit().putString(KEY_PING_METHOD, method.key).apply()
        AppLog.i("SettingsStore: метод пинга=${method.key}")
    }

    fun setPingTimeout(context: Context, timeoutMs: Long) {
        val value = timeoutMs.coerceIn(100L, 30_000L)
        if (_pingOptions.value.timeoutMs == value) return
        _pingOptions.value = _pingOptions.value.copy(timeoutMs = value)
        prefs(context).edit().putLong(KEY_PING_TIMEOUT, value).apply()
        AppLog.i("SettingsStore: тайм-аут пинга=$value мс")
    }

    fun setPingTestUrl(context: Context, url: String) {
        val value = url.trim().takeIf { it.isNotBlank() } ?: PingOptions.DEFAULT_TEST_URL
        if (_pingOptions.value.testUrl == value) return
        _pingOptions.value = _pingOptions.value.copy(testUrl = value)
        prefs(context).edit().putString(KEY_PING_URL, value).apply()
        AppLog.d("SettingsStore: URL-тест пинга=$value")
    }

    /**
     * Возвращает настройки к значениям по умолчанию. Стираются ВСЕ ключи
     * prefs (включая устаревшие вроде снятой с поддержки иконки-варианта) —
     * подписки и JSON-конфиг сбрасывает вызывающий код (см. SettingsTab).
     */
    fun reset(context: Context) {
        _theme.value = ThemeMode.SYSTEM
        _language.value = AppLanguage.RU
        _fontScale.value = FONT_SCALE_NORMAL
        _blockAds.value = true
        _autoConnect.value = false
        _pingOptions.value = PingOptions()
        prefs(context).edit()
            .clear()
            .putString(KEY_THEME, _theme.value.key)
            .putString(KEY_LANGUAGE, _language.value.code)
            .putFloat(KEY_FONT_SCALE, _fontScale.value)
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
