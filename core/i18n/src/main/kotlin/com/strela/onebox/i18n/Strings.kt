package com.strela.onebox.i18n

import java.util.Locale

/**
 * Язык интерфейса. AUTO — системный язык устройства (по умолчанию):
 * подходящий из поддерживаемых выбирается по locale, остальные — английский.
 */
enum class AppLanguage(val code: String) {
    AUTO("auto"),
    RU("ru"),
    EN("en"),
    FA("fa"),
    ZH("zh");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: AUTO

        /** Реальный язык интерфейса: AUTO раскрывается по системной locale. */
        fun resolve(language: AppLanguage, locale: Locale = Locale.getDefault()): AppLanguage {
            if (language != AUTO) return language
            return when (locale.language.lowercase(Locale.ROOT)) {
                "ru" -> RU
                "fa" -> FA
                "zh" -> ZH
                else -> EN
            }
        }
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
    val appearanceHint: String,
    val connectionHint: String,
    val diagnosticsHint: String,
    val aboutHint: String,
    // Экран подключения: режим
    val connectionMode: String,
    val connectionModeHint: String,
    val modeTun: String,
    val modeTunHint: String,
    val modeProxy: String,
    val modeProxyHint: String,
    val modeTunPlusProxy: String,
    val modeTunPlusProxyHint: String,
    val themeTitle: String,
    val themeDark: String,
    val themeLight: String,
    val themeGreen: String,
    val themeAmoled: String,
    val themeSystemShort: String,
    val textSize: String,
    val textSizeHint: String,
    val textSizeSmall: String,
    val textSizeNormal: String,
    val textSizeLarge: String,
    val textSizeHuge: String,
    val language: String,
    val languageAuto: String,
    val languageRussian: String,
    val languageEnglish: String,
    val languagePersian: String,
    val languageChinese: String,
    val languageHint: String,
    val connection: String,
    val blockAds: String,
    val blockAdsHint: String,
    val autoConnect: String,
    val autoConnectHint: String,
    val autoConnectOnBoot: String,
    val autoConnectOnBootHint: String,
    val killSwitch: String,
    val killSwitchHint: String,
    // Экран сети
    val network: String,
    val networkHint: String,
    val mtu: String,
    val mtuHint: String,
    val ipType: String,
    val ipTypeHint: String,
    val ipAuto: String,
    val ipv4: String,
    val ipv6: String,
    val fragmentation: String,
    val fragmentationHint: String,
    val fragmentPackets: String,
    val fragmentPacketsHint: String,
    val fragmentLength: String,
    val fragmentLengthHint: String,
    val fragmentInterval: String,
    val fragmentIntervalHint: String,
    val mux: String,
    val muxHint: String,
    val muxConcurrency: String,
    val muxConcurrencyHint: String,
    val muxXudp: String,
    val muxXudpHint: String,
    val diagnostics: String,
    val logsHint: String,
    val viewLogs: String,
    val viewLogsHint: String,
    val logsConsole: String,
    val logsConsoleEmpty: String,
    val logsSearch: String,
    val logsFilterAll: String,
    val logsNoMatches: String,
    val logsJumpToEnd: String,
    val importLogs: String,
    val importLogsHint: String,
    val clearLogs: String,
    val clearLogsHint: String,
    val logsCleared: String,
    val about: String,
    val aboutDescription: String,
    val githubProject: String,
    val githubProjectHint: String,
    val telegramChannel: String,
    val telegramChannelHint: String,
    val licensesTitle: String,
    val licensesHint: String,
    val licensesDescription: String,
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
    // Экран настроек подписок
    val subscriptionSettings: String,
    val subscriptionSettingsHint: String,
    val userAgent: String,
    val userAgentHint: String,
    val serverSort: String,
    val serverSortHint: String,
    val sortSubscription: String,
    val sortPing: String,
    val sortName: String,
    val updateInterval: String,
    val updateIntervalHint: String,
    val intervalNever: String,
    val interval30m: String,
    val interval60m: String,
    val interval2h: String,
    val interval6h: String,
    val interval12h: String,
    val interval24h: String,

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
    // Уведомление VPN-сервиса
    val notificationConnectingTitle: String,
    val notificationRunning: String,
    val notificationServer: (String) -> String,
    val notificationPing: (String) -> String,
    val notificationPingNa: String,
    val notificationModeTun: String,
    val notificationModeProxy: String,
    val notificationModeTunProxy: String,
    val notificationKillSwitch: String,
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
    appearanceHint = "Тема, размер текста и язык",
    connectionHint = "Режим трафика, реклама, автозапуск",
    diagnosticsHint = "Пинг и журнал приложения",
    aboutHint = "Версия, исходный код и связи",
    connectionMode = "Режим подключения",
    connectionModeHint = "Применяется при следующем подключении",
    modeTun = "TUN",
    modeTunHint = "Весь трафик через VPN-туннель",
    modeProxy = "Proxy",
    modeProxyHint = "Локальный прокси-порт, без туннеля",
    modeTunPlusProxy = "TUN + Proxy",
    modeTunPlusProxyHint = "Туннель плюс локальный прокси-порт",
    themeTitle = "Тема оформления",
    themeDark = "Тёмная",
    themeLight = "Светлая",
    themeGreen = "Зелёная",
    themeAmoled = "AMOLED чёрная",
    themeSystemShort = "Система",
    textSize = "Размер текста",
    textSizeHint = "Применяется ко всему интерфейсу",
    textSizeSmall = "Мелкий",
    textSizeNormal = "Обычный",
    textSizeLarge = "Крупный",
    textSizeHuge = "Очень крупный",
    language = "Язык",
    languageAuto = "Авто (системный)",
    languageRussian = "Русский",
    languageEnglish = "English",
    languagePersian = "فارسی",
    languageChinese = "中文",
    languageHint = "Авто — по системному языку устройства",
    connection = "Подключение",
    blockAds = "Блокировать рекламу",
    blockAdsHint = "Рекламные и трекинговые домены — при следующем подключении",
    autoConnect = "Автоподключение",
    autoConnectHint = "Запускать VPN при старте, если доступ уже разрешён",
    autoConnectOnBoot = "Автозапуск при загрузке",
    autoConnectOnBootHint = "Поднимать VPN после перезагрузки устройства",
    killSwitch = "Kill Switch",
    killSwitchHint = "Блокировать интернет, пока VPN не подключён",
    network = "Сеть",
    networkHint = "MTU, тип IP, фрагментация и MUX",
    mtu = "MTU туннеля",
    mtuHint = "Размер пакета туннеля, байты",
    ipType = "Тип IP",
    ipTypeHint = "Предпочтительное семейство адресов при разрешении доменов",
    ipAuto = "Авто",
    ipv4 = "IPv4",
    ipv6 = "IPv6",
    fragmentation = "Фрагментация TLS",
    fragmentationHint = "Дробить TLS-хендшейк для обхода DPI (VLESS/VMess/Trojan)",
    fragmentPackets = "Фрагментируемые пакеты",
    fragmentPacketsHint = "tlshello или диапазон, например 1-3",
    fragmentLength = "Длина фрагмента",
    fragmentLengthHint = "Диапазон в байтах, например 100-200",
    fragmentInterval = "Интервал фрагмента",
    fragmentIntervalHint = "Пауза между фрагментами, мс, например 10-20",
    mux = "MUX",
    muxHint = "Мультиплексирование соединений (VLESS/VMess/Trojan)",
    muxConcurrency = "Параллельные соединения",
    muxConcurrencyHint = "Число TCP-соединений в одном mux-канале",
    muxXudp = "Параллельные UDP",
    muxXudpHint = "Число UDP-соединений (XUDP)",
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
    logsSearch = "Поиск по журналу",
    logsFilterAll = "Все",
    logsNoMatches = "Совпадений не найдено",
    logsJumpToEnd = "К концу журнала",
    about = "О приложении",
    aboutDescription = "Открытый VPN-клиент на ядре xray: VLESS, VMess, Trojan, Shadowsocks, SOCKS и Hysteria2.",
    githubProject = "GitHub",
    githubProjectHint = "github.com/StrelaTech/onebox",
    telegramChannel = "Telegram",
    telegramChannelHint = "t.me/StrelaTech",
    licensesTitle = "Лицензии open-source",
    licensesHint = "Код и библиотеки проекта",
    licensesDescription = """
        OneBox — открытый исходный код (лицензия проекта в репозитории).

        xray-core (github.com/xtls/xray-core) — лицензия MIT: ядро VPN-подключений.
        quic-go (github.com/quic-go/quic-go) — лицензия MIT: транспорт HTTP/3 в Hysteria2.
        gomobile (github.com/golang/go / golang.org/x/mobile) — лицензия BSD-3: Go на Android.
        Jetpack Compose, AndroidX, Kotlin (Google, JetBrains) — лицензия Apache 2.0.
    """.trimIndent(),
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
    subscriptionSettings = "Подписки",
    subscriptionSettingsHint = "Обновление, сортировка серверов и User-Agent",
    userAgent = "User-Agent",
    userAgentHint = "Заголовок запросов к серверам подписок",
    serverSort = "Сортировка серверов",
    serverSortHint = "Порядок серверов в списке подписки",
    sortSubscription = "Как в подписке",
    sortPing = "По пингу",
    sortName = "По имени",
    updateInterval = "Интервал обновления",
    updateIntervalHint = "Автоматически обновлять URL-подписки в фоне",
    intervalNever = "Никогда",
    interval30m = "30 минут",
    interval60m = "60 минут",
    interval2h = "2 часа",
    interval6h = "6 часов",
    interval12h = "12 часов",
    interval24h = "24 часа",

