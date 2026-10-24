package com.strela.onebox.subscription

import org.json.JSONObject
import java.util.UUID

/**
 * Разбор объекта сервера в формате приложения — том же, в котором серверы
 * хранит SubscriptionStore: поля address/port/protocol лежат на верхнем уровне.
 */
object ServerJsonParser {

    /**
     * Возвращает сервер из JSON-объекта либо null, если объект не похож
     * на сервер: нет адреса, порта или он вне диапазона.
     */
    fun parse(obj: JSONObject): Server? {
        val address = obj.optString("address").trim()
        if (address.isEmpty()) return null
        val port = obj.optString("port").trim().toIntOrNull() ?: return null
        if (port !in 1..65535) return null

        return Server(
            id = obj.optString("id").trim().ifEmpty { newId() },
            name = obj.optString("name").trim().ifEmpty { "$address:$port" },
            protocol = protocol(obj.optString("protocol")),
            address = address,
            port = port,
            credential = obj.optString("credential"),
            extra = obj.optString("extra"),
            network = obj.optString("network").ifEmpty { "tcp" },
            security = obj.optString("security").ifEmpty { "none" },
            sni = obj.optString("sni"),
            alpn = obj.optString("alpn"),
            fingerprint = obj.optString("fingerprint"),
            publicKey = obj.optString("publicKey"),
            shortId = obj.optString("shortId"),
            spiderX = obj.optString("spiderX"),
            path = obj.optString("path"),
            host = obj.optString("host"),
            serviceName = obj.optString("serviceName"),
            rawUrl = obj.optString("rawUrl"),
        )
    }

    /** Протокол по имени; короткие имена (ss, socks5) и неизвестные → UNKNOWN. */
    private fun protocol(raw: String): Server.Protocol {
        val name = raw.trim().uppercase()
        val alias = when (name) {
            "SS" -> "SHADOWSOCKS"
            "SOCKS5" -> "SOCKS"
            else -> name
        }
        return Server.Protocol.entries.firstOrNull { it.name == alias }
            ?: Server.Protocol.UNKNOWN
    }

    private fun newId(): String = UUID.randomUUID().toString()
}
