package com.strela.onebox.i18n

import java.util.Locale

/** Язык интерфейса. По умолчанию — русский. */
enum class AppLanguage(val code: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: RU
    }
}

/**
 * Все тексты интерфейса. Держим их обычным data-классом, а не в ресурсах:
 * язык переключается внутри приложения независимо от системной локали.
 */
data class OneBoxStrings(
    // Вкладки
    val tabMain: String,
    val tabServer: String,
    val tabSettings: String,

    // Статус подключения
    val statusStoppedTitle: String,
    val statusStoppedDescription: String,
    val statusStartingTitle: String,
    val statusStartingDescription: String,
    val statusRunningTitle: String,
    val statusRunningDescription: String,
    val statusErrorTitle: String,
    val statusErrorDescription: String,
    val statusStoppedShort: String,
    val statusStartingShort: String,
    val statusRunningShort: String,
    val statusErrorShort: String,

    // Главный экран
    val subscriptionServers: String,
    val starting: String,
    val connect: String,
    val disconnect: String,
    val retry: String,
    val shareLogs: String,
    val logsChooser: String,
    val chooseSubscription: String,
    val configMode: String,
    val configModeHint: String,

    // Настройки
    val settingsHint: String,
    val appearance: String,
    val themeTitle: String,
    val themeDark: String,
    val themeLight: String,
    val themeSystemShort: String,
    val textSize: String,
    val textSizeHint: String,
    val textSizeSmall: String,
    val textSizeNormal: String,
    val textSizeLarge: String,
    val textSizeHuge: String,
    val language: String,
    val languageRussian: String,
    val languageEnglish: String,
    val languageHint: String,
    val connection: String,
    val blockAds: String,
    val blockAdsHint: String,
    val autoConnect: String,
    val autoConnectHint: String,
    val diagnostics: String,
    val logsHint: String,
    val viewLogs: String,
    val viewLogsHint: String,
    val logsConsole: String,
    val logsConsoleEmpty: String,
    val importLogs: String,
    val importLogsHint: String,
    val clearLogs: String,
    val clearLogsHint: String,
    val logsCleared: String,
    val about: String,
    val appVersion: String,
    val coreVersion: String,
    val activeSubscription: String,
    val resetSettings: String,
    val resetSettingsHint: String,
    val resetDone: String,

    // Экран серверов
    val addedServers: (Int) -> String,
    val noNewServers: String,
    val unrecognizedInput: String,
    val allSubscriptions: String,
    val subscriptionsServersBadge: (subscriptions: Int, servers: Int) -> String,
    val importAction: String,
    val active: String,
    val showServers: String,
    val hideServers: String,
    val noServersInSubscription: String,
    val noSubscriptions: String,
    val noSubscriptionsHint: String,
    val deleteSubscription: (String) -> String,
    val refreshSubscription: String,

    // Метаданные подписки: срок и трафик
    val expiresLeft: (Long) -> String,
    val expiredShort: String,
    val noExpiry: String,
    val trafficUsed: (used: String, limit: String) -> String,
    val trafficUsedNoLimit: (String) -> String,
    val formatBytes: (Long) -> String,

    // Импорт подписки (кнопка «Добавить»)
    val addSubscriptionAction: String,
    val subscriptionDialogTitle: String,
    val subscriptionUrlLabel: String,
    val pasteFromClipboard: String,
    val clipboardNoHttpsUrl: String,
    val importingSubscription: String,
    val subscriptionAdded: (name: String, servers: Int) -> String,
    val subscriptionUpdated: (name: String, servers: Int) -> String,
    val errorInsecureConnection: String,
    val errorInvalidUrl: String,
    val errorNetwork: String,
    val errorNoServersInSubscription: String,
    val errorBadResponse: String,
    val serversSubscriptionTitle: String,

    // Строка сервера
    val deleteServer: (String) -> String,
    val pingAll: String,
    val pingFailed: String,
    val pingMs: (Long) -> String,

    // Настройки пинга (экран «Пинг»)
    val settingsPing: String,
    val settingsPingHint: String,
    val pingScreenTitle: String,
    val pingMethod: String,
    val pingMethodGet: String,
    val pingMethodHead: String,
    val pingMethodTcp: String,
    val pingMethodIcmp: String,
    val pingMethodHintGet: String,
    val pingMethodHintHead: String,
    val pingMethodHintTcp: String,
    val pingMethodHintIcmp: String,
    val pingTimeout: String,
    val pingTimeoutHint: String,
    val pingSeconds: (Int) -> String,
    val pingUrlTest: String,
    val pingUrlTestHint: String,
    val pingUrlPresetGoogle: String,
    val pingUrlPresetCloudflare: String,
    val pingUrlPresetGithub: String,
    val pingUrlOther: String,
    val pingUrlLabel: String,
    val pingUrlHint: String,

    // Прочее
    val defaultSubscriptionName: (Int) -> String,
    val close: String,
    val vpnActiveNotification: String,
    val vpnChannelName: String
)