    expiresLeft = { millis -> formatRemainingWith(millis, LOCALE_RU, RU_REMAINING_UNITS, "истекла") },
    expiredShort = "истекла",
    noExpiry = "Бессрочная",
    trafficUsed = { used, limit -> "Трафик: $used из $limit" },
    trafficUsedNoLimit = { used -> "Трафик: $used / ∞" },
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
    pingMethodGet = "HTTP GET",
    pingMethodHead = "HTTP HEAD",
    pingMethodTcp = "TCP",
    pingMethodIcmp = "ICMP",
    pingTimeout = "Тайм-аут",
    pingTimeoutHint = "Ждать ответ не дольше",
    pingSeconds = { seconds -> "$seconds с" },
    pingUrlTest = "URL-тест",
    pingUrlTestHint = "Адрес для GET/HEAD",
    pingUrlPresetGoogle = "Google · generate_204",
    pingUrlPresetCloudflare = "Cloudflare · generate_204",
    pingUrlPresetGithub = "GitHub · robots.txt",
    pingUrlOther = "Другой URL…",
    pingUrlLabel = "URL",
    pingUrlHint = "Для GET/HEAD",

    defaultSubscriptionName = { number -> "Подписка $number" },
    close = "Закрыть",
    vpnActiveNotification = "VPN активен",
    notificationConnectingTitle = "Подключение…",
    notificationRunning = "Подключено",
    notificationServer = { name -> "Сервер: $name" },
    notificationPing = { ms -> "Пинг: $ms" },
    notificationPingNa = "Пинг: —",
    notificationModeTun = "TUN-режим",
    notificationModeProxy = "Proxy-режим",
    notificationModeTunProxy = "TUN + Proxy",
    notificationKillSwitch = "Kill Switch: интернет заблокирован",
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
    appearanceHint = "Theme, text size, and language",
    connectionHint = "Traffic mode, ads, auto-start",
    diagnosticsHint = "Ping and app log",
    aboutHint = "Version, source code, and links",
    connectionMode = "Connection mode",
    connectionModeHint = "Applied on the next connection",
    modeTun = "TUN",
    modeTunHint = "All traffic through the VPN tunnel",
    modeProxy = "Proxy",
    modeProxyHint = "Local proxy port, no tunnel",
    modeTunPlusProxy = "TUN + Proxy",
    modeTunPlusProxyHint = "Tunnel plus a local proxy port",
    themeTitle = "Theme",
    themeDark = "Dark",
    themeLight = "Light",
    themeGreen = "Green",
    themeAmoled = "AMOLED black",
    themeSystemShort = "System",
    textSize = "Text size",
    textSizeHint = "Applies to the entire interface",
    textSizeSmall = "Small",
    textSizeNormal = "Normal",
    textSizeLarge = "Large",
    textSizeHuge = "Extra large",
    language = "Language",
    languageAuto = "Auto (system)",
    languageRussian = "Русский",
    languageEnglish = "English",
    languagePersian = "فارسی",
    languageChinese = "中文",
    languageHint = "Auto follows the device language",
    connection = "Connection",
    blockAds = "Block ads",
    blockAdsHint = "Ad and tracking domains — on the next connection",
    autoConnect = "Auto-connect",
    autoConnectHint = "Start VPN on launch if access is already granted",
    autoConnectOnBoot = "Start on boot",
    autoConnectOnBootHint = "Bring VPN up after the device restarts",
    killSwitch = "Kill Switch",
    killSwitchHint = "Block the internet while VPN is not connected",
    network = "Network",
    networkHint = "MTU, IP type, fragmentation and MUX",
    mtu = "Tunnel MTU",
    mtuHint = "Tunnel packet size, bytes",
    ipType = "IP type",
    ipTypeHint = "Preferred address family for DNS resolution",
    ipAuto = "Auto",
    ipv4 = "IPv4",
    ipv6 = "IPv6",
    fragmentation = "TLS fragmentation",
    fragmentationHint = "Split the TLS handshake to bypass DPI (VLESS/VMess/Trojan)",
    fragmentPackets = "Packets to fragment",
    fragmentPacketsHint = "tlshello or a range such as 1-3",
    fragmentLength = "Fragment length",
    fragmentLengthHint = "Byte range, for example 100-200",
    fragmentInterval = "Fragment interval",
    fragmentIntervalHint = "Pause between fragments in ms, for example 10-20",
    mux = "MUX",
    muxHint = "Connection multiplexing (VLESS/VMess/Trojan)",
    muxConcurrency = "Concurrent connections",
    muxConcurrencyHint = "TCP connections per mux channel",
    muxXudp = "Concurrent UDP",
    muxXudpHint = "UDP connections (XUDP)",
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
    logsSearch = "Search the log",
    logsFilterAll = "All",
    logsNoMatches = "No matching entries",
    logsJumpToEnd = "Jump to end of log",
    about = "About",
    aboutDescription = "Open-source VPN client on the xray core: VLESS, VMess, Trojan, Shadowsocks, SOCKS, and Hysteria2.",
    githubProject = "GitHub",
    githubProjectHint = "github.com/StrelaTech/onebox",
    telegramChannel = "Telegram",
    telegramChannelHint = "t.me/StrelaTech",
    licensesTitle = "Open-source licenses",
    licensesHint = "Project code and libraries",
    licensesDescription = """
        OneBox is open source (the project license lives in the repository).

        xray-core (github.com/xtls/xray-core) — MIT: the VPN core.
        quic-go (github.com/quic-go/quic-go) — MIT: HTTP/3 transport in Hysteria2.
        gomobile (github.com/golang/go / golang.org/x/mobile) — BSD-3: Go on Android.
        Jetpack Compose, AndroidX, Kotlin (Google, JetBrains) — Apache 2.0.
    """.trimIndent(),
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
    subscriptionSettings = "Subscriptions",
    subscriptionSettingsHint = "Updates, server sorting and User-Agent",
    userAgent = "User-Agent",
    userAgentHint = "Header for subscription server requests",
    serverSort = "Server sorting",
    serverSortHint = "Order of servers inside a subscription",
    sortSubscription = "As in subscription",
    sortPing = "By ping",
    sortName = "By name",
    updateInterval = "Update interval",
    updateIntervalHint = "Refresh URL subscriptions in the background",
    intervalNever = "Never",
    interval30m = "30 minutes",
    interval60m = "60 minutes",
    interval2h = "2 hours",
    interval6h = "6 hours",
    interval12h = "12 hours",
    interval24h = "24 hours",

