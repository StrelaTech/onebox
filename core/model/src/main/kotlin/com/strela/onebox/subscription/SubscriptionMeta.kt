package com.strela.onebox.subscription

/**
 * Метаданные подписки, разобранные из заголовков ответа.
 *
 * Импорт подписки по URL переписывает ими поля [Subscription]: заголовок,
 * описание, дату окончания и сведения о трафике (сколько потрачено, лимит).
 * Все поля опциональны — провайдер не обязан отдавать заголовки, поэтому
 * отсутствие метаданных — нормальная ситуация, а не ошибка.
 */
data class SubscriptionMeta(
    /** Имя из заголовка Profile-Title (уже декодировано из base64), если было. */
    val title: String? = null,
    /** Описание из заголовка Announce (текст, base64 или URL-кодирование). */
    val announce: String? = null,
    /** Использованный трафик (upload), байты. */
    val uploadBytes: Long? = null,
    /** Скачанный трафик (download), байты. */
    val downloadBytes: Long? = null,
    /** Лимит трафика, байты; 0/отсутствие — без лимита. */
    val totalBytes: Long? = null,
    /** Момент истечения подписки в epoch millis (из поля expire, там он в секундах). */
    val expireAtMillis: Long? = null,
    /** Рекомендуемый интервал автообновления, часы (Profile-Update-Interval). */
    val updateIntervalHours: Int? = null,
) {
    /** Потрачено трафика всего (upload + download). */
    val usedBytes: Long?
        get() = when {
            uploadBytes == null && downloadBytes == null -> null
            else -> (uploadBytes ?: 0L) + (downloadBytes ?: 0L)
        }
}