val RuStrings = OneBoxStrings(
    tabMain = "Главная",
    tabServer = "Серверы",
    tabSettings = "Настройки",

    statusStoppedTitle = "Остановлен",
    statusStoppedDescription = "Трафик идёт напрямую, без VPN",
    statusStartingTitle = "Запуск…",
    statusStartingDescription = "Поднимаем VPN-туннель",
    statusRunningTitle = "VPN включён",
    statusRunningDescription = "Трафик защищён туннелем",
    statusErrorTitle = "Ошибка",
    statusErrorDescription = "Проверьте конфиг и поделитесь логами",
    statusStoppedShort = "Выкл",
    statusStartingShort = "Запуск",
    statusRunningShort = "Вкл",
    statusErrorShort = "Ошибка",

    subscriptionServers = "Серверы подписки",
    starting = "Запуск…",
    connect = "Подключиться",
    disconnect = "Отключиться",
    retry = "Повторить",
    shareLogs = "Поделиться логами",
    logsChooser = "Логи OneBox",
    chooseSubscription = "Выбрать подписку",
    configMode = "JSON-конфиг",
    configModeHint = "Используется вставленный JSON-конфиг. Выберите подписку, чтобы переключиться на серверы.",

    settingsHint = "Изменения применяются сразу",
    appearance = "Внешний вид",
    themeTitle = "Тема оформления",
    themeDark = "Тёмная",
    themeLight = "Светлая",
    themeSystemShort = "Система",
    textSize = "Размер текста",
    textSizeHint = "Применяется ко всему интерфейсу",
    textSizeSmall = "Мелкий",
    textSizeNormal = "Обычный",
    textSizeLarge = "Крупный",
    textSizeHuge = "Очень крупный",
    language = "Язык",
    languageRussian = "Русский",
    languageEnglish = "English",
    languageHint = "Русский — по умолчанию",
    connection = "Подключение",
    blockAds = "Блокировать рекламу",
    blockAdsHint = "Рекламные и трекинговые домены — при следующем подключении",
    autoConnect = "Автоподключение",
    autoConnectHint = "Запускать VPN при старте, если доступ уже разрешён",
    diagnostics = "Диагностика",
    logsHint = "Помогут разобраться в ошибках подключения",
    viewLogs = "Посмотреть логи",
    viewLogsHint = "Консоль журнала приложения",
    logsConsole = "Логи",
    logsConsoleEmpty = "Журнал пуст",
    importLogs = "Импортировать логи",
    importLogsHint = "Передать журнал файлом",
    clearLogs = "Очистить логи",
    clearLogsHint = "Удалить сохранённые записи",
    logsCleared = "Логи очищены",
    about = "О приложении",
    appVersion = "Версия приложения",
    coreVersion = "Ядро xray",
    activeSubscription = "Активная подписка",
    resetSettings = "Сбросить настройки",
    resetSettingsHint = "Всё: настройки, подписки и конфиг",
    resetDone = "Настройки сброшены",

    addedServers = { count -> "Добавлено серверов: $count" },
    noNewServers = "Новых серверов не найдено: дубликаты или ошибка разбора",
    unrecognizedInput = "Не удалось распознать формат: вставьте ссылки, JSON или base64",
    allSubscriptions = "Все подписки",
    subscriptionsServersBadge = { subscriptions, servers -> "подп. $subscriptions · серв. $servers" },
    importAction = "Импортировать",
    active = "активна",
    showServers = "Показать список серверов",
    hideServers = "Скрыть список серверов",
    noServersInSubscription = "В подписке пока нет серверов",
    noSubscriptions = "Подписок пока нет",
    noSubscriptionsHint = "Импортируйте подписку",
    deleteSubscription = { name -> "Удалить подписку $name" },
    refreshSubscription = "Обновить подписку",

    expiresLeft = { millis -> formatRemainingWith(millis, LOCALE_RU, RU_REMAINING_UNITS, "истекла") },
    expiredShort = "истекла",
    noExpiry = "Бессрочная",
    trafficUsed = { used, limit -> "Трафик: $used из $limit" },
    trafficUsedNoLimit = { used -> "Трафик: $used" },
    formatBytes = { bytes -> formatBytesWith(bytes, LOCALE_RU, BYTES_UNITS_RU) },

    addSubscriptionAction = "Добавить",
    subscriptionDialogTitle = "Добавить подписку",
    subscriptionUrlLabel = "URL, ссылки серверов, JSON или base64",
    pasteFromClipboard = "Вставить из буфера обмена",
    clipboardNoHttpsUrl = "Буфер обмена пуст или не содержит ссылку/JSON",
    importingSubscription = "Загрузка подписки…",
    subscriptionAdded = { name, servers -> "Подписка «$name» добавлена: серверов $servers" },
    subscriptionUpdated = { name, servers -> "Подписка «$name» обновлена: серверов $servers" },
    errorInsecureConnection = "Небезопасное соединение: разрешены только ссылки https://",
    errorInvalidUrl = "Некорректный формат ссылки",
    errorNetwork = "Ошибка сети: не удалось скачать подписку",
    errorNoServersInSubscription = "В подписке не найдено серверов",
    errorBadResponse = "Неверный формат ответа сервера",
    serversSubscriptionTitle = "Серверы",

    deleteServer = { name -> "Удалить сервер $name" },
    pingAll = "Пинг всех",
    pingFailed = "нет",
    pingMs = { millis -> "$millis мс" },

    settingsPing = "Пинг",
    settingsPingHint = "Метод, тайм-аут и URL-тест",
    pingScreenTitle = "Пинг",
    pingMethod = "Метод пинга",
    pingMethodGet = "via Proxy GET",
    pingMethodHead = "via Proxy HEAD",
    pingMethodTcp = "TCP",
    pingMethodIcmp = "ICMP",
    pingMethodHintGet = "HTTP GET через ядро — тот же путь, что у трафика",
    pingMethodHintHead = "HTTP HEAD через ядро — тот же путь, что у трафика",
    pingMethodHintTcp = "Только TCP-соединение с сервером, без ядра",
    pingMethodHintIcmp = "ICMP-эхо на адрес сервера — если устройство разрешает",
    pingTimeout = "Тайм-аут",
    pingTimeoutHint = "Ждать ответ не дольше",
    pingSeconds = { seconds -> "$seconds с" },
    pingUrlTest = "URL-тест",
    pingUrlTestHint = "Цель HTTP-проб (GET/HEAD)",
    pingUrlPresetGoogle = "Google · generate_204",
    pingUrlPresetCloudflare = "Cloudflare · generate_204",
    pingUrlPresetGithub = "GitHub · robots.txt",
    pingUrlOther = "Другой URL…",
    pingUrlLabel = "URL",
    pingUrlHint = "Используется с GET/HEAD; для TCP и ICMP нужен только адрес сервера",

    defaultSubscriptionName = { number -> "Подписка $number" },
    close = "Закрыть",
    vpnActiveNotification = "VPN активен",
    vpnChannelName = "OneBox VPN"
)

