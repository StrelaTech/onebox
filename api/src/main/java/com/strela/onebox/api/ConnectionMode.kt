package com.strela.onebox.api

/**
 * Режим подключения трафика: VPN-туннель (tun), локальный системный прокси
 * (socks-порт без туннеля) или оба сразу. Хранится в настройках (:data),
 * ядро и сервис читают только через [ProfileSource.connectionMode].
 */
enum class ConnectionMode(val key: String) {
    TUN("tun"),
    PROXY("proxy"),
    TUN_PLUS_PROXY("tun+proxy");

    companion object {
        fun fromKey(key: String?): ConnectionMode = entries.firstOrNull { it.key == key } ?: TUN
    }
}
