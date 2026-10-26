package com.strela.onebox.api

import android.content.Context
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Прослойка между UI и ядрами VPN.
 *
 * Единственная точка контакта UI с ядром: UI никогда не знает, какое ядро
 * активировано (xray, sing-box, ...), а общается только с этим фасадом.
 * Ядерные модули реализуют [VpnCore] и регистрируются здесь; сервис VPN
 * подключается через [VpnServiceBridge]. Активное ядро выбирается в
 * [init] и в любой момент может быть заменено — подписчики [status],
 * [pings] и [version] автоматически переключаются на новое ядро.
 */
object VpnConnector {

    /** Заглушка версии ядра до инициализации. */
    private const val INITIAL_VERSION = "…"

    /** Таймаут остановки ядра по умолчанию, мс. */
    private const val DEFAULT_STOP_TIMEOUT_MS = 3000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Зарегистрированные ядра; список маленький — косяка по блокировкам нет. */
    private val cores = java.util.concurrent.ConcurrentHashMap<String, VpnCore>()

    /** Активное ядро; до инициализации — заглушка «VPN выключен». */
    @Volatile
    private var core: VpnCore = IdleCore

    /** Источник профиля — передаётся ядру в [init]. */
    @Volatile
    private var profiles: ProfileSource? = null

    /** Мост к системному VpnService; регистрирует :engine. */
    @Volatile
    private var bridge: VpnServiceBridge? = null

    /** Инициализация идемпотентна: Activity и VpnService зовут её вместе. */
    private val initLock = Any()

    @Volatile
    private var initialized = false

    private var linksJob: Job? = null

    // ── состояния для UI ────────────────────────────────────────────────────

    private val _status = MutableStateFlow<VpnStatus>(VpnStatus.Stopped)
    val status: StateFlow<VpnStatus> = _status.asStateFlow()

    private val _pings = MutableStateFlow<Map<String, PingState>>(emptyMap())
    val pings: StateFlow<Map<String, PingState>> = _pings.asStateFlow()

    private val _version = MutableStateFlow(INITIAL_VERSION)
    val version: StateFlow<String> = _version.asStateFlow()

    // ── регистрация поставщиков ─────────────────────────────────────────────

    /** Регистрирует ядро. Пока его не выбрали в [init], оно неактивно. */
    fun register(core: VpnCore) {
        cores[core.id] = core
        AppLog.i("VpnConnector: зарегистрировано ядро ${core.id} («${core.displayName}»)")
    }

    /** Регистрирует мост к системному VpnService. */
    fun registerService(bridge: VpnServiceBridge) {
        this.bridge = bridge
        AppLog.i("VpnConnector: зарегистрирован VPN-сервис ${bridge.javaClass.simpleName}")
    }

    /**
     * Выбирает и инициализирует активное ядро. Идемпотентно.
     * [coreId] — какое ядро активировать; по умолчанию первое зарегистрированное.
     */
    fun init(context: Context, profiles: ProfileSource, coreId: String? = null) {
        synchronized(initLock) {
            if (initialized) return
            initialized = true
        }
        this.profiles = profiles
        val selectedId = coreId ?: cores.keys.firstOrNull()
        val selected = selectedId?.let { cores[it] }
        if (selected == null) {
            AppLog.e("VpnConnector: ядро «$selectedId» не зарегистрировано — VPN будет выключен")
            return
        }
        activate(selected)
        AppLog.i("VpnConnector: активное ядро — ${selected.id}")
        selected.initialize(context.applicationContext, profiles)
    }

    /** Переключает фасад на новое ядро и подписывается на его потоки. */
    private fun activate(newCore: VpnCore) {
        linksJob?.cancel()
        linksJob = scope.launch {
            launch { newCore.status.collect { _status.value = it } }
            launch { newCore.pings.collect { _pings.value = it } }
            launch { newCore.version.collect { _version.value = it } }
        }
        core = newCore
        // Первое значение ставим сразу — не дожидаясь первого collect'а.
        _status.value = newCore.status.value
        _pings.value = newCore.pings.value
        _version.value = newCore.version.value
    }

