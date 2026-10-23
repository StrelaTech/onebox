package com.strela.onebox.profile

import android.util.Base64
import org.json.JSONObject

/** Что удалось распознать во вставленном тексте. */
sealed interface ImportContent {
    /** Одна или несколько ссылок серверов. */
    data class Servers(val servers: List<Server>) : ImportContent

    /** JSON-конфиг xray-core. */
    data class Config(val json: String) : ImportContent
}

/**
 * Определяет формат вставленного текста без выбора режима:
 * JSON-конфиг, ссылки (URI) или base64-подписка, внутри которой лежит то же самое.
 */
object ImportDetector {

    /** Защита от base64 внутри base64 без конца. */
    private const val MAX_DEPTH = 3

    fun detect(raw: String): ImportContent? = detect(raw, 0)

    private fun detect(raw: String, depth: Int): ImportContent? {
        val text = stripCodeFence(raw.trim().removePrefix("\uFEFF").trim())
        if (text.isEmpty()) return null

        // 1. JSON-конфиг xray-core.
        if (text.startsWith("{")) jsonConfig(text)?.let { return ImportContent.Config(it) }

        // 2. Обычные ссылки — одна или несколько строк.
        val servers = LinkParser.parseMany(text)
        if (servers.isNotEmpty()) return ImportContent.Servers(servers)

        // 3. base64-подписка: декодируем и повторяем разбор.
        if (depth >= MAX_DEPTH) return null
        val decoded = decodeBase64(text) ?: return null
        return detect(decoded, depth + 1)
    }

    /** Убирает markdown-обёртку ```json … ```, если пользователь скопировал её из чата/доков. */
    private fun stripCodeFence(text: String): String {
        if (!text.startsWith("```")) return text
        val firstLineEnd = text.indexOf('\n')
        if (firstLineEnd < 0) return text
        return text.substring(firstLineEnd + 1).substringBeforeLast("```").trim()
    }

    /** Проверяет, что это действительно JSON-объект конфига, а не случайный JSON. */
    private fun jsonConfig(text: String): String? {
        val obj = runCatching { JSONObject(text) }.getOrNull() ?: return null
        val looksLikeConfig = obj.has("outbounds") || obj.has("inbounds") ||
            obj.has("log") || obj.has("dns") || obj.has("routing")
        return if (looksLikeConfig) text else null
    }

    /**
     * Декодирует base64 (обычный или URL-safe, с переносами строк).
     * Возвращает null, если результат не похож на текст подписки.
     */
    private fun decodeBase64(text: String): String? {
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
