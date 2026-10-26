package com.strela.onebox.subscription

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Разбор готового xray-конфига в [Server].
 *
 * JSON-конфиги — второй формат подписок наравне с URI: один конфиг описывает
 * один сервер (первый outbound с проксирующим протоколом), поэтому конфиг
 * раскладывается на сервер и кладётся в подписку как обычный элемент списка.
 * Исходный конфиг при этом сохраняется в [Server.json] как есть — при сборке
 * конфига ядра он отдаётся обратно без изменений (см. [ConfigBuilder]).
 *
 * Поддерживаемые outbound-протоколы: vless, vmess, trojan, shadowsocks, socks.
 * streamSettings читается целиком (ws/grpc/httpupgrade/splithttp/tcp-обфускация,
 * tls/reality); dns, routing и inbounds остаются только внутри снапшота.
 */
object XrayConfigParser {

    /** Проксирующие протоколы outbound'ов, которые приложение умеет подключать. */
    private val PROXY_PROTOCOLS = setOf("vless", "vmess", "trojan", "shadowsocks", "socks")

    /**
     * Сервер из JSON-конфига xray; [name] — название конфига (remarks).
     * Возвращает null, если это не конфиг с подключаемым outbound'ом.
     */
    fun parse(json: String, name: String? = null): Server? {
        val obj = runCatching { JSONObject(json) }.getOrNull() ?: return null
        val proxy = obj.optJSONArray("outbounds")?.let(::proxyOutbound) ?: return null

        val endpoint = endpointOf(proxy) ?: return null
        val stream = proxy.optJSONObject("streamSettings").orEmpty()

        return Server(
            id = UUID.randomUUID().toString(),
            name = name?.trim()?.takeIf(String::isNotEmpty) ?: "${endpoint.address}:${endpoint.port}",
            protocol = protocol(proxy.optString("protocol")),
            address = endpoint.address,
            port = endpoint.port,
            credential = endpoint.credential,
            extra = endpoint.extra,
            network = stream.optString("network").takeIf(String::isNotEmpty) ?: "tcp",
            security = stream.optString("security").takeIf(String::isNotEmpty) ?: "none",
            sni = sniOf(stream),
            alpn = tlsAlpn(stream.optJSONObject("tlsSettings")),
            fingerprint = fingerprintOf(stream),
            publicKey = stream.optJSONObject("realitySettings")?.optString("publicKey").orEmpty(),
            shortId = stream.optJSONObject("realitySettings")?.optString("shortId").orEmpty(),
            spiderX = stream.optJSONObject("realitySettings")?.optString("spiderX").orEmpty(),
            path = pathOf(stream),
            host = hostOf(stream),
            serviceName = stream.optJSONObject("grpcSettings")?.optString("serviceName").orEmpty(),
            rawUrl = "",
            json = json,
        )
    }

    // ── outbound и endpoint ────────────────────────────────────────────────

    /** Первый outbound с проксирующим протоколом: служебные (freedom, blackhole) пропускаются. */
    private fun proxyOutbound(outbounds: JSONArray): JSONObject? {
        for (i in 0 until outbounds.length()) {
            val outbound = outbounds.optJSONObject(i) ?: continue
            if (outbound.optString("protocol").lowercase() in PROXY_PROTOCOLS) return outbound
        }
        return null
    }

    /** destination и учётные данные конкретного протокола. */
    private data class Endpoint(
        val address: String,
        val port: Int,
        val credential: String,
        val extra: String,
    )

    private fun endpointOf(proxy: JSONObject): Endpoint? {
        val settings = proxy.optJSONObject("settings").orEmpty()
        val protocol = proxy.optString("protocol").lowercase()

        val endpoint = when (protocol) {
            "vless", "vmess" -> settings.optJSONArray("vnext")?.optJSONObject(0)?.let { destination ->
                val user = destination.optJSONArray("users")?.optJSONObject(0).orEmpty()
                Endpoint(
                    address = destination.optString("address"),
                    port = destination.optInt("port", -1),
                    credential = user.optString("id"), // uuid у обоих протоколов
                    // vless — flow (например, xtls-rprx-vision); vmess — alterId
                    extra = when (protocol) {
                        "vless" -> user.optString("flow")
                        else -> user.optInt("alterId", 0).takeIf { it > 0 }?.toString().orEmpty()
                    },
                )
            }

            "trojan", "shadowsocks" -> settings.optJSONArray("servers")?.optJSONObject(0)?.let { server ->
                Endpoint(
                    address = server.optString("address"),
                    port = server.optInt("port", -1),
                    credential = server.optString("password"),
                    // shadowsocks — метод шифрования, trojan — без доп. полей
                    extra = if (protocol == "shadowsocks") server.optString("method") else "",
                )
            }

            "socks" -> settings.optJSONArray("servers")?.optJSONObject(0)?.let { server ->
                val user = server.optJSONArray("users")?.optJSONObject(0).orEmpty()
                Endpoint(
                    address = server.optString("address"),
                    port = server.optInt("port", -1),
                    credential = user.optString("user"),
                    extra = user.optString("pass"),
                )
            }

            else -> null
        } ?: return null

        // адрес и порт обязательны для подключения
        val address = endpoint.address.trim()
        if (address.isEmpty() || endpoint.port !in 1..65535) return null
        return endpoint.copy(address = address)
    }

