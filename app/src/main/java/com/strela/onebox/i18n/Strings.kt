package com.strela.onebox.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

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
    val profileServers: String,
    val starting: String,
    val connect: String,
    val disconnect: String,
    val retry: String,
    val shareLogs: String,
    val logsChooser: String,
    val chooseProfile: String,
    val configMode: String,
    val configModeHint: String,

    // Настройки
    val settingsHint: String,
    val appearance: String,
    val themeTitle: String,
    val themeDark: String,
    val themeDarkHint: String,
    val themeLight: String,
    val themeLightHint: String,
    val themeSystem: String,
    val themeSystemShort: String,
    val themeSystemHint: String,
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
    val logs: String,
    val logsHint: String,
    val shareLogsHint: String,
    val clearLogs: String,
    val clearLogsHint: String,
    val logsCleared: String,
    val about: String,
    val appVersion: String,
    val coreVersion: String,
    val activeProfile: String,
    val resetSettings: String,
    val resetSettingsHint: String,
    val resetDone: String,

    // Экран серверов
    val pasteLinkOrLines: String,
    val addedServers: (Int) -> String,
    val noNewServers: String,
    val configImported: String,
    val unrecognizedInput: String,
    val allProfilesAndServers: String,
    val profilesServersBadge: (profiles: Int, servers: Int) -> String,
    val addServers: String,
    val profileName: String,
    val profileNamePlaceholder: String,
    val importAction: String,
    val clearField: String,
    val active: String,
    val noServersInProfile: String,
    val noProfiles: String,
    val noProfilesHint: String,
    val deleteProfile: (String) -> String,
    val serversCount: (Int) -> String,

    // Строка сервера
    val deleteServer: (String) -> String,
    val ping: String,
    val pingFailed: String,
    val pingMs: (Long) -> String,

    // Прочее
    val charCount: (Int) -> String,
    val defaultProfileName: (Int) -> String,
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

    profileServers = "Серверы профиля",
    starting = "Запуск…",
    connect = "Подключиться",
    disconnect = "Отключиться",
    retry = "Повторить",
    shareLogs = "Поделиться логами",
    logsChooser = "Логи OneBox",
    chooseProfile = "Выбрать профиль",
    configMode = "JSON-конфиг",
    configModeHint = "Используется вставленный JSON-конфиг. Выберите профиль, чтобы переключиться на серверы.",

    settingsHint = "Изменения применяются сразу",
    appearance = "Внешний вид",
    themeTitle = "Тема оформления",
    themeDark = "Тёмная",
    themeDarkHint = "Тёмный фон и светлый текст",
    themeLight = "Светлая",
    themeLightHint = "Светлый фон и тёмный текст",
    themeSystem = "Как в системе",
    themeSystemShort = "Система",
    themeSystemHint = "Как в настройках устройства",
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
    logs = "Логи",
    logsHint = "Помогут разобраться в ошибках подключения",
    shareLogsHint = "Отправить файл журнала",
    clearLogs = "Очистить логи",
    clearLogsHint = "Удалить сохранённые записи",
    logsCleared = "Логи очищены",
    about = "О приложении",
    appVersion = "Версия приложения",
    coreVersion = "Ядро xray",
    activeProfile = "Активный профиль",
    resetSettings = "Сбросить настройки",
    resetSettingsHint = "Тема, язык и параметры подключения",
    resetDone = "Настройки сброшены",

    pasteLinkOrLines = "Вставьте ссылки, JSON-конфиг или base64-подписку",
    addedServers = { count -> "Добавлено серверов: $count" },
    noNewServers = "Новых серверов не найдено: дубликаты или ошибка разбора",
    configImported = "JSON-конфиг сохранён и выбран для подключения",
    unrecognizedInput = "Не удалось распознать формат: вставьте ссылки, JSON или base64",
    allProfilesAndServers = "Все профили и серверы",
    profilesServersBadge = { profiles, servers -> "проф. $profiles · серв. $servers" },
    addServers = "Импорт",
    profileName = "Название профиля",
    profileNamePlaceholder = "Например, «Подписка» (только для ссылок)",
    importAction = "Импортировать",
    clearField = "Очистить поле",
    active = "активен",
    noServersInProfile = "В профиле пока нет серверов",
    noProfiles = "Профилей пока нет",
    noProfilesHint = "Импортируйте ссылки, JSON-конфиг или base64-подписку.",
    deleteProfile = { name -> "Удалить профиль $name" },
    serversCount = { count ->
        val mod10 = count % 10
        val mod100 = count % 100
        when {
            mod10 == 1 && mod100 != 11 -> "$count сервер"
            mod10 in 2..4 && mod100 !in 12..14 -> "$count сервера"
            else -> "$count серверов"
        }
    },

    deleteServer = { name -> "Удалить сервер $name" },
    ping = "Пинг",
    pingFailed = "нет",
    pingMs = { millis -> "$millis мс" },

    charCount = { count -> "$count симв." },
    defaultProfileName = { number -> "Профиль $number" },
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

    profileServers = "Profile servers",
    starting = "Starting…",
    connect = "Connect",
    disconnect = "Disconnect",
    retry = "Retry",
    shareLogs = "Share logs",
    logsChooser = "OneBox logs",
    chooseProfile = "Choose a profile",
    configMode = "JSON config",
    configModeHint = "The pasted JSON config is in use. Choose a profile to switch to servers.",

    settingsHint = "Changes apply immediately",
    appearance = "Appearance",
    themeTitle = "Theme",
    themeDark = "Dark",
    themeDarkHint = "Dark background and light text",
    themeLight = "Light",
    themeLightHint = "Light background and dark text",
    themeSystem = "System",
    themeSystemShort = "System",
    themeSystemHint = "Follow the device setting",
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
    logs = "Logs",
    logsHint = "Helps diagnose connection problems",
    shareLogsHint = "Send the log file",
    clearLogs = "Clear logs",
    clearLogsHint = "Delete saved entries",
    logsCleared = "Logs cleared",
    about = "About",
    appVersion = "App version",
    coreVersion = "xray core",
    activeProfile = "Active profile",
    resetSettings = "Reset settings",
    resetSettingsHint = "Theme, language, and connection options",
    resetDone = "Settings reset",

    pasteLinkOrLines = "Paste links, a JSON config, or a base64 subscription",
    addedServers = { count -> "Servers added: $count" },
    noNewServers = "No new servers found: duplicates or a parse error",
    configImported = "JSON config saved and selected for connection",
    unrecognizedInput = "Could not detect the format: paste links, JSON, or base64",
    allProfilesAndServers = "All profiles and servers",
    profilesServersBadge = { profiles, servers -> "prof. $profiles · srv. $servers" },
    addServers = "Import",
    profileName = "Profile name",
    profileNamePlaceholder = "For example, “Subscription” (links only)",
    importAction = "Import",
    clearField = "Clear field",
    active = "active",
    noServersInProfile = "This profile has no servers yet",
    noProfiles = "No profiles yet",
    noProfilesHint = "Import links, a JSON config, or a base64 subscription.",
    deleteProfile = { name -> "Delete profile $name" },
    serversCount = { count -> if (count == 1) "1 server" else "$count servers" },

    deleteServer = { name -> "Delete server $name" },
    ping = "Ping",
    pingFailed = "none",
    pingMs = { millis -> "$millis ms" },

    charCount = { count -> "$count chars" },
    defaultProfileName = { number -> "Profile $number" },
    vpnActiveNotification = "VPN is on",
    vpnChannelName = "OneBox VPN"
)

fun stringsFor(language: AppLanguage): OneBoxStrings = when (language) {
    AppLanguage.RU -> RuStrings
    AppLanguage.EN -> EnStrings
}

val LocalStrings = staticCompositionLocalOf { RuStrings }

/** Текущие тексты: русские или английские, в зависимости от настроек. */
val strings: OneBoxStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalStrings.current
