package com.strela.onebox.subscription

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

/** Что удалось распознать во вставленном тексте. */
sealed interface ImportContent {
    /** Одна или несколько ссылок серверов, либо объект(ы) сервера из JSON. */
    data class Servers(val servers: List<Server>) : ImportContent

    /**
     * JSON-конфиги xray-core: один объект либо массив объектов — тогда каждый
     * элемент импортируется отдельно. [configs] никогда не пуст.
     */
    data class Config(val configs: List<JsonConfig>) : ImportContent
}

/** Отдельный JSON-конфиг и его название — поле remarks из самого конфига. */
data class JsonConfig(val json: String, val name: String? = null)

/**
 * Определяет формат вставленного текста без выбора режима:
 * JSON (конфиг xray-core либо серверы в формате приложения — один объект
 * или массив, который делится по элементам), ссылки (URI) или
 * base64-подписка, внутри которой лежит то же самое.
 */
object ImportDetector {

    /** Защита от base64 внутри base64 без конца. */
    private const val MAX_DEPTH = 3

    fun detect(raw: String): ImportContent? = detect(raw, 0)

    private fun detect(raw: String, depth: Int): ImportContent? {
        val text = stripCodeFence(raw.trim().removePrefix("\uFEFF").trim())
        if (text.isEmpty()) return null

        // 1. JSON: конфиг xray-core либо сервер(ы) в формате приложения —
        //    объект остаётся как есть, массив делится по элементам.
        if (text.startsWith("{") || text.startsWith("[")) {
            jsonContent(text)?.let { return it }
        }

        // 2. Обычные ссылки — одна или несколько строк.
        val servers = LinkParser.parseMany(text)
        if (servers.isNotEmpty()) return ImportContent.Servers(servers)

        // 3. base64-подписка: декодируем и повторяем разбор.
        if (depth >= MAX_DEPTH) return null
        val decoded = decodeBase64OrNull(text) ?: return null
        return detect(decoded, depth + 1)
    }

    /** Убирает markdown-обёртку ```json … ```, если пользователь скопировал её из чата/доков. */
    private fun stripCodeFence(text: String): String {
        if (!text.startsWith("```")) return text
        val firstLineEnd = text.indexOf('\n')
        if (firstLineEnd < 0) return text
        return text.substring(firstLineEnd + 1).substringBeforeLast("```").trim()
    }

    /**
     * Разбирает JSON: объект — один конфиг либо один сервер, массив — по элементу
     * на каждый. Возвращает null, если это ни конфиги xray-core, ни серверы.
     */
    private fun jsonContent(text: String): ImportContent? {
        val parsed = runCatching {
            if (text.startsWith("[")) jsonArray(text) else jsonSingle(text)
        }.getOrNull() ?: return null

        return when {
            parsed.configs.isNotEmpty() -> ImportContent.Config(parsed.configs)
            parsed.servers.isNotEmpty() -> ImportContent.Servers(parsed.servers)
            else -> null
        }
    }

    /** Результат разбора одного JSON-фрагмента: конфиги и/или серверы. */
    private class Parsed(
        val configs: List<JsonConfig> = emptyList(),
        val servers: List<Server> = emptyList(),
    )

    /** Один JSON-объект: конфиг xray-core (или запись с remarks) либо сервер. */
    private fun jsonSingle(text: String): Parsed {
        val obj = JSONObject(text)
        if (looksLikeConfig(obj) || obj.has("remarks")) {
            return Parsed(configs = listOf(JsonConfig(json = text, name = configName(obj))))
        }
        val server = ServerJsonParser.parse(obj) ?: return Parsed()
        return Parsed(servers = listOf(server))
    }

    /**
     * Массив: каждый объект становится отдельным конфигом либо отдельным
     * сервером, посторонние элементы пропускаются. Если вперемешку попались
     * конфиги — отдаём их: массив конфигов встречается чаще.
     */
    private fun jsonArray(text: String): Parsed {
        val arr = JSONArray(text)
        val configs = mutableListOf<JsonConfig>()
        val servers = mutableListOf<Server>()

        for (i in 0 until arr.length()) {
            val obj = when (val element = arr.opt(i)) {
                is JSONObject -> element
                // некоторые выгрузки кладут конфиг строкой
                is String -> runCatching { JSONObject(element) }.getOrNull()
                else -> null
            } ?: continue

            if (looksLikeConfig(obj) || obj.has("remarks")) {
                configs += JsonConfig(json = obj.toString(), name = configName(obj))
            } else {
                ServerJsonParser.parse(obj)?.let { servers += it }
            }
        }
        return Parsed(configs = configs, servers = servers)
    }

    /**
     * Название конфига: поле remarks (или name), если есть. Панели часто
     * шлют их в base64 или URL-кодировке — расшифровывает [NameDecoder].
     */
    private fun configName(obj: JSONObject): String? =
        runCatching {
            NameDecoder.decode(obj.optString("remarks").ifBlank { obj.optString("name") })
        }.getOrNull()

    /** Проверяет, что это действительно JSON-объект конфига, а не случайный JSON. */
    private fun looksLikeConfig(obj: JSONObject): Boolean =
        obj.has("outbounds") || obj.has("inbounds") ||
            obj.has("log") || obj.has("dns") || obj.has("routing")

    /**
     * Декодирует base64 (обычный или URL-safe, с переносами строк).
     * Возвращает null, если результат не похож на текст подписки.
     *
     * Публично: маршрутизация «Добавить» (см. AddSubscriptionUseCase) проверяет,
     * что скрывается внутри base64, — там может быть и ссылка подписки.
     */
    fun decodeBase64OrNull(text: String): String? {
        val compact = text.filterNot { it.isWhitespace() }
        if (compact.length < 8) return null

        val variants = listOf(compact, compact.replace('-', '+').replace('_', '/'))
        for (variant in variants.distinct()) {
            val padded = variant + "=".repeat((4 - variant.length % 4) % 4)
            val bytes = runCatching { Base64.decode(padded, Base64.DEFAULT) }.getOrNull()
                ?: continue
            if (bytes.isEmpty()) continue
            val decoded = runCatching { String(bytes, Charsets.UTF_8) }.getOrNull() ?: continue
            if (decoded.isBlank() || decoded.trim() == text.trim()) continue

            // Отсекаем бинарный мусор: в подписке почти только печатные символы.
            val printable = decoded.count {
                it == '\n' || it == '\r' || it == '\t' || it.code in 32..126
            }
            if (printable.toDouble() / decoded.length < 0.95) continue
            return decoded
        }
        return null
    }
}
