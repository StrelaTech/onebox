package com.strela.onebox.api

import android.content.Context

/**
 * Мост прослойки к системному VpnService: поднимает и гасит сервис.
 * Реализация живёт в :vpn ([TunVpnServiceBridge]) — она знает конкретный
 * класс сервиса; ядра и UI — нет.
 */
interface VpnServiceBridge {
    /** Поднимает VPN-сервис (connect). */
    fun start(context: Context)

    /** Гасит VPN-сервис (disconnect); сервис сам остановит ядро в верном порядке. */
    fun stop(context: Context)
}
