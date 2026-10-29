package com.strela.onebox.xray

import android.content.Context
import com.strela.onebox.api.ProfileSource
import com.strela.onebox.api.PingMethod
import com.strela.onebox.api.PingOptions
import com.strela.onebox.api.PingState
import com.strela.onebox.api.VpnCore
import com.strela.onebox.api.VpnStatus
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.Server
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import xray.Xray

/**
 * Реализация [VpnCore] поверх нативного xray-core (xray.aar, собран через
 * gomobile). Регистрируется в прослойке [com.strela.onebox.api.VpnConnector]:
 * UI и VpnService ядро не знают — общаются только через прослойку.
 *
 * Ядро живёт на уровне процесса, состояние отражается в [status].
 */
object XrayEngine : VpnCore {

    override val id: String = "xray"

    override val displayName: String = "Xray"

    /** Лимит ожидания ответа при пинге, если настройки не отдали своё значение, мс. */
    private const val PING_TIMEOUT_MS = 5000L

    /** Остановки ядра сериализуются в одном потоке: stop'ы не идут параллельно. */
    private val stopExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "xray-stop").apply { isDaemon = true }
    }

    // Версия ядра: считается в фоне в initialize(), чтобы не грузить 30+ МБ .so
    // на главном потоке (иначе джанк на старте — "Skipped 65 frames").
    private val _version = MutableStateFlow("…")
    override val version: StateFlow<String> = _version.asStateFlow()

    private val _status = MutableStateFlow<VpnStatus>(VpnStatus.Stopped)
    override val status: StateFlow<VpnStatus> = _status.asStateFlow()

    private val _pings = MutableStateFlow<Map<String, PingState>>(emptyMap())
    override val pings: StateFlow<Map<String, PingState>> = _pings.asStateFlow()

    private fun setPing(profileId: String, value: PingState) {
        _pings.value = _pings.value + (profileId to value)
    }

    /** Текущий конфиг (JSON). Собирается в [initialize]/[refreshFromProfile]. */
    @Volatile
    private var config: String = DEFAULT_CONFIG

    /** Нативные вызовы ядра сериализуются: start/stop/ping не должны пересекаться. */
    private val nativeLock = Any()

    private lateinit var appContext: Context
    private lateinit var profiles: ProfileSource

    override fun initialize(context: Context, profiles: ProfileSource) {
        appContext = context.applicationContext
        this.profiles = profiles
        // Считаем конфиг сразу, чтобы начать писать в него данные до старта ядра.
        config = activeConfig()

        // Загрузка нативной .so уходит с главного потока.
        Thread {
            // заодно распаковываем geoip.dat/geosite.dat — ядру они нужны
            // до StartInstance (конфиги с geoip:/geosite:-правилами)
            GeoAssets.ensure(appContext)
            _version.value = try {
                Xray.version()
            } catch (_: Throwable) {
                "н/д"
            }
            AppLog.d("xray-core version=${_version.value}")
        }.start()
    }

    /**
     * Конфиг, который реально пойдёт в ядро: из профиля (JSON-подписки или
     * выбранного сервера) — если тот задан; иначе пользовательский сохранённый
     * JSON, иначе стартовый.
     */
    private fun activeConfig(): String {
        val profile = profiles.activeProfile()
        profile?.configJson?.let { return it }
        profile?.server?.let { return XrayConfigBuilder.build(it, profiles.blockAds()) }
        return profiles.userConfig() ?: DEFAULT_CONFIG
    }

    override fun refreshFromProfile() {
        config = activeConfig()
        AppLog.i("xray: конфиг пересобран из активного профиля")
    }

    /**
     * Запуск ядра поверх tun-интерфейса VpnService.
     * [fd] — уже detachFd() из ParcelFileDescriptor.
     * [onFailure] вызывается, если ядро не стартовало, чтобы сервис успел погасить tun.
     */
    override fun startTun(fd: Int, onFailure: (String) -> Unit) {
        if (_status.value is VpnStatus.Starting || _status.value is VpnStatus.Running) {
            // молчаливый return оставил бы tun без читателя (живой туннель без
            // трафика) — отдаём управление сервису, он закроет fd и погасит VPN
            AppLog.w("startTun: ядро уже запущено, fd=$fd не отдётся ядру")
            onFailure("ядро уже запущено")
            return
        }
        val cfg = buildTunConfig(config)
        AppLog.i("startTun: fd=$fd")
        _status.value = VpnStatus.Starting
        Thread {
            try {
                // geoip.dat/geosite.dat должны быть на месте до StartInstance
                GeoAssets.ensure(appContext)
                synchronized(nativeLock) {
                    // глушим stdout/stderr ядра: на Android они уходят в logcat
                    Xray.setQuiet()
                    Xray.setTunFd(fd.toLong())
                    Xray.startInstance(cfg)
                }
                AppLog.i("startTun: ядро запущено")
                _status.value = VpnStatus.Running
            } catch (e: Exception) {
                AppLog.e("startTun: ошибка ядра", e)
                val message = e.message ?: "unknown error"
                _status.value = VpnStatus.Error(message)
                onFailure(message)
            }
        }.start()
    }

    /**
     * Останавливает ядро с жёстким таймаутом: нативный Close() выполняется
     * в отдельном потоке, здесь ждём не дольше [timeoutMs]. Зависшее ядро не
     * должно блокировать разрыв туннеля — иначе кнопка «Отключить» оставит
     * VPN включённым.
     *
     * Статус выставляется в [VpnStatus.Stopped] в любом случае (пользователь
     * отключился), даже если Close() завис — тогда остановка доделается сама,
     * когда ядро оживёт.
     *
     * Возвращает true, если ядро остановилось за отведённое время,
     * false — если остановка зависла и туннель нужно рвать принудительно.
     * Блокирует вызывающий поток максимум [timeoutMs] — можно вызывать и с
     * главного потока.
     */
    override fun stopBlocking(timeoutMs: Long): Boolean {
        AppLog.i("stop: остановка ядра (таймаут $timeoutMs мс)")
        val done = CountDownLatch(1)
        stopExecutor.execute {
            synchronized(nativeLock) {
                try {
                    Xray.stopInstance()
                } catch (e: Exception) {
                    AppLog.w("stop: ошибка остановки", e)
                }
            }
            done.countDown()
        }
        val stopped = done.await(timeoutMs, TimeUnit.MILLISECONDS)
        if (!stopped) {
            AppLog.w("stop: ядро не остановилось за $timeoutMs мс — статус ставлю Stopped, туннель рвём принудительно")
        }
        _status.value = VpnStatus.Stopped
        return stopped
    }

    /** Вызывается сервисом, когда establish() вернул null (отзыв разрешения / чужой VPN). */
    override fun onVpnRevoked() {
        AppLog.w("onVpnRevoked: establish() вернул null")
        _status.value = VpnStatus.Error("VPN-доступ отозван")
    }

    /** Вызывается сервисом при ошибке поднятия tun. */
    override fun onVpnError(message: String) {
        AppLog.e("onVpnError: $message")
        _status.value = VpnStatus.Error(message)
    }

    /**
     * Пингует сервер выбранным способом (настройки пинга, в фоне).
     *
     * GET/HEAD — HTTP-проба через временное ядро: поднимает инстанс с конфигом
     * именно этого сервера, меряет, гасит. Если VPN уже поднят — пинг
     * невозможен (второй инстанс ядра не создаётся), поэтому сообщаем об
     * этом, а не меряем «не тот» сервер.
     *
     * TCP/ICMP — ядро не нужно: голый хендшейк/эхо на адрес сервера, работают
     * и при активном VPN, и параллельно (nativeLock не задействуется).
     */
    override fun pingServer(server: Server) {
        if (_pings.value[server.id] is PingState.InProgress) return
        setPing(server.id, PingState.InProgress)

        val options = profiles.pingOptions()
        val timeoutMs = options.timeoutMs.takeIf { it > 0 } ?: PING_TIMEOUT_MS
        val method = options.method

        Thread {
            try {
                if (method == PingMethod.TCP || method == PingMethod.ICMP) {
                    pingDirect(server, method, options, timeoutMs)
                } else {
                    pingThroughCore(server, method, options, timeoutMs)
                }
            } catch (e: Exception) {
                AppLog.w("pingServer: ${server.name} → ошибка: ${e.message}")
                setPing(server.id, PingState.Failed(e.message ?: "неизвестная ошибка"))
            }
        }.start()
    }

    /** Пинг без ядра: TCP-хендшейк или ICMP-эхо напрямую на адрес сервера. */
    private fun pingDirect(
        server: Server,
        method: PingMethod,
        options: PingOptions,
        timeoutMs: Long,
    ) {
        val ms = Xray.ping(method.key, server.address, server.port.toLong(), timeoutMs, options.testUrl)
        AppLog.i("pingServer(${method.key}): ${server.name} → $ms мс")
        setPing(server.id, PingState.Ok(ms))
    }

    /** HTTP-проба через ядро: временный инстанс под этот сервер, start→ping→stop. */
    private fun pingThroughCore(
        server: Server,
        method: PingMethod,
        options: PingOptions,
        timeoutMs: Long,
    ) {
        synchronized(nativeLock) {
            // источник истины — нативное состояние ядра, а не поток статуса
            val alreadyRunning = try {
                Xray.isRunning()
            } catch (_: Throwable) {
                _status.value is VpnStatus.Running || _status.value is VpnStatus.Starting
            }
            if (alreadyRunning) {
                AppLog.w("pingServer: ядро занято (VPN активен), пинг невозможен")
                setPing(server.id, PingState.Failed("отключите VPN"))
                return@synchronized
            }

            try {
                GeoAssets.ensure(appContext)
                Xray.setQuiet()
                Xray.startInstance(XrayConfigBuilder.build(server, profiles.blockAds()))
                try {
                    val ms = Xray.ping(method.key, server.address, server.port.toLong(), timeoutMs, options.testUrl)
                    AppLog.i("pingServer(${method.key}): ${server.name} → $ms мс")
                    setPing(server.id, PingState.Ok(ms))
                } finally {
                    try {
                        Xray.stopInstance()
                    } catch (e: Exception) {
                        AppLog.w("pingServer: не удалось остановить ядро после пинга", e)
                    }
                }
            } catch (e: Exception) {
                AppLog.w("pingServer: ${server.name} → ошибка: ${e.message}")
                setPing(server.id, PingState.Failed(e.message ?: "неизвестная ошибка"))
            }
        }
    }

    override fun saveUserConfig(json: String) {
        profiles.saveUserConfig(json)
        config = json
        AppLog.i("xray: сохранён пользовательский JSON-конфиг")
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
