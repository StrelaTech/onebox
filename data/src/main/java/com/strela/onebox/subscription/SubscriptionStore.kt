package com.strela.onebox.subscription

import android.content.Context
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Хранилище подписок: список подписок с серверами и выбранная подписка
 * в SharedPreferences (JSON). Подписки небольшие, поэтому храним целиком, без базы.
 */
object SubscriptionStore {

    private const val PREFS = "profiles"
    private const val KEY_LIST = "list"
    private const val KEY_SELECTED = "selected"

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    private val _selectedSubscriptionId = MutableStateFlow<String?>(null)
    val selectedSubscriptionId: StateFlow<String?> = _selectedSubscriptionId.asStateFlow()

    /** Инициализация: читает список из prefs. Идемпотентна. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_LIST, null)
        val list = raw?.let { decodeList(it) } ?: emptyList()
        _subscriptions.value = list
        val stored = prefs.getString(KEY_SELECTED, null)
        _selectedSubscriptionId.value = when {
            // ключа нет — первый запуск, выбираем первую подписку
            stored == null -> list.firstOrNull()?.id
            // пустая строка — пользователь явно выбрал JSON-конфиг
            stored.isBlank() -> null
            else -> stored.takeIf { id -> list.any { it.id == id } } ?: list.firstOrNull()?.id
        }
        AppLog.i(
            "SubscriptionStore: загружено подписок=${list.size}, " +
                "серверов=${list.sumOf { it.servers.size }}, выбрана=${_selectedSubscriptionId.value}"
        )
        // Нормализуем хранилище (в т.ч. старый формат «профиль = сервер» и
        // «профиль с вложенными метаданными подписки») сразу,
        // чтобы id подписок/серверов оставались стабильными между запусками.
        persist(context)
    }

    /**
     * Добавляет серверы ручного импорта (ссылки, base64, JSON-объекты серверов)
     * в ОДНУ постоянную подписку [Subscription.SERVERS_ID] «Серверы»:
     * повторные действия не плодят новые подписки, серверы лишь дополняются
     * (без дубликатов). У этой подписки только заголовок — без описания,
     * трафика и срока.
     *
     * Возвращает подписку или null, если новых серверов не оказалось.
     * Подписка сразу становится выбранной.
     */
    fun importServers(context: Context, servers: List<Server>): Subscription? {
        if (servers.isEmpty()) return null

        val existing = _subscriptions.value.firstOrNull { it.id == Subscription.SERVERS_ID }
        val merged = (existing?.servers.orEmpty() + servers)
            .distinctByServer()

        val added = merged.size - (existing?.servers?.size ?: 0)
        if (added <= 0) return null

        val subscription = Subscription(
            id = Subscription.SERVERS_ID,
            // Заголовок фиксированный: у подписки «Серверы» нет ничего, кроме него
            title = existing?.title?.takeIf { it.isNotBlank() }
                ?: SettingsStore.strings().serversSubscriptionTitle,
            servers = merged,
            selectedServerId = merged.firstOrNull { it.id == existing?.selectedServerId }?.id
                ?: merged.first().id,
            json = existing?.json
        )

        _subscriptions.value = if (existing == null) {
            _subscriptions.value + subscription
        } else {
            _subscriptions.value.map { if (it.id == Subscription.SERVERS_ID) subscription else it }
        }
        _selectedSubscriptionId.value = subscription.id
        persist(context)
        AppLog.i(
            "SubscriptionStore: ручной импорт в подписку «${subscription.title}», " +
                "добавлено=$added, всего=${merged.size}"
        )
        return subscription
    }

    /** Находит подписку по нормализованному URL источника (ключ дедупликации). */
    fun findBySubscriptionUrl(url: String): Subscription? =
        _subscriptions.value.firstOrNull { it.url == url }

