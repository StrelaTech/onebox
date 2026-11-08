package com.strela.onebox.subscription

/**
 * Сервер подключения — разобранная ссылка (vless/vmess/trojan/ss/socks).
 * Серверы объединяются в подписки — [Subscription].
 */
data class Server(
    val id: String,
    val name: String,
    val protocol: Protocol,
    val address: String,
    val port: Int,
    /** UUID (vless/vmess), пароль (ss/trojan) или логин (socks) */
    val credential: String = "",
    /**
     * Доп. параметр, зависящий от протокола:
     * vless — flow, vmess — alterId, ss — метод шифрования, socks — пароль, trojan — пусто
     */
    val extra: String = "",
    /** Транспорт: tcp, ws, grpc, httpupgrade, splithttp */
    val network: String = "tcp",
    /** TLS/Reality настройки */
    val security: String = "none",
    val sni: String = "",
    val alpn: String = "",
    val fingerprint: String = "",
    val publicKey: String = "",
    val shortId: String = "",
    val spiderX: String = "",
    /** Транспортные параметры */
    val path: String = "",
    val host: String = "",
    val serviceName: String = "",
    /**
     * Hysteria2: пароль обфускации QUIC-трафика (masquerade).
     * Пусто — обфускации нет.
     */
    val obfsPassword: String = "",
    /** TLS: разрешить самоподписанные сертификаты (hy2://?insecure=1). */
    val allowInsecure: Boolean = false,
    /** Исходная ссылка — для отображения и повторного шаринга */
    val rawUrl: String = "",
    /**
     * Исходный xray-конфиг, из которого разобран сервер (для JSON-импорта).
     * Используется как есть при сборке конфига ядра: DNS, routing и прочие
     * тонкости провайдерского конфига сохраняются. Пусто у серверов из ссылок.
     */
    val json: String = ""
) {
    enum class Protocol {
        VLESS, VMESS, TROJAN, SHADOWSOCKS, SOCKS, HYSTERIA2, UNKNOWN;

        val displayName: String
            get() = when (this) {
                VLESS -> "VLESS"
                VMESS -> "VMess"
                TROJAN -> "Trojan"
                SHADOWSOCKS -> "Shadowsocks"
                SOCKS -> "SOCKS"
                HYSTERIA2 -> "Hysteria2"
                UNKNOWN -> "?"
            }
    }

    /** Человекочитаемое описание для списка: address:port */
    val endpoint: String
        get() = "$address:$port"
}
