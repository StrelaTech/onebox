package com.strela.onebox.profile

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
 * Хранилище профилей: список групп серверов и выбранный профиль в SharedPreferences (JSON).
 * Профили небольшие, поэтому храним целиком, без базы.
 */
object ProfileStore {

    private const val PREFS = "profiles"
    private const val KEY_LIST = "list"
    private const val KEY_SELECTED = "selected"

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _selectedProfileId = MutableStateFlow<String?>(null)
    val selectedProfileId: StateFlow<String?> = _selectedProfileId.asStateFlow()

    /** Инициализация: читает список из prefs. Идемпотентна. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_LIST, null)
        val list = raw?.let { decodeList(it) } ?: emptyList()
        _profiles.value = list
        val stored = prefs.getString(KEY_SELECTED, null)
        _selectedProfileId.value = when {
            // ключа нет — первый запуск, выбираем первый профиль
            stored == null -> list.firstOrNull()?.id
            // пустая строка — пользователь явно выбрал JSON-конфиг
            stored.isBlank() -> null
            else -> stored.takeIf { id -> list.any { it.id == id } } ?: list.firstOrNull()?.id
        }
        AppLog.i(
            "ProfileStore: загружено профилей=${list.size}, " +
                "серверов=${list.sumOf { it.servers.size }}, выбран=${_selectedProfileId.value}"
        )
        // Нормализуем хранилище (в т.ч. старый формат «профиль = сервер») сразу,
        // чтобы id профилей/серверов оставались стабильными между запусками.
        persist(context)
    }

    /**
     * Создаёт новый профиль из набора серверов (например, из вставленных ссылок).
     * Профиль сразу становится выбранным, активным сервером — первый в списке.
     */
    fun import(context: Context, servers: List<Server>, name: String? = null): Profile? {
        if (servers.isEmpty()) return null

        val unique = mutableListOf<Server>()
        for (server in servers) {
            if (unique.none { it.sameServerAs(server) }) unique.add(server)
        }
        if (unique.isEmpty()) return null

        val profile = Profile(
            id = newId(),
            name = name?.trim()?.takeIf { it.isNotEmpty() } ?: defaultProfileName(),
            servers = unique,
            selectedServerId = unique.first().id
        )

        _profiles.value = _profiles.value + profile
        _selectedProfileId.value = profile.id
        persist(context)
        AppLog.i("ProfileStore: импортирован профиль ${profile.name}, серверов=${unique.size}")
        return profile
    }

    fun removeProfile(context: Context, id: String) {
        val current = _profiles.value.filterNot { it.id == id }
        _profiles.value = current
        if (_selectedProfileId.value == id) _selectedProfileId.value = current.firstOrNull()?.id
        persist(context)
    }

    fun removeServer(context: Context, profileId: String, serverId: String) {
        val current = _profiles.value.mapNotNull { profile ->
            if (profile.id != profileId) return@mapNotNull profile

            val servers = profile.servers.filterNot { it.id == serverId }
            if (servers.isEmpty()) return@mapNotNull null

            val selectedServerId = profile.selectedServerId
                ?.takeIf { id -> servers.any { it.id == id } }
                ?: servers.first().id
            profile.copy(servers = servers, selectedServerId = selectedServerId)
        }
        _profiles.value = current
        if (current.none { it.id == _selectedProfileId.value }) {
            _selectedProfileId.value = current.firstOrNull()?.id
        }
        persist(context)
    }

    fun renameProfile(context: Context, id: String, name: String) {
        _profiles.value = _profiles.value.map {
            if (it.id == id) it.copy(name = name) else it
        }
        persist(context)
    }

    fun selectProfile(context: Context, id: String) {
        if (_profiles.value.none { it.id == id }) return
        _selectedProfileId.value = id
        persist(context)
        AppLog.i("ProfileStore: выбран профиль $id")
    }

    /** Снимает выбор профиля — подключение пойдёт по сохранённому JSON-конфигу. */
    fun clearSelection(context: Context) {
        _selectedProfileId.value = null
        persist(context)
        AppLog.i("ProfileStore: выбор профиля снят (используется JSON-конфиг)")
    }

    /** Выбирает сервер внутри профиля; профиль при этом становится активным. */
    fun selectServer(context: Context, profileId: String, serverId: String) {
        val profile = _profiles.value.firstOrNull { it.id == profileId } ?: return
        if (profile.servers.none { it.id == serverId }) return

        _profiles.value = _profiles.value.map {
            if (it.id == profileId) it.copy(selectedServerId = serverId) else it
        }
        _selectedProfileId.value = profileId
        persist(context)
        AppLog.i("ProfileStore: выбран сервер $serverId в профиле $profileId")
    }

    fun selectedProfile(): Profile? =
        _selectedProfileId.value?.let { id -> _profiles.value.firstOrNull { it.id == id } }

    fun selectedServer(): Server? = selectedProfile()?.selectedServer

    private fun defaultProfileName(): String =
        SettingsStore.strings().defaultProfileName(_profiles.value.size + 1)

    private fun persist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LIST, encodeList(_profiles.value))
            // пустая строка = «выбран JSON-конфиг», null снова включил бы первый профиль
            .putString(KEY_SELECTED, _selectedProfileId.value.orEmpty())
            .apply()
    }

    // ── сериализация ───────────────────────────────────────────────────────

    private fun newId(): String = UUID.randomUUID().toString()

    private fun encodeList(list: List<Profile>): String {
        val arr = JSONArray()
        for (p in list) arr.put(encodeProfile(p))
        return arr.toString()
    }

    private fun decodeList(raw: String): List<Profile> = try {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { i ->
            runCatching { decodeProfile(arr.getJSONObject(i)) }.getOrNull()
        }
    } catch (e: Exception) {
        AppLog.e("ProfileStore: не удалось прочитать список профилей", e)
        emptyList()
    }

    private fun encodeProfile(p: Profile): JSONObject = JSONObject().apply {
        put("id", p.id)
        put("name", p.name)
        put("selectedServerId", p.selectedServerId.orEmpty())
        put("servers", JSONArray().apply {
            p.servers.forEach { put(encodeServer(it)) }
        })
    }

    private fun decodeProfile(o: JSONObject): Profile {
        // Старый формат: профиль и был одним сервером (поля address/port на верхнем уровне).
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

        return Profile(
            id = o.optString("id").ifEmpty { newId() },
            name = o.optString("name"),
            servers = servers,
            selectedServerId = selectedServerId
        )
    }

    /** Превращает запись старого формата (один сервер = профиль) в группу из одного сервера. */
    private fun migrateLegacy(o: JSONObject): Profile {
        val server = decodeServer(o)
        return Profile(
            id = newId(),
            name = o.optString("name").ifEmpty { server.name },
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
        rawUrl = o.optString("rawUrl")
    )
}

/** Один и тот же сервер: совпадают протокол, адрес, порт и учётные данные. */
private fun Server.sameServerAs(other: Server): Boolean =
    protocol == other.protocol &&
        address == other.address &&
        port == other.port &&
        credential == other.credential
