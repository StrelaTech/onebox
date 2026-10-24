package com.strela.onebox.subscription

/**
 * Подписка — группа серверов: одна подписка по URL (ссылки ИЛИ JSON-конфиги),
 * отдельный импортированный JSON-/серверный набор или постоянная группа
 * «Серверы», куда попадает ручной импорт ссылок.
 * Хранится сериализованно в SharedPreferences (см. SubscriptionStore).
 *
 * У каждой подписки есть заголовок, описание, дата окончания и сведения
 * о трафике (сколько потрачено и какой лимит) — их заполняет импорт подписки
 * по URL из заголовков ответа; для ручных групп они остаются пустыми.
 * Серверов в подписке может быть сколько угодно.
 *
 * У подписки есть активный сервер: именно он попадает в конфиг ядра.
 * JSON-конфиги импортируются как серверы (конфиг сохраняется в Server.json,
 * см. XrayConfigParser); поле [json] — устаревший способ хранения
 * целого конфига в JSON-подписке (старые сохранённые данные).
 */
data class Subscription(
    val id: String,
    /** Заголовок подписки. */
    val title: String,
    /** Описание подписки (источник, пометки); у группы «Серверы» пустое. */
    val description: String = "",
    /**
     * Потрачено трафика, байты (upload + download из Subscription-Userinfo).
     * null — источник подписки не отдаёт сведений о трафике.
     */
    val usedBytes: Long? = null,
    /** Лимит трафика, байты; null — лимита нет. */
    val trafficLimitBytes: Long? = null,
    /** Дата окончания подписки, epoch millis; null — бессрочная. */
    val expireAtMillis: Long? = null,
    /** Серверы подписки. */
    val servers: List<Server> = emptyList(),
    val selectedServerId: String? = null,
    /** Вставленный JSON-конфиг xray-core (устаревшее хранение JSON-подписок). */
    val json: String? = null,
    /** URL источника подписки — ключ дедупликации и обновления; null у ручных. */
    val url: String? = null,
    /** Когда мы последний раз скачивали подписку, epoch millis. */
    val lastUpdateMillis: Long = 0L,
) {
    companion object {
        /**
         * ID постоянной подписки «Серверы»: сюда складываются все серверы,
         * добавленные вручную (ссылки, base64, JSON-объекты серверов).
         * Константа, чтобы не плодить новые группы при каждом действии.
         */
        const val SERVERS_ID = "local"
    }

    /** Активный сервер; если id не задан/устарел — первый в списке. */
    val selectedServer: Server?
        get() = servers.firstOrNull { it.id == selectedServerId } ?: servers.firstOrNull()

    /** Это JSON-подписка — она хранит готовый конфиг, а не список серверов. */
    val isJson: Boolean
        get() = !json.isNullOrBlank()

    val endpoint: String
        get() = selectedServer?.endpoint.orEmpty()
}