    private fun protocol(raw: String): Server.Protocol =
        when (raw.trim().uppercase()) {
            "VLESS" -> Server.Protocol.VLESS
            "VMESS" -> Server.Protocol.VMESS
            "TROJAN" -> Server.Protocol.TROJAN
            "SHADOWSOCKS" -> Server.Protocol.SHADOWSOCKS
            "SOCKS" -> Server.Protocol.SOCKS
            else -> Server.Protocol.UNKNOWN
        }

    // ── streamSettings ─────────────────────────────────────────────────────

    /** SNI: realitySettings.serverName или tlsSettings.serverName. */
    private fun sniOf(stream: JSONObject): String {
        stream.optJSONObject("realitySettings")?.optString("serverName")
            ?.takeIf(String::isNotEmpty)
            ?.let { return it }
        return stream.optJSONObject("tlsSettings")?.optString("serverName").orEmpty()
    }

    /** Отпечаток TLS-клиента: в reality он лежит в realitySettings, в обычном tls — в tlsSettings. */
    private fun fingerprintOf(stream: JSONObject): String {
        stream.optJSONObject("tlsSettings")?.optString("fingerprint")
            ?.takeIf(String::isNotEmpty)
            ?.let { return it }
        return stream.optJSONObject("realitySettings")?.optString("fingerprint").orEmpty()
    }

    private fun tlsAlpn(tls: JSONObject?): String {
        if (tls == null) return ""
        val list = tls.optJSONArray("alpn")
            ?.let { arr -> (0 until arr.length()).mapNotNull { arr.optString(it).takeIf(String::isNotEmpty) } }
        return list?.joinToString(", ") ?: tls.optString("alpn").orEmpty()
    }

    private fun pathOf(stream: JSONObject): String {
        stream.optJSONObject("wsSettings")?.optString("path")?.takeIf(String::isNotEmpty)?.let { return it }
        stream.optJSONObject("httpupgradeSettings")?.optString("path")?.takeIf(String::isNotEmpty)?.let { return it }
        return stream.optJSONObject("splithttpSettings")?.optString("path").orEmpty()
    }

    /**
     * Хост для обфускации: ws/httpupgrade/splithttp хранят его в поле "host"
     * (или в headers.Host — deprecated-вариант), tcp — в header.request.headers.Host
     * (http-маскировка).
     */
    private fun hostOf(stream: JSONObject): String {
        val direct = when (stream.optString("network")) {
            "ws" -> wsHost(stream.optJSONObject("wsSettings"))
            "httpupgrade" -> stream.optJSONObject("httpupgradeSettings")?.optString("host").orEmpty()
            "splithttp", "xhttp" -> stream.optJSONObject("splithttpSettings")?.optString("host").orEmpty()
            else -> ""
        }
        if (direct.isNotEmpty()) return direct

        return stream.optJSONObject("tcpSettings")
            ?.optJSONObject("header")
            ?.optJSONObject("request")
            ?.optJSONObject("headers")
            ?.optJSONArray("Host")
            ?.optString(0)
            .orEmpty()
    }

    private fun wsHost(ws: JSONObject?): String {
        if (ws == null) return ""
        ws.optString("host").takeIf(String::isNotEmpty)?.let { return it }
        val headers = ws.optJSONObject("headers") ?: return ""
        headers.optJSONArray("Host")?.optString(0)?.takeIf(String::isNotEmpty)?.let { return it }
        return headers.optString("Host").orEmpty()
    }
}

/** У JSONObject нет значений по умолчанию для opt-возвратов — обёртка для читаемости. */
private fun JSONObject?.orEmpty(): JSONObject = this ?: JSONObject()

private fun JSONArray?.orEmpty(): JSONArray = this ?: JSONArray()
