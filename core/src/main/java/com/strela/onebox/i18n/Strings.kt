package com.strela.onebox.i18n

import java.text.SimpleDateFormat
import java.util.Date
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
    val shareLogsHint: String,
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
    val pasteLinkOrLines: String,
    val addedServers: (Int) -> String,
    val noNewServers: String,
    val configImported: String,
    val configsImported: (Int) -> String,
    val noConfigImported: String,
    val unrecognizedInput: String,
    val allSubscriptions: String,
    val subscriptionsServersBadge: (subscriptions: Int, servers: Int) -> String,
    val addServers: String,
    val subscriptionTitleOptional: String,
    val importAction: String,
    val clearField: String,
    val active: String,
    val noServersInSubscription: String,
    val noSubscriptions: String,
    val noSubscriptionsHint: String,
    val deleteSubscription: (String) -> String,
    val refreshSubscription: String,
    val serversCount: (Int) -> String,

    // Метаданные подписки: срок и трафик
    val expiresAt: (String) -> String,
    val expiredShort: String,
    val noExpiry: String,
    val trafficUsed: (used: String, limit: String) -> String,
    val trafficUsedNoLimit: (String) -> String,
    val formatDate: (Long) -> String,
    val formatBytes: (Long) -> String,

    // Импорт подписки по URL
    val importSubscriptionAction: String,
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
    val ping: String,
    val pingAll: String,
    val pingFailed: String,
    val pingMs: (Long) -> String,

    // Прочее
    val defaultSubscriptionName: (Int) -> String,
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
    shareLogsHint = "Отправить файл журнала",
    clearLogs = "Очистить логи",
    clearLogsHint = "Удалить сохранённые записи",
    logsCleared = "Логи очищены",
    about = "О приложении",
    appVersion = "Версия приложения",
    coreVersion = "Ядро xray",
    activeSubscription = "Активная подписка",
    resetSettings = "Сбросить настройки",
    resetSettingsHint = "Тема, язык и параметры подключения",
    resetDone = "Настройки сброшены",

    pasteLinkOrLines = "Вставьте ссылки, JSON-конфиг или base64-подписку",
    addedServers = { count -> "Добавлено серверов: $count" },
    noNewServers = "Новых серверов не найдено: дубликаты или ошибка разбора",
    configImported = "JSON-конфиг сохранён и выбран для подключения",
    configsImported = { count -> "Добавлено JSON-конфигов: $count" },
    noConfigImported = "Не удалось импортировать JSON-конфиг",
    unrecognizedInput = "Не удалось распознать формат: вставьте ссылки, JSON или base64",
    allSubscriptions = "Все подписки",
    subscriptionsServersBadge = { subscriptions, servers -> "подп. $subscriptions · серв. $servers" },
    addServers = "Импорт",
    subscriptionTitleOptional = "Название подписки (опционально)",
    importAction = "Импортировать",
    clearField = "Очистить поле",
    active = "активна",
    noServersInSubscription = "В подписке пока нет серверов",
    noSubscriptions = "Подписок пока нет",
    noSubscriptionsHint = "Импортируйте подписку по URL, ссылки, JSON-конфиг или base64.",
    deleteSubscription = { name -> "Удалить подписку $name" },
    refreshSubscription = "Обновить подписку",
    serversCount = { count ->
        val mod10 = count % 10
        val mod100 = count % 100
        when {
            mod10 == 1 && mod100 != 11 -> "$count сервер"
            mod10 in 2..4 && mod100 !in 12..14 -> "$count сервера"
            else -> "$count серверов"
        }
    },

    expiresAt = { date -> "Действует до $date" },
    expiredShort = "истекла",
    noExpiry = "Бессрочная",
    trafficUsed = { used, limit -> "Трафик: $used из $limit" },
    trafficUsedNoLimit = { used -> "Трафик: $used" },
    formatDate = { millis -> formatDateWith(millis, LOCALE_RU, "d MMM yyyy") },
    formatBytes = { bytes -> formatBytesWith(bytes, LOCALE_RU, BYTES_UNITS_RU) },

    importSubscriptionAction = "Подписка по URL",
    subscriptionDialogTitle = "Импорт подписки",
    subscriptionUrlLabel = "https://ссылка подписки или JSON-конфиг",
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
    ping = "Пинг",
    pingAll = "Пинг всех",
    pingFailed = "нет",
    pingMs = { millis -> "$millis мс" },

    defaultSubscriptionName = { number -> "Подписка $number" },
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
    shareLogsHint = "Send the log file",
    clearLogs = "Clear logs",
    clearLogsHint = "Delete saved entries",
    logsCleared = "Logs cleared",
    about = "About",
    appVersion = "App version",
    coreVersion = "xray core",
    activeSubscription = "Active subscription",
    resetSettings = "Reset settings",
    resetSettingsHint = "Theme, language, and connection options",
    resetDone = "Settings reset",

    pasteLinkOrLines = "Paste links, a JSON config, or a base64 subscription",
    addedServers = { count -> "Servers added: $count" },
    noNewServers = "No new servers found: duplicates or a parse error",
    configImported = "JSON config saved and selected for connection",
    configsImported = { count -> "JSON configs added: $count" },
    noConfigImported = "Could not import the JSON config",
    unrecognizedInput = "Could not detect the format: paste links, JSON, or base64",
    allSubscriptions = "All subscriptions",
    subscriptionsServersBadge = { subscriptions, servers -> "sub. $subscriptions · srv. $servers" },
    addServers = "Import",
    subscriptionTitleOptional = "Subscription title (optional)",
    importAction = "Import",
    clearField = "Clear field",
    active = "active",
    noServersInSubscription = "This subscription has no servers yet",
    noSubscriptions = "No subscriptions yet",
    noSubscriptionsHint = "Import a subscription by URL, links, a JSON config, or base64.",
    deleteSubscription = { name -> "Delete subscription $name" },
    refreshSubscription = "Refresh subscription",
    serversCount = { count -> if (count == 1) "1 server" else "$count servers" },

    expiresAt = { date -> "Valid until $date" },
    expiredShort = "expired",
    noExpiry = "No expiry",
    trafficUsed = { used, limit -> "Traffic: $used of $limit" },
    trafficUsedNoLimit = { used -> "Traffic: $used" },
    formatDate = { millis -> formatDateWith(millis, Locale.ENGLISH, "d MMM yyyy") },
    formatBytes = { bytes -> formatBytesWith(bytes, Locale.ENGLISH, BYTES_UNITS_EN) },

    importSubscriptionAction = "Subscription URL",
    subscriptionDialogTitle = "Import subscription",
    subscriptionUrlLabel = "https://subscription link or JSON config",
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
    ping = "Ping",
    pingAll = "Ping all",
    pingFailed = "none",
    pingMs = { millis -> "$millis ms" },

    defaultSubscriptionName = { number -> "Subscription $number" },
    vpnActiveNotification = "VPN is on",
    vpnChannelName = "OneBox VPN"
)

fun stringsFor(language: AppLanguage): OneBoxStrings = when (language) {
    AppLanguage.RU -> RuStrings
    AppLanguage.EN -> EnStrings
}

/** Форматирует дату окончания подписки: «24 окт 2026» / "24 Oct 2026". */
private fun formatDateWith(millis: Long, locale: Locale, pattern: String): String =
    runCatching {
        SimpleDateFormat(pattern, locale).format(Date(millis))
    }.getOrDefault("")

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

private val BYTES_UNITS_RU = arrayOf("Б", "КБ", "МБ", "ГБ", "ТБ")
private val BYTES_UNITS_EN = arrayOf("B", "KB", "MB", "GB", "TB")