val EnStrings = OneBoxStrings(
    tabMain = "Main",
    tabServer = "Servers",
    tabSettings = "Settings",

    statusStoppedTitle = "Stopped",
    statusStoppedDescription = "Traffic goes directly, without VPN",
    statusStartingTitle = "Starting…",
    statusStartingDescription = "Bringing up the VPN tunnel",
    statusRunningTitle = "VPN is on",
    statusRunningDescription = "Traffic is protected by the tunnel",
    statusErrorTitle = "Error",
    statusErrorDescription = "Check the config and share the logs",
    statusStoppedShort = "Off",
    statusStartingShort = "Starting",
    statusRunningShort = "On",
    statusErrorShort = "Error",

    subscriptionServers = "Subscription servers",
    starting = "Starting…",
    connect = "Connect",
    disconnect = "Disconnect",
    retry = "Retry",
    shareLogs = "Share logs",
    logsChooser = "OneBox logs",
    chooseSubscription = "Choose a subscription",
    configMode = "JSON config",
    configModeHint = "The pasted JSON config is in use. Choose a subscription to switch to servers.",

    settingsHint = "Changes apply immediately",
    appearance = "Appearance",
    themeTitle = "Theme",
    themeDark = "Dark",
    themeLight = "Light",
    themeSystemShort = "System",
    textSize = "Text size",
    textSizeHint = "Applies to the entire interface",
    textSizeSmall = "Small",
    textSizeNormal = "Normal",
    textSizeLarge = "Large",
    textSizeHuge = "Extra large",
    language = "Language",
    languageRussian = "Русский",
    languageEnglish = "English",
    languageHint = "Russian is the default",
    connection = "Connection",
    blockAds = "Block ads",
    blockAdsHint = "Ad and tracking domains — on the next connection",
    autoConnect = "Auto-connect",
    autoConnectHint = "Start VPN on launch if access is already granted",
    diagnostics = "Diagnostics",
    logsHint = "Helps diagnose connection problems",
    viewLogs = "View logs",
    viewLogsHint = "In-app log console",
    logsConsole = "Logs",
    logsConsoleEmpty = "Log is empty",
    importLogs = "Import logs",
    importLogsHint = "Share the log file",
    clearLogs = "Clear logs",
    clearLogsHint = "Delete saved entries",
    logsCleared = "Logs cleared",
    about = "About",
    appVersion = "App version",
    coreVersion = "xray core",
    activeSubscription = "Active subscription",
    resetSettings = "Reset settings",
    resetSettingsHint = "Everything: settings, subscriptions, and config",
    resetDone = "Settings reset",

    addedServers = { count -> "Servers added: $count" },
    noNewServers = "No new servers found: duplicates or a parse error",
    unrecognizedInput = "Could not detect the format: paste links, JSON, or base64",
    allSubscriptions = "All subscriptions",
    subscriptionsServersBadge = { subscriptions, servers -> "sub. $subscriptions · srv. $servers" },
    importAction = "Import",
    active = "active",
    showServers = "Show server list",
    hideServers = "Hide server list",
    noServersInSubscription = "This subscription has no servers yet",
    noSubscriptions = "No subscriptions yet",
    noSubscriptionsHint = "Import a subscription",
    deleteSubscription = { name -> "Delete subscription $name" },
    refreshSubscription = "Refresh subscription",

    expiresLeft = { millis -> formatRemainingWith(millis, Locale.ENGLISH, EN_REMAINING_UNITS, "expired") },
    expiredShort = "expired",
    noExpiry = "No expiry",
    trafficUsed = { used, limit -> "Traffic: $used of $limit" },
    trafficUsedNoLimit = { used -> "Traffic: $used" },
    formatBytes = { bytes -> formatBytesWith(bytes, Locale.ENGLISH, BYTES_UNITS_EN) },

    addSubscriptionAction = "Add",
    subscriptionDialogTitle = "Add subscription",
    subscriptionUrlLabel = "URL, server links, JSON, or base64",
    pasteFromClipboard = "Paste from clipboard",
    clipboardNoHttpsUrl = "Clipboard is empty or has no link/JSON",
    importingSubscription = "Downloading subscription…",
    subscriptionAdded = { name, servers -> "Subscription “$name” added: $servers servers" },
    subscriptionUpdated = { name, servers -> "Subscription “$name” updated: $servers servers" },
    errorInsecureConnection = "Insecure connection: only https:// links are allowed",
    errorInvalidUrl = "Invalid link format",
    errorNetwork = "Network error: could not download the subscription",
    errorNoServersInSubscription = "No servers found in the subscription",
    errorBadResponse = "Unexpected server response format",
    serversSubscriptionTitle = "Servers",

    deleteServer = { name -> "Delete server $name" },
    pingAll = "Ping all",
    pingFailed = "none",
    pingMs = { millis -> "$millis ms" },

    settingsPing = "Ping",
    settingsPingHint = "Method, timeout, and URL test",
    pingScreenTitle = "Ping",
    pingMethod = "Ping method",
    pingMethodGet = "via Proxy GET",
    pingMethodHead = "via Proxy HEAD",
    pingMethodTcp = "TCP",
    pingMethodIcmp = "ICMP",
    pingMethodHintGet = "HTTP GET through the core — the same path as real traffic",
    pingMethodHintHead = "HTTP HEAD through the core — the same path as real traffic",
    pingMethodHintTcp = "Plain TCP connection to the server, without the core",
    pingMethodHintIcmp = "ICMP echo to the server address — if the device allows it",
    pingTimeout = "Timeout",
    pingTimeoutHint = "Maximum wait for a reply",
    pingSeconds = { seconds -> "$seconds s" },
    pingUrlTest = "URL test",
    pingUrlTestHint = "HTTP probe target (GET/HEAD)",
    pingUrlPresetGoogle = "Google · generate_204",
    pingUrlPresetCloudflare = "Cloudflare · generate_204",
    pingUrlPresetGithub = "GitHub · robots.txt",
    pingUrlOther = "Custom URL…",
    pingUrlLabel = "URL",
    pingUrlHint = "Used with GET/HEAD; TCP and ICMP only need the server address",

    defaultSubscriptionName = { number -> "Subscription $number" },
    close = "Close",
    vpnActiveNotification = "VPN is on",
    vpnChannelName = "OneBox VPN"
)