    /**
     * Создаёт или обновляет подписку: если подписка с таким id уже есть
     * (тот же URL), серверы заменяются свежими, метаданные обновляются.
     * Выбранный сервер сохраняется: по ссылке (rawUrl), а у JSON-серверов —
     * по снапшоту конфига.
     */
    fun upsertSubscription(context: Context, subscription: Subscription): Subscription {
        requireNotNull(subscription.url) { "upsertSubscription требует url источника" }

        val existing = _subscriptions.value.firstOrNull { it.id == subscription.id }
        val selectedRawUrl = existing?.selectedServer?.rawUrl
        val selectedJson = existing?.selectedServer?.json
        val servers = subscription.servers.distinctByServer()
        val resolved = subscription.copy(
            servers = servers,
            // выбранный сервер сохраняется по его ссылке, а у JSON-серверов —
            // по их снапшоту конфига (rawUrl у них пустой)
            selectedServerId = servers.firstOrNull { it.rawUrl == selectedRawUrl && selectedRawUrl.isNotEmpty() }?.id
                ?: servers.firstOrNull { it.json == selectedJson && selectedJson?.isNotBlank() == true }?.id
                ?: servers.firstOrNull()?.id
        )

        _subscriptions.value = if (existing == null) {
            _subscriptions.value + resolved
        } else {
            _subscriptions.value.map { if (it.id == resolved.id) resolved else it }
        }
        persist(context)
        AppLog.i(
            "SubscriptionStore: подписка «${resolved.title}» " +
                "${if (existing == null) "создана" else "обновлена"}, серверов=${servers.size}"
        )
        return resolved
    }

    /**
     * Создаёт подписку из вставленных конфигов xray-core: каждый конфиг
     * раскладывается на сервер (с сохранением исходного JSON в Server.json),
     * все они попадают в ОДНУ новую подписку. Заголовок: [fallbackTitle],
     * иначе remarks первого конфига, иначе «Подписка N».
     * Подписка сразу становится выбранной. Возвращает единственную созданную
     * подписку или пустой список, если конфиги не разобрались.
     */
    fun importJsons(
        context: Context,
        configs: List<JsonConfig>,
        fallbackTitle: String? = null,
    ): List<Subscription> {
        val servers = configs
            .filter { it.json.isNotBlank() }
            .mapNotNull { cfg -> XrayConfigParser.parse(cfg.json, cfg.name) }
            .distinctByServer()
        if (servers.isEmpty()) return emptyList()

        val explicit = fallbackTitle?.trim()?.takeIf { it.isNotEmpty() }
        val firstRemarks = configs.firstOrNull { !it.name.isNullOrBlank() }?.name
        val created = Subscription(
            id = newId(),
            title = explicit
                ?: firstRemarks?.takeIf { it.isNotBlank() }
                ?: defaultSubscriptionName(_subscriptions.value.size + 1),
            servers = servers,
            selectedServerId = servers.first().id,
        )

        _subscriptions.value = _subscriptions.value + created
        _selectedSubscriptionId.value = created.id
        persist(context)
        AppLog.i(
            "SubscriptionStore: импортирована JSON-подписка «${created.title}», " +
                "серверов=${servers.size}"
        )
        return listOf(created)
    }

    /** Создаёт JSON-подписку из одного вставленного конфига. */
    fun importJson(context: Context, json: String, title: String? = null): Subscription? =
        importJsons(context, listOf(JsonConfig(json)), title).firstOrNull()

    /**
     * Создаёт отдельную подписку из готовых серверов (например, из списка
     * ссылок при импорте «подписки» без URL). Без дедупликации с уже
     * существующими группами: каждое действие — новая подписка.
     */
    fun importServersAsSubscription(
        context: Context,
        servers: List<Server>,
        title: String? = null,
    ): Subscription? {
        val merged = servers.distinctByServer()
        if (merged.isEmpty()) return null

        val created = Subscription(
            id = newId(),
            title = title?.trim()?.takeIf { it.isNotEmpty() }
                ?: defaultSubscriptionName(_subscriptions.value.size + 1),
            servers = merged,
            selectedServerId = merged.first().id,
        )
        _subscriptions.value = _subscriptions.value + created
        _selectedSubscriptionId.value = created.id
        persist(context)
        AppLog.i(
            "SubscriptionStore: создана подписка «${created.title}» " +
                "из ${merged.size} серверов вручную"
        )
        return created
    }

    fun removeSubscription(context: Context, id: String) {
        val current = _subscriptions.value.filterNot { it.id == id }
        _subscriptions.value = current
        if (_selectedSubscriptionId.value == id) _selectedSubscriptionId.value = current.firstOrNull()?.id
        persist(context)
    }

