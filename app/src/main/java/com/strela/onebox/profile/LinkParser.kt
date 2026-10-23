package com.strela.onebox.profile

import android.net.Uri
import android.util.Base64
import java.util.UUID

/**
 * Разбор ссылок серверов в [Server].
 *
 * Поддерживаются схемы: vless://, vmess://, trojan://, ss://, socks://.
 * VMess бывает двух видов: base64-json (v2rayN) и стандартный vmess://<base64>.
 * Shadowsocks — как SIP002 (ss://base64(method:pass)@host:port), так и legacy.
 */
object LinkParser {

    class ParseException(message: String) : Exception(message)

    /** Разбирает одну ссылку. Бросает ParseException при неудаче. */
    fun parse(raw: String): Server {
        val url = raw.trim()
        if (url.isEmpty()) throw ParseException("пустая ссылка")

        val sepIndex = url.indexOf("://")
        if (sepIndex <= 0) throw ParseException("нет схемы (://)")
        val scheme = url.substring(0, sepIndex).lowercase()
        // нормализуем схему к нижнему регистру, сохраняя остальное как есть
        val normalized = scheme + url.substring(sepIndex)

        return when (scheme) {
            "vless" -> parseVless(normalized)
            "vmess" -> parseVmess(normalized)
            "trojan" -> parseTrojan(normalized)
            "ss" -> parseShadowsocks(normalized)
            "socks", "socks5" -> parseSocks(normalized)
            else -> throw ParseException("неизвестная схема: $scheme")
        }
    }

