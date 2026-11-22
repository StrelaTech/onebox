package com.strela.onebox.api

/**
 * Трафик текущего VPN-подключения в байтах: отправлено и принято.
 * Считается по UID приложения от момента запуска туннеля.
 */
data class VpnTraffic(
    val sentBytes: Long = 0L,
    val receivedBytes: Long = 0L,
)
