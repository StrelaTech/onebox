package com.strela.onebox.xray

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.strela.onebox.log.AppLog
import com.strela.onebox.profile.ConfigBuilder
import com.strela.onebox.profile.Server
import com.strela.onebox.profile.ProfileStore
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.vpn.TunVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import xray.Xray

/**
 * Тонкая обёртка над нативным xray-core (xray.aar, собран через gomobile).
 * Ядро живёт на уровне процесса, состояние отражается в [status].
 */
object XrayController {

    private const val PREFS = "xray"
    private const val KEY_CONFIG = "config"

    /** Лимит ожидания ответа при пинге профиля, мс. */
    private const val PING_TIMEOUT_MS = 5000L

    // Версия ядра: считается в фоне в init(), чтобы не грузить 30+ МБ .so
    // на главном потоке (иначе джанк на старте — "Skipped 65 frames").
    private val _version = MutableStateFlow("…")
    val version: StateFlow<String> = _version.asStateFlow()

    sealed interface Status {
        data object Stopped : Status
        data object Starting : Status
        data object Running : Status
        data class Error(val message: String) : Status
    }

    private val _status = MutableStateFlow<Status>(Status.Stopped)
    val status: StateFlow<Status> = _status.asStateFlow()

    /** Результат пинга по id профиля: null — ещё не пинговали. */
    sealed interface Ping {
        data object InProgress : Ping
        data class Ok(val millis: Long) : Ping
        data class Failed(val reason: String) : Ping
    }

    private val _pings = MutableStateFlow<Map<String, Ping>>(emptyMap())
    val pings: StateFlow<Map<String, Ping>> = _pings.asStateFlow()

    private fun setPing(profileId: String, value: Ping) {
        _pings.value = _pings.value + (profileId to value)
    }

    /** Текущий конфиг (JSON). Загружается из SharedPreferences в [init]. */
    var config: String = DEFAULT_CONFIG
        private set

    /** Был ли конфиг сохранён пользователем (JSON-режим), а не взят по умолчанию. */
    var hasUserConfig: Boolean = false
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        hasUserConfig = prefs.contains(KEY_CONFIG)
        config = prefs.getString(KEY_CONFIG, DEFAULT_CONFIG) ?: DEFAULT_CONFIG

        // Активный сервер выбранного профиля (если есть) подменяет сохранённый JSON.
        ProfileStore.selectedServer()?.let { server ->
            config = ConfigBuilder.build(server, blockAds = SettingsStore.blockAds.value)
            AppLog.i("init: активный сервер ${server.name} (${server.endpoint})")
        }