    /** Разбирает текст с несколькими ссылками (построчно), игнорируя пустые и битые. */
    fun parseMany(text: String): List<Server> =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.contains("://") }
            .mapNotNull { runCatching { parse(it) }.getOrNull() }
            .toList()

    // ── VLESS ──────────────────────────────────────────────────────────────
    // vless://uuid@host:port?type=ws&security=tls&sni=...&path=/x&host=...#name
    private fun parseVless(url: String): Server {
        val uri = Uri.parse(url)
        val uuid = uri.userInfo ?: throw ParseException("vless: нет UUID")
        val host = uri.host ?: throw ParseException("vless: нет адреса")
        val port = uri.port.takeIf { it > 0 } ?: throw ParseException("vless: нет порта")
        val q = uri.queryParameters()

        return Server(
            id = newId(),
            name = fragmentName(uri, host, port),
            protocol = Server.Protocol.VLESS,
            address = host,
            port = port,
            credential = uuid,
            extra = q["flow"].orEmpty(),
            network = q["type"] ?: "tcp",
            security = q["security"] ?: "none",
            sni = q["sni"].orEmpty(),
            alpn = q["alpn"].orEmpty(),
            fingerprint = q["fp"].orEmpty(),
            publicKey = q["pbk"].orEmpty(),
            shortId = q["sid"].orEmpty(),
            spiderX = q["spx"].orEmpty(),
            path = q["path"].orEmpty(),
            host = q["host"].orEmpty(),
            serviceName = q["serviceName"].orEmpty(),
            rawUrl = url
        )
    }

    // ── Trojan ─────────────────────────────────────────────────────────────
    // trojan://password@host:port?security=tls&sni=...&type=ws&path=...#name
    private fun parseTrojan(url: String): Server {
        val uri = Uri.parse(url)
        val password = uri.userInfo ?: throw ParseException("trojan: нет пароля")
        val host = uri.host ?: throw ParseException("trojan: нет адреса")
        val port = uri.port.takeIf { it > 0 } ?: throw ParseException("trojan: нет порта")
        val q = uri.queryParameters()

        return Server(
            id = newId(),
            name = fragmentName(uri, host, port),
            protocol = Server.Protocol.TROJAN,
            address = host,
            port = port,
            credential = password,
            network = q["type"] ?: "tcp",
            security = q["security"] ?: "tls",
            sni = q["sni"].orEmpty(),
            alpn = q["alpn"].orEmpty(),
            fingerprint = q["fp"].orEmpty(),
            path = q["path"].orEmpty(),
            host = q["host"].orEmpty(),
            serviceName = q["serviceName"].orEmpty(),
            rawUrl = url
        )
    }

    // ── VMess (base64-json) ────────────────────────────────────────────────
    private fun parseVmess(url: String): Server {
        val payload = url.removePrefix("vmess://").substringBefore("#")
        val json = runCatching { String(base64Decode(payload)) }
            .getOrElse { throw ParseException("vmess: не удалось декодировать base64") }

        val obj = runCatching { org.json.JSONObject(json) }
            .getOrElse { throw ParseException("vmess: некорректный JSON") }

        val host = obj.optString("add").ifEmpty { throw ParseException("vmess: нет адреса") }
        val port = obj.optString("port").toIntOrNull() ?: throw ParseException("vmess: нет порта")
        val id = obj.optString("id").ifEmpty { throw ParseException("vmess: нет UUID") }
        val net = obj.optString("net", "tcp").ifEmpty { "tcp" }
        val tls = obj.optString("tls", "").ifEmpty { "none" }

        // host из ws-заголовка → в поле host; path → path
        val wsHost = obj.optString("host", "")
        val sni = obj.optString("sni", "").ifEmpty { wsHost }

        return Server(
            id = newId(),
            name = obj.optString("ps", "").ifEmpty { "$host:$port" },
            protocol = Server.Protocol.VMESS,
            address = host,
            port = port,
            credential = id,
            extra = obj.optString("aid", "0"),
            network = net,
            security = if (tls == "tls") "tls" else "none",
            sni = sni,
            alpn = obj.optString("alpn", ""),
            fingerprint = obj.optString("fp", ""),
            path = obj.optString("path", ""),
            host = wsHost,
            serviceName = obj.optString("path", "").takeIf { net == "grpc" }.orEmpty(),
            rawUrl = url
        )
    }

    // ── Shadowsocks (SIP002 + legacy) ──────────────────────────────────────
    private fun parseShadowsocks(url: String): Server {
        val body = url.removePrefix("ss://")
        val withoutFragment = body.substringBefore("#")
        val fragment = body.substringAfter("#", "")

        // Разделяем query (?plugin=...) — пока не поддерживаем плагины
        val main = withoutFragment.substringBefore("?")

        val (method, password, host, port) = if (main.contains("@")) {
            // SIP002: base64(method:pass)@host:port  |  method:pass@host:port
            val atIndex = main.lastIndexOf('@')
            val userInfo = main.substring(0, atIndex)
            val hostPort = main.substring(atIndex + 1)

            val decodedUser = if (userInfo.contains(":")) {
                userInfo
            } else {
                runCatching { String(base64Decode(userInfo)) }
                    .getOrElse { throw ParseException("ss: не удалось декодировать userinfo") }
            }
            val sep = decodedUser.indexOf(':')
            if (sep < 0) throw ParseException("ss: нет метода шифрования")
            val m = decodedUser.substring(0, sep)
            val p = decodedUser.substring(sep + 1)

            val hp = parseHostPort(hostPort)
            Quad(m, p, hp.first, hp.second)
        } else {
            // legacy: ss://base64(method:pass@host:port)
            val decoded = runCatching { String(base64Decode(main)) }
                .getOrElse { throw ParseException("ss: некорректный формат") }
            val atIndex = decoded.lastIndexOf('@')
            if (atIndex < 0) throw ParseException("ss: нет разделителя @")
            val creds = decoded.substring(0, atIndex)
            val hostPort = decoded.substring(atIndex + 1)
            val sep = creds.indexOf(':')
            if (sep < 0) throw ParseException("ss: нет метода шифрования")
            val hp = parseHostPort(hostPort)
            Quad(creds.substring(0, sep), creds.substring(sep + 1), hp.first, hp.second)
        }

        val name = runCatching { Uri.decode(fragment) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "$host:$port"

        return Server(
            id = newId(),
            name = name,
            protocol = Server.Protocol.SHADOWSOCKS,
            address = host,
            port = port,
            credential = password,
            extra = method,
            rawUrl = url
        )
    }

    // ── SOCKS ──────────────────────────────────────────────────────────────
    // socks://user:pass@host:port#name
    private fun parseSocks(url: String): Server {
        val uri = Uri.parse(url)
        val host = uri.host ?: throw ParseException("socks: нет адреса")
        val port = uri.port.takeIf { it > 0 } ?: throw ParseException("socks: нет порта")
        val userInfo = uri.userInfo.orEmpty()

        return Server(
            id = newId(),
            name = fragmentName(uri, host, port),
            protocol = Server.Protocol.SOCKS,
            address = host,
            port = port,
            credential = userInfo.substringBefore(":"),
            extra = userInfo.substringAfter(":", ""),
            rawUrl = url
        )
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private data class Quad(val a: String, val b: String, val c: String, val d: Int)

    private fun parseHostPort(value: String): Pair<String, Int> {
        val v = value.trim().removeSuffix("/")
        // IPv6: [::1]:443
        if (v.startsWith("[")) {
            val end = v.indexOf(']')
            if (end < 0) throw ParseException("некорректный IPv6 адрес")
            val host = v.substring(1, end)
            val port = v.substring(end + 1).removePrefix(":").toIntOrNull()
                ?: throw ParseException("нет порта")
            return host to port
        }
        val sep = v.lastIndexOf(':')
        if (sep < 0) throw ParseException("нет порта")
        val host = v.substring(0, sep)
        val port = v.substring(sep + 1).toIntOrNull() ?: throw ParseException("некорректный порт")
        return host to port
    }

    private fun fragmentName(uri: Uri, host: String, port: Int): String {
        val frag = uri.fragment
        return if (!frag.isNullOrBlank()) Uri.decode(frag) else "$host:$port"
    }

    private fun base64Decode(input: String): ByteArray {
        val normalized = input
            .replace('-', '+')
            .replace('_', '/')
            .trim()
        return try {
            Base64.decode(normalized, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            // добираем padding, если его нет
            val padded = normalized + "=".repeat((4 - normalized.length % 4) % 4)
            Base64.decode(padded, Base64.DEFAULT)
        }
    }

    private fun newId(): String = UUID.randomUUID().toString()

    private fun Uri.queryParameters(): Map<String, String> =
        queryParameterNames.associateWith { getQueryParameter(it).orEmpty() }
}