    fun removeServer(context: Context, subscriptionId: String, serverId: String) {
        val current = _subscriptions.value.mapNotNull { subscription ->
            if (subscription.id != subscriptionId) return@mapNotNull subscription

            val servers = subscription.servers.filterNot { it.id == serverId }
            if (servers.isEmpty()) return@mapNotNull null

            val selectedServerId = subscription.selectedServerId
                ?.takeIf { id -> servers.any { it.id == id } }
                ?: servers.first().id
            subscription.copy(servers = servers, selectedServerId = selectedServerId)
        }
        _subscriptions.value = current
        if (current.none { it.id == _selectedSubscriptionId.value }) {
            _selectedSubscriptionId.value = current.firstOrNull()?.id
        }
        persist(context)
    }

    fun selectSubscription(context: Context, id: String) {
        if (_subscriptions.value.none { it.id == id }) return
        _selectedSubscriptionId.value = id
        persist(context)
        AppLog.i("SubscriptionStore: выбрана подписка $id")
    }

    /** Снимает выбор подписки — подключение пойдёт по сохранённому JSON-конфигу. */
    fun clearSelection(context: Context) {
        _selectedSubscriptionId.value = null
        persist(context)
        AppLog.i("SubscriptionStore: выбор подписки снят (используется JSON-конфиг)")
    }

    /** Выбирает сервер внутри подписки; подписка при этом становится активной. */
    fun selectServer(context: Context, subscriptionId: String, serverId: String) {
        val subscription = _subscriptions.value.firstOrNull { it.id == subscriptionId } ?: return
        if (subscription.servers.none { it.id == serverId }) return

        _subscriptions.value = _subscriptions.value.map {
            if (it.id == subscriptionId) it.copy(selectedServerId = serverId) else it
        }
        _selectedSubscriptionId.value = subscriptionId
        persist(context)
        AppLog.i("SubscriptionStore: выбран сервер $serverId в подписке $subscriptionId")
    }

    fun selectedSubscription(): Subscription? =
        _selectedSubscriptionId.value?.let { id -> _subscriptions.value.firstOrNull { it.id == id } }

    fun selectedServer(): Server? = selectedSubscription()?.selectedServer

    private fun defaultSubscriptionName(number: Int): String =
        SettingsStore.strings().defaultSubscriptionName(number)

