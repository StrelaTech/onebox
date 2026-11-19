package com.strela.onebox.settings

import android.content.Context
import com.strela.onebox.api.ConnectionMode
import com.strela.onebox.api.FragmentOptions
import com.strela.onebox.api.IpType
import com.strela.onebox.api.MuxOptions
import com.strela.onebox.api.NetworkOptions
import com.strela.onebox.api.PingMethod
import com.strela.onebox.api.PingOptions
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.OneBoxStrings
import com.strela.onebox.i18n.stringsFor
import com.strela.onebox.log.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Тема оформления: системная, светлая, тёмная, зелёная и AMOLED-чёрная. */
enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light"),
    GREEN("green"),
    AMOLED("amoled");

    companion object {
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

/**
 * Режим подключения трафика (TUN / Proxy / TUN + Proxy) — в :api, см.
 * com.strela.onebox.api.ConnectionMode; хранится в этих prefs.
 */

/**
 * Порядок серверов в списках: как в подписке, по возрастанию пинга,
 * по имени.
 */
enum class ServerSort(val key: String) {
    SUBSCRIPTION("subscription"),
    PING("ping"),
    NAME("name");

    companion object {
        fun fromKey(key: String?): ServerSort = entries.firstOrNull { it.key == key } ?: SUBSCRIPTION
    }
}

/** Периодичность обновления подписок; [millis] = 0 — никогда. */
enum class UpdateInterval(val key: String, val millis: Long) {
    NEVER("never", 0L),
    MIN30("30m", 30L * 60 * 1000),
    MIN60("60m", 60L * 60 * 1000),
    H2("2h", 2L * 60 * 60 * 1000),
    H6("6h", 6L * 60 * 60 * 1000),
    H12("12h", 12L * 60 * 60 * 1000),
    H24("24h", 24L * 60 * 60 * 1000);

    companion object {
        fun fromKey(key: String?): UpdateInterval = entries.firstOrNull { it.key == key } ?: NEVER
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
    private const val KEY_AUTO_CONNECT_ON_BOOT = "auto_connect_on_boot"
    private const val KEY_KILL_SWITCH = "kill_switch"
    private const val KEY_CONNECTION_MODE = "connection_mode"
    private const val KEY_PING_METHOD = "ping_method"
    private const val KEY_PING_TIMEOUT = "ping_timeout"
    private const val KEY_PING_URL = "ping_url"
    private const val KEY_MTU = "mtu"
    private const val KEY_IP_TYPE = "ip_type"
    private const val KEY_FRAGMENT_ENABLED = "fragment_enabled"
    private const val KEY_FRAGMENT_PACKETS = "fragment_packets"
    private const val KEY_FRAGMENT_LENGTH = "fragment_length"
    private const val KEY_FRAGMENT_INTERVAL = "fragment_interval"
    private const val KEY_MUX_ENABLED = "mux_enabled"
    private const val KEY_MUX_CONCURRENCY = "mux_concurrency"
    private const val KEY_MUX_XUDP_CONCURRENCY = "mux_xudp_concurrency"
    private const val KEY_SUBSCRIPTION_USER_AGENT = "subscription_user_agent"
    private const val KEY_SERVER_SORT = "server_sort"
    private const val KEY_UPDATE_INTERVAL = "update_interval"

    private val _language = MutableStateFlow(AppLanguage.AUTO)
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

    private val _autoConnectOnBoot = MutableStateFlow(false)
    val autoConnectOnBoot: StateFlow<Boolean> = _autoConnectOnBoot.asStateFlow()

    private val _killSwitch = MutableStateFlow(false)
    val killSwitch: StateFlow<Boolean> = _killSwitch.asStateFlow()

    private val _connectionMode = MutableStateFlow(ConnectionMode.TUN)
    val connectionMode: StateFlow<ConnectionMode> = _connectionMode.asStateFlow()

    private val _pingOptions = MutableStateFlow(PingOptions())
    val pingOptions: StateFlow<PingOptions> = _pingOptions.asStateFlow()

    private val _networkOptions = MutableStateFlow(NetworkOptions())
    val networkOptions: StateFlow<NetworkOptions> = _networkOptions.asStateFlow()

    /** User-Agent запросов подписок; пусто до [init] — тогда берётся [defaultUserAgent]. */
    private val _subscriptionUserAgent = MutableStateFlow("")
    val subscriptionUserAgent: StateFlow<String> = _subscriptionUserAgent.asStateFlow()

    private val _serverSort = MutableStateFlow(ServerSort.SUBSCRIPTION)
    val serverSort: StateFlow<ServerSort> = _serverSort.asStateFlow()

    private val _updateInterval = MutableStateFlow(UpdateInterval.NEVER)
    val updateInterval: StateFlow<UpdateInterval> = _updateInterval.asStateFlow()

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
        _autoConnectOnBoot.value = prefs.getBoolean(KEY_AUTO_CONNECT_ON_BOOT, false)
        _killSwitch.value = prefs.getBoolean(KEY_KILL_SWITCH, false)
        _connectionMode.value = ConnectionMode.fromKey(prefs.getString(KEY_CONNECTION_MODE, null))
        _pingOptions.value = PingOptions(
            method = PingMethod.fromKey(prefs.getString(KEY_PING_METHOD, null)),
            timeoutMs = prefs.getLong(KEY_PING_TIMEOUT, PingOptions.DEFAULT_TIMEOUT_MS)
                .coerceIn(100L, 30_000L),
            testUrl = prefs.getString(KEY_PING_URL, null)
                ?.takeIf { it.isNotBlank() }
                ?: PingOptions.DEFAULT_TEST_URL,
        )
        _networkOptions.value = NetworkOptions(
            mtu = prefs.getInt(KEY_MTU, NetworkOptions.DEFAULT_MTU)
                .coerceIn(NetworkOptions.MIN_MTU, NetworkOptions.MAX_MTU),
            ipType = IpType.fromKey(prefs.getString(KEY_IP_TYPE, null)),
            fragment = FragmentOptions(
                enabled = prefs.getBoolean(KEY_FRAGMENT_ENABLED, false),
                packets = prefs.getString(KEY_FRAGMENT_PACKETS, null)
                    ?.takeIf { it.isNotBlank() } ?: FragmentOptions().packets,
                length = prefs.getString(KEY_FRAGMENT_LENGTH, null)
                    ?.takeIf { it.isNotBlank() } ?: FragmentOptions().length,
                interval = prefs.getString(KEY_FRAGMENT_INTERVAL, null)
                    ?.takeIf { it.isNotBlank() } ?: FragmentOptions().interval,
            ),
            mux = MuxOptions(
                enabled = prefs.getBoolean(KEY_MUX_ENABLED, false),
                concurrency = prefs.getInt(KEY_MUX_CONCURRENCY, 8)
                    .coerceIn(1, 1024),
                xudpConcurrency = prefs.getInt(KEY_MUX_XUDP_CONCURRENCY, 16)
                    .coerceIn(1, 1024),
            ),
        )
        _subscriptionUserAgent.value = prefs.getString(KEY_SUBSCRIPTION_USER_AGENT, null)
            ?.takeIf { it.isNotBlank() }
            ?: defaultUserAgent(context)
        _serverSort.value = ServerSort.fromKey(prefs.getString(KEY_SERVER_SORT, null))
        _updateInterval.value = UpdateInterval.fromKey(prefs.getString(KEY_UPDATE_INTERVAL, null))
        AppLog.i(
            "SettingsStore: язык=${_language.value.code}, тема=${_theme.value.key}, " +
                "масштаб текста=${_fontScale.value}, блокировка рекламы=${_blockAds.value}, " +
                "автоподключение=${_autoConnect.value}, автозапуск при загрузке=${_autoConnectOnBoot.value}, " +
                "Kill Switch=${_killSwitch.value}"
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

    fun setAutoConnectOnBoot(context: Context, enabled: Boolean) {
        if (_autoConnectOnBoot.value == enabled) return
        _autoConnectOnBoot.value = enabled
        prefs(context).edit().putBoolean(KEY_AUTO_CONNECT_ON_BOOT, enabled).apply()
        AppLog.i("SettingsStore: автозапуск при загрузке ${if (enabled) "включён" else "выключен"}")
    }

    fun setKillSwitch(context: Context, enabled: Boolean) {
        if (_killSwitch.value == enabled) return
        _killSwitch.value = enabled
        prefs(context).edit().putBoolean(KEY_KILL_SWITCH, enabled).apply()
        AppLog.i("SettingsStore: Kill Switch ${if (enabled) "включён" else "выключен"}")
    }

    fun setConnectionMode(context: Context, mode: ConnectionMode) {
        if (_connectionMode.value == mode) return
        _connectionMode.value = mode
        prefs(context).edit().putString(KEY_CONNECTION_MODE, mode.key).apply()
        AppLog.i("SettingsStore: режим подключения=${mode.key}")
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

    fun setSubscriptionUserAgent(context: Context, userAgent: String) {
        val value = userAgent.trim().ifBlank { defaultUserAgent(context) }
        if (_subscriptionUserAgent.value == value) return
        _subscriptionUserAgent.value = value
        prefs(context).edit().putString(KEY_SUBSCRIPTION_USER_AGENT, value).apply()
        AppLog.i("SettingsStore: User-Agent подписок=$value")
    }

    fun setServerSort(context: Context, sort: ServerSort) {
        if (_serverSort.value == sort) return
        _serverSort.value = sort
        prefs(context).edit().putString(KEY_SERVER_SORT, sort.key).apply()
        AppLog.i("SettingsStore: сортировка серверов=${sort.key}")
    }

    fun setUpdateInterval(context: Context, interval: UpdateInterval) {
        if (_updateInterval.value == interval) return
        _updateInterval.value = interval
        prefs(context).edit().putString(KEY_UPDATE_INTERVAL, interval.key).apply()
        AppLog.i("SettingsStore: интервал обновления подписок=${interval.key}")
    }

    /** User-Agent по умолчанию: `OneBox/<versionName>`. */    fun defaultUserAgent(context: Context): String {
        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty().ifBlank { "1.0" }
        return "OneBox/$version"
    }

    fun setMtu(context: Context, mtu: Int) {        val value = mtu.coerceIn(NetworkOptions.MIN_MTU, NetworkOptions.MAX_MTU)
        if (_networkOptions.value.mtu == value) return
        _networkOptions.value = _networkOptions.value.copy(mtu = value)
        prefs(context).edit().putInt(KEY_MTU, value).apply()
        AppLog.i("SettingsStore: MTU=$value")
    }

    fun setIpType(context: Context, ipType: IpType) {
        if (_networkOptions.value.ipType == ipType) return
        _networkOptions.value = _networkOptions.value.copy(ipType = ipType)
        prefs(context).edit().putString(KEY_IP_TYPE, ipType.key).apply()
        AppLog.i("SettingsStore: тип IP=${ipType.key}")
    }

    fun setFragmentEnabled(context: Context, enabled: Boolean) {
        if (_networkOptions.value.fragment.enabled == enabled) return
        _networkOptions.value = _networkOptions.value.copy(
            fragment = _networkOptions.value.fragment.copy(enabled = enabled),
        )
        prefs(context).edit().putBoolean(KEY_FRAGMENT_ENABLED, enabled).apply()
        AppLog.i("SettingsStore: фрагментация ${if (enabled) "включена" else "выключена"}")
    }

    fun setFragmentPackets(context: Context, packets: String) {
        val value = packets.trim().takeIf { it.isNotBlank() } ?: FragmentOptions().packets
        if (_networkOptions.value.fragment.packets == value) return
        _networkOptions.value = _networkOptions.value.copy(
            fragment = _networkOptions.value.fragment.copy(packets = value),
        )
        prefs(context).edit().putString(KEY_FRAGMENT_PACKETS, value).apply()
        AppLog.d("SettingsStore: fragment packets=$value")
    }

    fun setFragmentLength(context: Context, length: String) {
        val value = length.trim().takeIf { it.isNotBlank() } ?: FragmentOptions().length
        if (_networkOptions.value.fragment.length == value) return
        _networkOptions.value = _networkOptions.value.copy(
            fragment = _networkOptions.value.fragment.copy(length = value),
        )
        prefs(context).edit().putString(KEY_FRAGMENT_LENGTH, value).apply()
        AppLog.d("SettingsStore: fragment length=$value")
    }

    fun setFragmentInterval(context: Context, interval: String) {
        val value = interval.trim().takeIf { it.isNotBlank() } ?: FragmentOptions().interval
        if (_networkOptions.value.fragment.interval == value) return
        _networkOptions.value = _networkOptions.value.copy(
            fragment = _networkOptions.value.fragment.copy(interval = value),
        )
        prefs(context).edit().putString(KEY_FRAGMENT_INTERVAL, value).apply()
        AppLog.d("SettingsStore: fragment interval=$value")
    }

    fun setMuxEnabled(context: Context, enabled: Boolean) {
        if (_networkOptions.value.mux.enabled == enabled) return
        _networkOptions.value = _networkOptions.value.copy(
            mux = _networkOptions.value.mux.copy(enabled = enabled),
        )
        prefs(context).edit().putBoolean(KEY_MUX_ENABLED, enabled).apply()
        AppLog.i("SettingsStore: MUX ${if (enabled) "включён" else "выключен"}")
    }

    fun setMuxConcurrency(context: Context, concurrency: Int) {
        val value = concurrency.coerceIn(1, 1024)
        if (_networkOptions.value.mux.concurrency == value) return
        _networkOptions.value = _networkOptions.value.copy(
            mux = _networkOptions.value.mux.copy(concurrency = value),
        )
        prefs(context).edit().putInt(KEY_MUX_CONCURRENCY, value).apply()
        AppLog.d("SettingsStore: mux concurrency=$value")
    }

    fun setMuxXudpConcurrency(context: Context, concurrency: Int) {
        val value = concurrency.coerceIn(1, 1024)
        if (_networkOptions.value.mux.xudpConcurrency == value) return
        _networkOptions.value = _networkOptions.value.copy(
            mux = _networkOptions.value.mux.copy(xudpConcurrency = value),
        )
        prefs(context).edit().putInt(KEY_MUX_XUDP_CONCURRENCY, value).apply()
        AppLog.d("SettingsStore: mux xudp concurrency=$value")
    }

    /**
     * Возвращает настройки к значениям по умолчанию. Стираются ВСЕ ключи
     * prefs (включая устаревшие вроде снятой с поддержки иконки-варианта) —
     * подписки и JSON-конфиг сбрасывает вызывающий код (см. SettingsTab).
     */
    fun reset(context: Context) {
        _theme.value = ThemeMode.SYSTEM
        _language.value = AppLanguage.AUTO
        _fontScale.value = FONT_SCALE_NORMAL
        _blockAds.value = true
        _autoConnect.value = false
        _autoConnectOnBoot.value = false
        _killSwitch.value = false
        _connectionMode.value = ConnectionMode.TUN
        _pingOptions.value = PingOptions()
        _networkOptions.value = NetworkOptions()
        _subscriptionUserAgent.value = defaultUserAgent(context)
        _serverSort.value = ServerSort.SUBSCRIPTION
        _updateInterval.value = UpdateInterval.NEVER
        prefs(context).edit()
            .clear()
            .putString(KEY_THEME, _theme.value.key)
            .putString(KEY_LANGUAGE, _language.value.code)
            .putFloat(KEY_FONT_SCALE, _fontScale.value)
            .putBoolean(KEY_BLOCK_ADS, _blockAds.value)
            .putBoolean(KEY_AUTO_CONNECT, _autoConnect.value)
            .putBoolean(KEY_AUTO_CONNECT_ON_BOOT, _autoConnectOnBoot.value)
            .putBoolean(KEY_KILL_SWITCH, _killSwitch.value)
            .putString(KEY_CONNECTION_MODE, _connectionMode.value.key)
            .apply()
        AppLog.i("SettingsStore: настройки сброшены к значениям по умолчанию")
    }

    /**
     * Тексты для текущего языка — для мест вне Compose (сервис, хранилище профилей).
     * AUTO раскрывается по системной locale; язык Compose получает так же.
     */
    fun strings(): OneBoxStrings = stringsFor(_language.value)

    /** Реальный язык интерфейса (AUTO → системный). */
    fun resolvedLanguage(): AppLanguage = AppLanguage.resolve(_language.value)

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