    // ── чтение состояния ────────────────────────────────────────────────────

    /** Ядро запущено или стартует. */
    val isRunning: Boolean
        get() = core.status.value is VpnStatus.Running || core.status.value is VpnStatus.Starting

    /** Сохранён ли пользователем «сырой» конфиг (JSON-режим). */
    val hasUserConfig: Boolean
        get() = profiles?.userConfig() != null

    // ── команды UI ──────────────────────────────────────────────────────────

    /** Поднимает VPN (VpnService) — сервис сам передаст fd активному ядру. */
    fun start(context: Context) {
        val bridge = bridge
        if (bridge == null) {
            AppLog.e("VpnConnector: VpnServiceBridge не зарегистрирован — VPN не запускаем")
            return
        }
        bridge.start(context)
    }

    /** Гасит VPN. Ядро останавливает сам сервис — в правильном порядке. */
    fun stop(context: Context) {
        val bridge = bridge
        if (bridge == null) {
            AppLog.e("VpnConnector: VpnServiceBridge не зарегистрирован — VPN не останавливаем")
            return
        }
        bridge.stop(context)
    }

    /** Пересборка конфига ядра из активного профиля. */
    fun refreshFromProfile() = core.refreshFromProfile()

    /** Пингует сервер через ядро (в фоне). */
    fun pingServer(server: Server) = core.pingServer(server)

    /** Запуск ядра поверх tun-fd — зовёт только VpnService (:engine). */
    fun startTun(fd: Int, onFailure: (String) -> Unit = {}) = core.startTun(fd, onFailure)

    /**
     * Остановка ядра с таймаутом (блокирует поток не дольше [timeoutMs]).
     * true — успело, false — остановка зависла, туннель рвать принудительно.
     */
    fun stopBlocking(timeoutMs: Long = DEFAULT_STOP_TIMEOUT_MS): Boolean =
        core.stopBlocking(timeoutMs)

    /** VPN-доступ отозван системой — звонит VpnService. */
    fun onVpnRevoked() = core.onVpnRevoked()

    /** Ошибка поднятия tun — звонит VpnService. */
    fun onVpnError(message: String) = core.onVpnError(message)

    /** Сохраняет пользовательский JSON-конфиг (JSON-режим). */
    fun saveUserConfig(json: String) = core.saveUserConfig(json)

    /** Список зарегистрированных ядер (для выбора ядра в настройках). */
    fun registeredCores(): List<VpnCore> = cores.values.toList()

    /** Активное ядро. */
    fun activeCore(): VpnCore = core
}

/** Заглушка ядра до инициализации: статус «выключено», команды без действия. */
private object IdleCore : VpnCore {

    override val id: String = "idle"
    override val displayName: String = "не инициализировано"

    override val version: StateFlow<String> = MutableStateFlow("…")

    override val status: StateFlow<VpnStatus> = MutableStateFlow(VpnStatus.Stopped)

    override val pings: StateFlow<Map<String, PingState>> = MutableStateFlow(emptyMap())

    override fun initialize(context: Context, profiles: ProfileSource) = Unit

    override fun refreshFromProfile() = Unit

    override fun startTun(fd: Int, onFailure: (String) -> Unit) {
        AppLog.e("VpnConnector: ядро не инициализировано, fd=$fd не принят")
        onFailure("ядро не инициализировано")
    }

    override fun stopBlocking(timeoutMs: Long): Boolean = true

    override fun onVpnRevoked() = Unit

    override fun onVpnError(message: String) = Unit

    override fun pingServer(server: Server) {
        AppLog.e("VpnConnector: ядро не инициализировано, пинг невозможен")
    }

    override fun saveUserConfig(json: String) = Unit
}