    private fun persist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LIST, encodeList(_subscriptions.value))
            // пустая строка = «выбран JSON-конфиг», null снова включил бы первую подписку
            .putString(KEY_SELECTED, _selectedSubscriptionId.value.orEmpty())
            .apply()
    }

    // ── сериализация ───────────────────────────────────────────────────────

    private fun newId(): String = UUID.randomUUID().toString()

    private fun encodeList(list: List<Subscription>): String {
        val arr = JSONArray()
        for (s in list) arr.put(encodeSubscription(s))
        return arr.toString()
    }

    private fun decodeList(raw: String): List<Subscription> = try {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { i ->
            runCatching { decodeSubscription(arr.getJSONObject(i)) }.getOrNull()
        }
    } catch (e: Exception) {
        AppLog.e("SubscriptionStore: не удалось прочитать список подписок", e)
        emptyList()
    }

    private fun encodeSubscription(s: Subscription): JSONObject = JSONObject().apply {
        put("id", s.id)
        put("title", s.title)
        put("description", s.description)
        put("usedBytes", s.usedBytes ?: -1L)
        put("trafficLimitBytes", s.trafficLimitBytes ?: -1L)
        put("expireAtMillis", s.expireAtMillis ?: -1L)
        put("url", s.url.orEmpty())
        put("lastUpdateMillis", s.lastUpdateMillis)
        put("selectedServerId", s.selectedServerId.orEmpty())
        put("json", s.json.orEmpty())
        put("servers", JSONArray().apply {
            s.servers.forEach { put(encodeServer(it)) }
        })
    }

    /**
     * Читает подписку в новом (плоском) формате или в старом формате профилей:
     * там поля назывались `name`, а метаданные подписки лежали вложенным объектом
     * `subscription` — их раскладываем по плоским полям при миграции.
     */
    private fun decodeSubscription(o: JSONObject): Subscription {
        // Совсем старый формат: запись и была одним сервером (address/port наверху).
        if (!o.has("servers") && o.has("address")) return migrateLegacy(o)

        val servers = o.optJSONArray("servers")
            ?.let { arr ->
                (0 until arr.length()).mapNotNull { i ->
                    runCatching { decodeServer(arr.getJSONObject(i)) }.getOrNull()
                }
            }
            .orEmpty()

        val selectedServerId = o.optString("selectedServerId")
            .takeIf { id -> servers.any { it.id == id } }
            ?: servers.firstOrNull()?.id

        val legacy = o.optJSONObject("subscription")
        val url = o.optString("url").takeIf { it.isNotBlank() }
            ?: legacy?.optString("url")?.takeIf { it.isNotBlank() }

        val usage = o.optLong("usedBytes", -1L).takeIf { it >= 0 }
            ?: legacy
                ?.let { meta ->
                    val up = meta.optLong("uploadBytes", -1L).takeIf { it >= 0 } ?: 0L
                    val down = meta.optLong("downloadBytes", -1L).takeIf { it >= 0 } ?: 0L
                    if (up + down > 0) up + down else null
                }

        return Subscription(
            id = o.optString("id").ifEmpty { newId() },
            title = o.optString("title").ifBlank { o.optString("name").ifBlank { url ?: "" } },
            // Описания нет — оставляем пустым; Announce → description, иначе — пусто.
            description = o.optString("description").ifBlank { "" },
            usedBytes = usage,
            trafficLimitBytes = o.optLong("trafficLimitBytes", -1L).takeIf { it >= 0 }
                ?: legacy?.optLong("totalBytes", -1L)?.takeIf { it >= 0 },
            expireAtMillis = o.optLong("expireAtMillis", -1L).takeIf { it >= 0 }
                ?: legacy?.optLong("expireAtMillis", -1L)?.takeIf { it >= 0 },
            servers = servers,
            selectedServerId = selectedServerId,
            json = o.optString("json").takeIf { it.isNotBlank() },
            url = url,
            lastUpdateMillis = o.optLong("lastUpdateMillis", 0L).takeIf { it > 0 }
                ?: legacy?.optLong("lastUpdateMillis", 0L)?.takeIf { it > 0 }
                ?: 0L
        )
    }

    /** Превращает запись старого формата (один сервер = профиль) в подписку с одним сервером. */
    private fun migrateLegacy(o: JSONObject): Subscription {
        val server = decodeServer(o)
        return Subscription(
            id = newId(),
            title = o.optString("name").ifBlank { server.name },
            servers = listOf(server),
            selectedServerId = server.id
        )
    }

    private fun encodeServer(s: Server): JSONObject = JSONObject().apply {
        put("id", s.id)
        put("name", s.name)
        put("protocol", s.protocol.name)
        put("address", s.address)
        put("port", s.port)
        put("credential", s.credential)
        put("extra", s.extra)
        put("network", s.network)
        put("security", s.security)
        put("sni", s.sni)
        put("alpn", s.alpn)
        put("fingerprint", s.fingerprint)
        put("publicKey", s.publicKey)
        put("shortId", s.shortId)
        put("spiderX", s.spiderX)
        put("path", s.path)
        put("host", s.host)
        put("serviceName", s.serviceName)
        put("rawUrl", s.rawUrl)
        put("json", s.json)
    }

    private fun decodeServer(o: JSONObject): Server = Server(
        id = o.optString("id").ifEmpty { newId() },
        name = o.optString("name").ifEmpty { o.optString("address") },
        protocol = runCatching { Server.Protocol.valueOf(o.getString("protocol")) }
            .getOrDefault(Server.Protocol.UNKNOWN),
        address = o.getString("address"),
        port = o.getInt("port"),
        credential = o.optString("credential"),
        extra = o.optString("extra"),
        network = o.optString("network", "tcp"),
        security = o.optString("security", "none"),
        sni = o.optString("sni"),
        alpn = o.optString("alpn"),
        fingerprint = o.optString("fingerprint"),
        publicKey = o.optString("publicKey"),
        shortId = o.optString("shortId"),
        spiderX = o.optString("spiderX"),
        path = o.optString("path"),
        host = o.optString("host"),
        serviceName = o.optString("serviceName"),
        rawUrl = o.optString("rawUrl"),
        json = o.optString("json")
    )
}

/** Один и тот же сервер: совпадают протокол, адрес, порт и учётные данные. */
private fun Server.sameServerAs(other: Server): Boolean =
    protocol == other.protocol &&
        address == other.address &&
        port == other.port &&
        credential == other.credential

/** Убирает дубликаты серверов, сохраняя порядок и первое вхождение. */
private fun List<Server>.distinctByServer(): List<Server> {
    val unique = mutableListOf<Server>()
    for (server in this) {
        if (unique.none { it.sameServerAs(server) }) unique.add(server)
    }
    return unique
}
