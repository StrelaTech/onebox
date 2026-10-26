package com.strela.onebox.subscription

/**
 * Разбор HTTP-заголовков ответа подписки.
 *
 * Поддерживаемые заголовки (де-факто стандарт v2rayN/подписок):
 * - `Profile-Title` — имя подписки, часто в base64 (в т.ч. с префиксом `base64:`)
 *   или URL-кодировкой — расшифровывает [NameDecoder];
 * - `Announce` — описание подписки (панели шлют его тоже в base64/percent);
 * - `Subscription-Userinfo` — `upload=…; download=…; total=…; expire=…` (байты; expire — epoch-секунды);
 * - `Profile-Update-Interval` — рекомендуемый интервал автообновления в часах.
 *
 * Все заголовки опциональны: отсутствие любого из них не ошибка,
 * вызывающий код падает обратно на имя по домену.
 */
object SubscriptionHeaderParser {

    private const val HEADER_TITLE = "profile-title"
    private const val HEADER_ANNOUNCE = "announce"
    private const val HEADER_USERINFO = "subscription-userinfo"
    private const val HEADER_UPDATE_INTERVAL = "profile-update-interval"

    /**
     * Разбирает метаданные из заголовков ответа.
     *
     * @param headers заголовки в любом регистре (поиск регистронезависимый)
     */
    fun parse(headers: Map<String, String>): SubscriptionMeta {
        val lower = headers.entries.associate { it.key.lowercase() to it.value.trim() }

        return SubscriptionMeta(
            title = lower[HEADER_TITLE]?.let(::decodeTitle),
            // Описание — тот же текст, что и заголовок: base64/percent раскручивает NameDecoder
            announce = lower[HEADER_ANNOUNCE]?.let(NameDecoder::decode),
            uploadBytes = userInfoField(lower[HEADER_USERINFO], "upload"),
            downloadBytes = userInfoField(lower[HEADER_USERINFO], "download"),
            totalBytes = userInfoField(lower[HEADER_USERINFO], "total")
                ?.takeIf { it > 0 }, // 0 означает «без лимита»
            expireAtMillis = userInfoField(lower[HEADER_USERINFO], "expire")
                ?.let { it * 1000 }, // expire приходит в секундах
            updateIntervalHours = lower[HEADER_UPDATE_INTERVAL]
                ?.toIntOrNull()
                ?.takeIf { it in 1..(24 * 30) }, // от 1 часа до месяца — остальное мусор
        )
    }

    /**
     * Имя подписки из значения заголовка: вся расшифровка (base64 с префиксом
     * и без, URL-кодирование) — в общем [NameDecoder]. Возвращает null для
     * пустых/заглушечных значений.
     */
    fun decodeTitle(raw: String): String? = NameDecoder.decode(raw)

    /** Числовое поле из `upload=…; download=…; total=…; expire=…`. */
    private fun userInfoField(userInfo: String?, key: String): Long? {
        if (userInfo.isNullOrBlank()) return null
        return userInfo
            .split(';', ',', ' ')
            .mapNotNull { part ->
                val (name, value) = part.trim().split('=', limit = 2).let {
                    if (it.size != 2) return@mapNotNull null
                    it[0].trim().lowercase() to it[1].trim()
                }
                if (name != key) return@mapNotNull null
                value.toLongOrNull()
            }
            .firstOrNull()
    }
}
