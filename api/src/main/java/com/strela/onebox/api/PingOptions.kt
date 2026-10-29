package com.strela.onebox.api

/**
 * Способ пинга серверов. Ключи [key] передаются нативному ядру
 * (см. pingPing в xray/ping.go) и не меняются — это часть ABI.
 */
enum class PingMethod(val key: String) {
    /** HTTP GET через ядро — путь реального трафика. */
    PROXY_GET("get"),

    /** HTTP HEAD через ядро — путь реального трафика. */
    PROXY_HEAD("head"),

    /** Голый TCP-хендшейк до адреса сервера, без ядра. */
    TCP("tcp"),

    /** ICMP-эхо на адрес сервера, без ядра. */
    ICMP("icmp");

    companion object {
        fun fromKey(key: String?): PingMethod = entries.firstOrNull { it.key == key } ?: PROXY_GET
    }
}

/**
 * Настройки пинга серверов: способ, тайм-аут ожидания и URL HTTP-пробы.
 * Текутся к ядру через [ProfileSource.pingOptions]: ядро читает их в момент
 * пинга, так что изменения настроек применяются без перезапуска.
 */
data class PingOptions(
    val method: PingMethod = PingMethod.PROXY_GET,
    /** Лимит ожидания ответа, мс. */
    val timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    /** Цель HTTP-проб для GET/HEAD; TCP и ICMP используют адрес сервера. */
    val testUrl: String = DEFAULT_TEST_URL,
) {
    companion object {
        const val DEFAULT_TIMEOUT_MS = 5_000L
        const val DEFAULT_TEST_URL = "https://www.gstatic.com/generate_204"
    }
}
