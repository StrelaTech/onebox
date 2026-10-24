package com.strela.onebox.subscription

import java.net.URI

/**
 * Валидация ссылки подписки и работа с её URL.
 *
 * Безопасность: принимаем только `https://`. `http://` отклоняется как
 * «Небезопасное соединение» — подписка может содержать учётные данные серверов,
 * поэтому plaintext недопустим.
 */
object SubscriptionUrl {

    /** Ссылка не начинается с https:// — трафик по ней пойдёт открытым текстом. */
    class InsecureUrlException(message: String) : Exception(message)

    /** Ссылка вообще не является корректным URL. */
    class InvalidUrlException(message: String) : Exception(message)

    /**
     * Проверяет и нормализует ссылку подписки.
     *
     * @return нормализованная ссылка (обрезанные пробелы, https, есть хост)
     * @throws InsecureUrlException если схема http://
     * @throws InvalidUrlException если это не URL или нет хоста
     */
    fun validate(raw: String): String {
        val text = raw.trim().removePrefix("\uFEFF").trim()
        if (text.isEmpty()) throw InvalidUrlException("пустая ссылка")

        val scheme = text.substringBefore("://", missingDelimiterValue = "").lowercase()
        when (scheme) {
            "http" -> throw InsecureUrlException("небезопасное соединение: разрешён только https")
            "https" -> Unit
            else -> throw InvalidUrlException("неизвестная схема: «$scheme»")
        }

        val uri = runCatching { URI(text) }
            .getOrElse { throw InvalidUrlException("некорректный URL") }
        if (uri.host.isNullOrBlank()) throw InvalidUrlException("в URL нет хоста")

        return text
    }

    /** То же, что [validate], но возвращает null вместо исключений — для UI-проверок. */
    fun validateOrNull(raw: String): String? = runCatching { validate(raw) }.getOrNull()

    /**
     * Извлекает домен из ссылки для использования как имя группы,
     * например `https://sub.example.com/path` → `sub.example.com`.
     */
    fun hostOf(url: String): String? =
        runCatching { URI(url.trim()).host }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
}
