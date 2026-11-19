package com.strela.onebox.api

/** Предпочтительное семейство IP при разрешении доменных имён. */
enum class IpType(val key: String) {
    AUTO("auto"),
    IPV4("ipv4"),
    IPV6("ipv6");

    /** Значение dns.queryStrategy в xray-конфиге. */
    val queryStrategy: String
        get() = when (this) {
            AUTO -> "UseIP"
            IPV4 -> "UseIPv4"
            IPV6 -> "UseIPv6"
        }

    companion object {
        fun fromKey(key: String?): IpType = entries.firstOrNull { it.key == key } ?: AUTO
    }
}

/**
 * Фрагментация TLS-потока (обход DPI). Реализуется freedom-аутбаундом
 * с секцией fragment и dialerProxy у основного аутбаунда.
 */
data class FragmentOptions(
    val enabled: Boolean = false,
    /** Какие пакеты фрагментировать: tlshello, 1-3, 1-2 и т.п. */
    val packets: String = "tlshello",
    /** Диапазон длины фрагмента, байты: «100-200». */
    val length: String = "100-200",
    /** Пауза между фрагментами, мс: «10-20». */
    val interval: String = "10-20",
)

/** Мультиплексирование исходящих соединений (xray mux). */
data class MuxOptions(
    val enabled: Boolean = false,
    /** Число одновременных TCP-подключений в одном mux-канале. */
    val concurrency: Int = 8,
    /** Число одновременных UDP-подключений (XUDP). */
    val xudpConcurrency: Int = 16,
)

/** Сетевые параметры, которые применяются к сборке конфига и tun-интерфейсу. */
data class NetworkOptions(
    /** MTU tun-интерфейса. */
    val mtu: Int = DEFAULT_MTU,
    val ipType: IpType = IpType.AUTO,
    val fragment: FragmentOptions = FragmentOptions(),
    val mux: MuxOptions = MuxOptions(),
) {
    companion object {
        const val DEFAULT_MTU = 1500
        const val MIN_MTU = 576
        const val MAX_MTU = 9000
    }
}
