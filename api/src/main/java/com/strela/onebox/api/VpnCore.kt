package com.strela.onebox.api

import android.content.Context
import com.strela.onebox.subscription.Server
import kotlinx.coroutines.flow.StateFlow

/**
 * SPI ядра VPN: контракт, по которому прослойка общается с конкретным
 * ядром (xray, sing-box, naive, ...). Реализация регистрируется в
 * [VpnConnector.register] модулем-поставщиком ядра; UI ядро не знает.
 */
interface VpnCore {

    /** Идентификатор ядра («xray», ...) — по нему выбирается активное. */
    val id: String

    /** Человекочитаемое имя ядра — для логов и выбора ядра в настройках. */
    val displayName: String

    /**
     * Жизненный цикл: контекст приложения и источник профилей передаёт
     * прослойка. Может вызываться из нового процесса (VpnService стартовал
     * без Activity) — реализация обязана быть идемпотентной.
     */
    fun initialize(context: Context, profiles: ProfileSource)

    /** Версия ядра; до инициализации — заглушка. */
    val version: StateFlow<String>
    /** Состояние ядра. */
    val status: StateFlow<VpnStatus>
    /** Пинги серверов по их id. */
    val pings: StateFlow<Map<String, PingState>>

    /** Пересборка внутреннего конфига ядра (изменились профиль/настройки). */
    fun refreshFromProfile()

    /**
     * Запуск ядра поверх tun-интерфейса VpnService.
     * [fd] — уже detachFd() из ParcelFileDescriptor; ядро владеет им дальше.
     * [onFailure] вызывается, если ядро не стартовало — сервис успеет
     * закрыть fd и погасить VPN.
     */
    fun startTun(fd: Int, onFailure: (String) -> Unit = {})

    /**
     * Остановка ядра с жёстким таймаутом (блокирует вызывающий поток
     * не дольше [timeoutMs]; можно звать с главного потока).
     * true — ядро остановилось, false — остановка зависла и туннель
     * нужно рвать принудительно.
     */
    fun stopBlocking(timeoutMs: Long): Boolean

    /** VPN-доступ отозван системой (establish() вернул null / onRevoke). */
    fun onVpnRevoked()

    /** Ошибка поднятия tun. */
    fun onVpnError(message: String)

    /** Пинг сервера через ядро (в фоне). */
    fun pingServer(server: Server)

    /** Сохраняет пользовательский JSON-конфиг (JSON-режим). */
    fun saveUserConfig(json: String)
}