fun stringsFor(language: AppLanguage): OneBoxStrings = when (language) {
    AppLanguage.RU -> RuStrings
    AppLanguage.EN -> EnStrings
}

/**
 * Остаток срока подписки: «Осталось 45 дн.» / "45 d left".
 * Истёкшая подписка — «истекла» / "expired". До двух суток считаем в часах,
 * до двух месяцев — в днях, дальше — в месяцах (вверх — неполный час/день
 * уже целый).
 */
private fun formatRemainingWith(millis: Long, locale: Locale, units: Array<String>, expired: String): String {
    val diff = millis - System.currentTimeMillis()
    if (diff <= 0) return expired
    val hours = (diff + MILLIS_PER_HOUR - 1) / MILLIS_PER_HOUR
    val (count, unit) = when {
        hours < 48L -> hours to units[0]
        hours < 48L * 60L -> (hours + 23L) / 24L to units[1]
        else -> Math.round(hours / 24.0 / 30.436875).toLong().coerceAtLeast(2L) to units[2]
    }
    return when (locale.language) {
        "ru" -> "Осталось $count $unit"
        else -> "$count $unit left"
    }
}

private const val MILLIS_PER_HOUR = 3_600_000L

private val EN_REMAINING_UNITS = arrayOf("h", "d", "mo")

/**
 * Человекочитаемый размер трафика: 2048 → «2 КБ».
 * До килобайта байты не дробятся, выше — одна десятая.
 */
private fun formatBytesWith(bytes: Long, locale: Locale, units: Array<String>): String {
    if (bytes < 0) return "—"
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.size - 1) {
        value /= 1024.0
        index++
    }
    return if (index == 0) {
        "$bytes ${units[0]}"
    } else {
        val pattern = if (value % 1.0 == 0.0) "%.0f %s" else "%.1f %s"
        String.format(locale, pattern, value, units[index])
    }
}

private val LOCALE_RU = Locale("ru")
private val RU_REMAINING_UNITS = arrayOf("ч.", "дн.", "мес.")

private val BYTES_UNITS_RU = arrayOf("Б", "КБ", "МБ", "ГБ", "ТБ")
private val BYTES_UNITS_EN = arrayOf("B", "KB", "MB", "GB", "TB")
