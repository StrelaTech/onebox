package com.strela.onebox.subscription

import android.net.Uri
import android.util.Base64

/**
 * Декодирование названий (подписки, сервера, remarks) во всех формах,
 * которые встречаются «в дикой природе»:
 *
 * - `base64:<данные>` — явный префикс (v2rayN/marzban и подобные панели);
 * - base64 без префикса, если текст похож на него и результат читаем;
 * - URL-кодирование (`%D0%9F…`), иногда двойное;
 * - иначе текст остаётся как есть.
 *
 * Декодер консервативен: если ничего не расшифровалось в читаемый текст,
 * возвращается исходное значение — изменённый «наугад» заголовок хуже
 * исходного. null только для пустого ввода.
 */
object NameDecoder {

    /** Префикс, которым некоторые панели явно помечают base64-имя. */
    private const val BASE64_PREFIX = "base64:"

    /** Для base64 «без префикса» требуем минимум данных, чтобы не трогать короткие слова. */
    private const val MIN_BASE64_LENGTH = 8

    /** Последовательности %-кодирования: %D0, %20, … */
    private val PERCENT_SEQUENCE = Regex("%([0-9A-Fa-f]{2})")

    /** Тело base64 (паддинг проверяем отдельно, он допустим только в конце). */
    private val BASE64_BODY = Regex("[A-Za-z0-9+/]+")

    /** Декодирует название; null — если после трима ничего не осталось. */
    fun decode(raw: String): String? {
        val value = raw.trim().removePrefix("\uFEFF").trim()
        if (value.isEmpty()) return null
        // голый префикс без данных — это заглушка, а не название
        if (value.startsWith(BASE64_PREFIX, ignoreCase = true) &&
            value.substring(BASE64_PREFIX.length).isBlank()
        ) {
            return null
        }

        // URL-кодирование и явный префикс могут лежать друг на друге
        // (`base64:SGVsbG8%3D`, `base64%3A…`), поэтому повторяем до двух раз.
        var text = value
        repeat(2) {
            decodeWithPrefix(text)?.let { return it }
            text = percentDecode(text) ?: text
        }

        decodeWithPrefix(text)?.let { return it }

        // base64 без префикса — только если похоже на него и декод читаем
        decodeBase64Heuristic(text)?.let { return it }

        return text
    }

    // ── шаги декодирования ─────────────────────────────────────────────────

    /**
     * Снимает URL-кодирование; null, если %-последовательностей нет или
     * декод не удался.
     */
    private fun percentDecode(text: String): String? {
        if (!text.containsPercentSequence()) return null
        return runCatching { Uri.decode(text) }.getOrNull()
    }

    /** `base64:000 …`: декодирует данные после префикса; null, если префикса нет. */
    private fun decodeWithPrefix(text: String): String? {
        if (!text.startsWith(BASE64_PREFIX, ignoreCase = true)) return null
        val payload = text.substring(BASE64_PREFIX.length).trim()
        if (payload.isEmpty()) return null

        decodeBase64(payload)?.let { return it }

        // Данные после префикса могли быть ещё и в URL-кодировке: base64:SGVsbG8%3D
        val unquoted = percentDecode(payload)
        if (unquoted != null && unquoted != payload) {
            decodeBase64(unquoted)?.let { return it }
        }

        // префикс есть, но данные битые: убираем хоть сам префикс — лучше, чем «base64:…»
        return unquoted ?: payload
    }

    private fun String.containsPercentSequence(): Boolean = PERCENT_SEQUENCE.containsMatchIn(this)

    /**
     * base64 без префикса: только если строка целиком из base64-символов
     * (паддинг — в конце), а декод — читаемый текст.
     */
    private fun decodeBase64Heuristic(text: String): String? {
        val compact = toBase64Alphabet(text)
        if (compact.length < MIN_BASE64_LENGTH || !isBase64Shape(compact)) return null
        return decodeBase64(compact)?.takeIf { it != text }
    }

    /**
     * Честное base64-декодирование (обычный или URL-safe алфавит, переносы
     * строк допустимы). null, если результат не читаемый текст.
     */
    private fun decodeBase64(text: String): String? {
        val compact = toBase64Alphabet(text)
        val padded = compact + "=".repeat((4 - compact.length % 4) % 4)
        if (padded.length % 4 != 0) return null

        val bytes = runCatching { Base64.decode(padded, Base64.DEFAULT) }.getOrNull() ?: return null
        if (bytes.isEmpty()) return null
        val decoded = runCatching { String(bytes, Charsets.UTF_8) }.getOrNull() ?: return null
        if (decoded.isBlank() || !isPrintable(decoded)) return null
        return decoded
    }

    private fun toBase64Alphabet(text: String): String =
        text.filterNot(Char::isWhitespace).replace('-', '+').replace('_', '/')

    /** Форма base64: тело из алфавита, паддинг («=», не более двух) — только в конце. */
    private fun isBase64Shape(compact: String): Boolean {
        val body = compact.trimEnd('=')
        val padding = compact.length - body.length
        return body.isNotEmpty() && padding in 0..2 && BASE64_BODY.matches(body)
    }

    /**
     * Отсекает бинарный мусор: допускаем переводы строк/табуляцию и любой
     * печатный текст (в т.ч. не-латиницу), бракуем control-символы и маркеры
     * невалидного UTF-8 (U+FFFD).
     */
    private fun isPrintable(text: String): Boolean {
        if (text.isEmpty()) return false
        val printable = text.count { ch ->
            when {
                ch == '\n' || ch == '\r' || ch == '\t' -> true
                ch == '\uFFFD' -> false
                ch.code < 32 || ch.code == 127 -> false
                else -> true
            }
        }
        return printable.toDouble() / text.length >= 0.95
    }
}