    expiresLeft = { millis -> formatRemainingWith(millis, Locale.ENGLISH, EN_REMAINING_UNITS, "expired") },
    expiredShort = "expired",
    noExpiry = "No expiry",
    trafficUsed = { used, limit -> "Traffic: $used of $limit" },
    trafficUsedNoLimit = { used -> "Traffic: $used / ∞" },
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
    pingMethodGet = "HTTP GET",
    pingMethodHead = "HTTP HEAD",
    pingMethodTcp = "TCP",
    pingMethodIcmp = "ICMP",
    pingTimeout = "Timeout",
    pingTimeoutHint = "Maximum wait for a reply",
    pingSeconds = { seconds -> "$seconds s" },
    pingUrlTest = "URL test",
    pingUrlTestHint = "Address for GET/HEAD",
    pingUrlPresetGoogle = "Google · generate_204",
    pingUrlPresetCloudflare = "Cloudflare · generate_204",
    pingUrlPresetGithub = "GitHub · robots.txt",
    pingUrlOther = "Custom URL…",
    pingUrlLabel = "URL",
    pingUrlHint = "For GET/HEAD",

    defaultSubscriptionName = { number -> "Subscription $number" },
    close = "Close",
    vpnActiveNotification = "VPN is on",
    notificationConnectingTitle = "Connecting…",
    notificationRunning = "Connected",
    notificationServer = { name -> "Server: $name" },
    notificationPing = { ms -> "Ping: $ms" },
    notificationPingNa = "Ping: —",
    notificationModeTun = "TUN mode",
    notificationModeProxy = "Proxy mode",
    notificationModeTunProxy = "TUN + Proxy",
    notificationKillSwitch = "Kill Switch: internet blocked",
    vpnChannelName = "OneBox VPN"
)

