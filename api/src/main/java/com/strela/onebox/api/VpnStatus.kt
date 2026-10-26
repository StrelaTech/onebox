package com.strela.onebox.api

/**
 * Состояние ядра VPN — нейтральный тип прослойки.
 * UI видит только его, а не классы конкретного ядра (xray, sing-box, ...).
 */
sealed interface VpnStatus {
    data object Stopped : VpnStatus
    data object Starting : VpnStatus
    data object Running : VpnStatus
    data class Error(val message: String) : VpnStatus
}