        // Загрузка нативной .so уходит с главного потока.
        Thread {
            _version.value = try {
                Xray.version()
            } catch (_: Throwable) {
                "н/д"
            }
            AppLog.d("xray-core version=${_version.value}")
        }.start()
    }

    fun saveConfig(context: Context, value: String) {
        config = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONFIG, value)
            .apply()
    }

    /** Запуск ядра поверх tun-интерфейса VpnService. [fd] — уже detachFd() из ParcelFileDescriptor. */
    fun startTun(fd: Int) {
        val cfg = buildTunConfig(activeConfig())
        AppLog.i("startTun: fd=$fd, config=$cfg")
        _status.value = Status.Starting
        Thread {
            try {
                // глушим stdout/stderr ядра: на Android они уходят в logcat
                Xray.setQuiet()
                Xray.setTunFd(fd.toLong())
                Xray.startInstance(cfg)
                AppLog.i("startTun: ядро запущено")
                _status.value = Status.Running
            } catch (e: Exception) {
                AppLog.e("startTun: ошибка ядра", e)
                _status.value = Status.Error(e.message ?: "unknown error")
            }
        }.start()
    }

    /**
     * Останавливает ядро. Возвращает результат сразу после остановки (синхронно).
     * Вызывать не с главного потока.
     */
    fun stopBlocking() {
        AppLog.i("stop: остановка ядра")
        try {
            Xray.stopInstance()
        } catch (e: Exception) {
            AppLog.w("stop: ошибка остановки", e)
        }
        _status.value = Status.Stopped
    }

    /** Асинхронная остановка ядра (для вызова из UI). */
    fun stop() {        Thread { stopBlocking() }.start()
    }

    /** Вызывается сервисом, когда establish() вернул null (отзыв разрешения / чужой VPN). */
    fun onVpnRevoked() {
        AppLog.w("onVpnRevoked: establish() вернул null")
        _status.value = Status.Error("VPN-доступ отозван")
    }

    /** Вызывается сервисом при ошибке поднятия tun. */
    fun onVpnError(message: String) {
        AppLog.e("onVpnError: $message")
        _status.value = Status.Error(message)
    }

    val isRunning: Boolean
        get() = status.value == Status.Running || status.value == Status.Starting

    /**
     * Конфиг, который реально пойдёт в ядро: при выбранном профиле он собирается
     * заново из активного сервера (профиль мог быть отредактирован), иначе берётся сохранённый JSON.
     */
    private fun activeConfig(): String =
        ProfileStore.selectedServer()?.let {
            ConfigBuilder.build(it, blockAds = SettingsStore.blockAds.value)
        } ?: config

    /** Пересобирает сохранённый конфиг из активного сервера (после выбора/удаления). */
    fun refreshFromProfile() {
        val server = ProfileStore.selectedServer() ?: return
        config = ConfigBuilder.build(server, blockAds = SettingsStore.blockAds.value)
        AppLog.i("refreshFromProfile: активен ${server.name} (${server.endpoint})")
    }

    /**
     * Пингует сервер через ядро (в фоне).
     *
     * Поднимает ядро с конфигом именно этого сервера, измеряет задержку и гасит.
     * Если VPN уже поднят — пинг невозможен (второй инстанс ядра не создаётся),
     * поэтому сообщаем об этом, а не меряем «не тот» сервер.
     */
    fun pingServer(server: Server) {
        if (_pings.value[server.id] is Ping.InProgress) return
        setPing(server.id, Ping.InProgress)

        Thread {
            // источник истины — нативное состояние ядра, а не поток статуса
            val alreadyRunning = try {
                Xray.isRunning()
            } catch (_: Throwable) {
                isRunning
            }
            if (alreadyRunning) {
                AppLog.w("pingServer: ядро занято (VPN активен), пинг невозможен")
                setPing(server.id, Ping.Failed("отключите VPN"))
                return@Thread
            }

            try {
                Xray.setQuiet()
                Xray.startInstance(ConfigBuilder.build(server, blockAds = SettingsStore.blockAds.value))
                try {
                    val ms = Xray.pingThroughOutbound("", 0L, "", PING_TIMEOUT_MS)
                    AppLog.i("pingServer: ${server.name} → $ms мс")
                    setPing(server.id, Ping.Ok(ms))
                } finally {
                    try {
                        Xray.stopInstance()
                    } catch (e: Exception) {
                        AppLog.w("pingServer: не удалось остановить ядро после пинга", e)
                    }
                }
            } catch (e: Exception) {
                AppLog.w("pingServer: ${server.name} → ошибка: ${e.message}")
                setPing(server.id, Ping.Failed(e.message ?: "неизвестная ошибка"))
            }
        }.start()
    }

    /** Запускает TunVpnService в режиме подключения. */
    fun connectVpn(context: Context) {
        AppLog.i("connectVpn: запуск TunVpnService")
        val intent = Intent(context, TunVpnService::class.java)
            .setAction(TunVpnService.ACTION_CONNECT)
        ContextCompat.startForegroundService(context, intent)
    }

    /**
     * Гасит TunVpnService. Ядро останавливает сам сервис — в правильном порядке
     * (сначала ядро, потом tun и stopSelf), поэтому здесь stop() не вызывается.
     */
    fun disconnectVpn(context: Context) {
        AppLog.i("disconnectVpn: останавливаю VPN-сервис")
        val intent = Intent(context, TunVpnService::class.java)
            .setAction(TunVpnService.ACTION_DISCONNECT)
        context.startService(intent)
    }

    /**
     * Собирает конфиг для VPN-режима: из пользовательского конфига берётся всё,
     * кроме inbound'ов — вместо них подставляется tun-инбаунд (fd берётся из env).
     */
    private fun buildTunConfig(base: String): String {
        val obj = try {
            JSONObject(base)
        } catch (_: Exception) {
            JSONObject()
        }
        val tunInbound = JSONObject().apply {
            put("protocol", "tun")
            put("settings", JSONObject().apply {
                put("name", "onebox0")
                put("MTU", 1500)
            })
        }
        obj.put("inbounds", JSONArray().put(tunInbound))
        // чтобы DNS-запросы из tun не зацикливались — форсируем внешние DNS, если их нет
        if (!obj.has("dns")) {
            obj.put("dns", JSONObject().apply {
                put("servers", JSONArray().put("1.1.1.1").put("8.8.8.8"))
            })
        }
        return obj.toString()
    }
}

/** Стартовый конфиг: локальный socks-инбаунд → freedom (для теста без VPN). */
private val DEFAULT_CONFIG = """
{
  "log": { "loglevel": "warning" },
  "inbounds": [
    {
      "listen": "127.0.0.1",
      "port": 10808,
      "protocol": "socks",
      "settings": { "udp": true }
    }
  ],
  "outbounds": [ { "protocol": "freedom" } ]
}
""".trimIndent()