fun stringsFor(language: AppLanguage): OneBoxStrings = when (AppLanguage.resolve(language)) {
    AppLanguage.AUTO -> EnStrings
    AppLanguage.RU -> RuStrings
    AppLanguage.EN -> EnStrings
    AppLanguage.FA -> FaStrings
    AppLanguage.ZH -> ZhStrings
}

/**
 * Остаток срока подписки: «Осталось 45 дн.» / "45 d left".
 * Истёкшая подписка — «истекла» / "expired". До двух суток считаем в часах,
 * до двух месяцев — в днях, дальше — в месяцах (вверх — неполный час/день
 * уже целый).
 */
internal fun formatRemainingWith(millis: Long, locale: Locale, units: Array<String>, expired: String): String {
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
        "fa" -> "باقی‌مانده $count $unit"
        "zh" -> "剩余 $count $unit"
        else -> "$count $unit left"
    }
}

internal const val MILLIS_PER_HOUR = 3_600_000L

internal val EN_REMAINING_UNITS = arrayOf("h", "d", "mo")

/**
 * Человекочитаемый размер трафика: 2048 → «2 КБ».
 * До килобайта байты не дробятся, выше — одна десятая.
 */
internal fun formatBytesWith(bytes: Long, locale: Locale, units: Array<String>): String {
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

internal val LOCALE_RU = Locale("ru")
internal val LOCALE_FA = Locale("fa")
internal val LOCALE_ZH = Locale.SIMPLIFIED_CHINESE
internal val RU_REMAINING_UNITS = arrayOf("ч.", "дн.", "мес.")
internal val FA_REMAINING_UNITS = arrayOf("ساعت", "روز", "ماه")
internal val ZH_REMAINING_UNITS = arrayOf("小时", "天", "个月")

internal val BYTES_UNITS_RU = arrayOf("Б", "КБ", "МБ", "ГБ", "ТБ")
internal val BYTES_UNITS_EN = arrayOf("B", "KB", "MB", "GB", "TB")
internal val BYTES_UNITS_FA = arrayOf("بایت", "کیلوبایت", "مگابایت", "گیگابایت", "ترابایت")
internal val BYTES_UNITS_ZH = arrayOf("字节", "KB", "MB", "GB", "TB")
