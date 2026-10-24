package com.strela.onebox.xray

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.ConfigBuilder
import com.strela.onebox.subscription.Server
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.vpn.TunVpnService
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
 * Тонкая обёртка над нативным xray-core (xray.aar, собран через gomobile).
 * Ядро живёт на уровне процесса, состояние отражается в [status].
 */
object XrayController {

    private const val PREFS = "xray"
    private const val KEY_CONFIG = "config"

    /** Лимит ожидания ответа при пинге профиля, мс. */
    private const val PING_TIMEOUT_MS = 5000L

    /**
     * Жёсткий лимит остановки ядра, мс: закрытие инстанса не должно блокировать
     * разрыв туннеля (иначе зависшее ядро оставит VPN «включённым» после кнопки
     * «Отключить»).
     */
    private const val STOP_TIMEOUT_MS = 3000L

    /** Остановки ядра сериализуются в одном потоке: stop'ы не идут параллельно. */
    private val stopExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "xray-stop").apply { isDaemon = true }
    }

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

    /** Сохранённый пользователем JSON-конфиг (используется, когда профиль не выбран). */
    private var savedConfig: String = DEFAULT_CONFIG

    /** Был ли конфиг сохранён пользователем (JSON-режим), а не взят по умолчанию. */
    var hasUserConfig: Boolean = false
        private set

    /** Нативные вызовы ядра сериализуются: start/stop/ping не должны пересекаться. */
    private val nativeLock = Any()

    /** Контекст приложения — нужен [GeoAssets] для распаковки geoip.dat/geosite.dat. */
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        hasUserConfig = prefs.contains(KEY_CONFIG)
        savedConfig = prefs.getString(KEY_CONFIG, DEFAULT_CONFIG) ?: DEFAULT_CONFIG
        config = savedConfig

        // Активная подписка подменяет сохранённый JSON:
        // у JSON-подписки берём её конфиг, иначе собираем из активного сервера.
        SubscriptionStore.selectedSubscription()?.let { subscription ->
            if (subscription.isJson) {
                config = subscription.json.orEmpty()
                AppLog.i("init: активна JSON-подписка ${subscription.title}")
            } else {
                subscription.selectedServer?.let { server ->
                    config = ConfigBuilder.build(server, blockAds = SettingsStore.blockAds.value)
                    AppLog.i("init: активный сервер ${server.name} (${server.endpoint})")
                }
            }
        }

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

    fun saveConfig(context: Context, value: String) {
        config = value
        savedConfig = value
        hasUserConfig = true
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONFIG, value)
            .apply()
    }

    /**
     * Запуск ядра поверх tun-интерфейса VpnService.
     * [fd] — уже detachFd() из ParcelFileDescriptor.
     * [onFailure] вызывается, если ядро не стартовало, чтобы сервис успел погасить tun.
     */
    fun startTun(fd: Int, onFailure: (String) -> Unit = {}) {
        if (_status.value is Status.Starting || _status.value is Status.Running) {
            // молчаливый return оставил бы tun без читателя (живой туннель без
            // трафика) — отдаём управление сервису, он закроет fd и погасит VPN
            AppLog.w("startTun: ядро уже запущено, fd=$fd не отдётся ядру")
            onFailure("ядро уже запущено")
            return
        }
        val cfg = buildTunConfig(activeConfig())
        AppLog.i("startTun: fd=$fd")
        _status.value = Status.Starting
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
                _status.value = Status.Running
            } catch (e: Exception) {
                AppLog.e("startTun: ошибка ядра", e)
                val message = e.message ?: "unknown error"
                _status.value = Status.Error(message)
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
     * Статус выставляется в [Status.Stopped] в любом случае (пользователь
     * отключился), даже если Close() завис — тогда остановка доделается сама,
     * когда ядро оживёт.
     *
     * Возвращает true, если ядро остановилось за отведённое время,
     * false — если остановка зависла и туннель нужно рвать принудительно.
     * Блокирует вызывающий поток максимум [timeoutMs] — можно вызывать и с
     * главного потока.
     */
    fun stopBlocking(timeoutMs: Long = STOP_TIMEOUT_MS): Boolean {
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
        _status.value = Status.Stopped
        return stopped
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
     * Конфиг, который реально пойдёт в ядро: при выбранной подписке он берётся
     * из JSON-подписки или собирается заново из активного сервера,
     * иначе берётся сохранённый JSON.
     */
    private fun activeConfig(): String {
        val subscription = SubscriptionStore.selectedSubscription()
        if (subscription != null) {
            if (subscription.isJson) return subscription.json.orEmpty()
            subscription.selectedServer?.let {
                return ConfigBuilder.build(it, blockAds = SettingsStore.blockAds.value)
            }
        }
        return config
    }

    /**
     * Пересобирает конфиг из активной подписки (JSON или сервер)
     * или возвращается к сохранённому JSON, если подписки нет/она удалена.
     */
    fun refreshFromProfile() {
        val subscription = SubscriptionStore.selectedSubscription()
        if (subscription == null) {
            config = savedConfig
            AppLog.i("refreshFromProfile: активной подписки нет, используется JSON-конфиг")
            return
        }
        if (subscription.isJson) {
            config = subscription.json.orEmpty()
            AppLog.i("refreshFromProfile: активна JSON-подписка ${subscription.title}")
            return
        }
        val server = subscription.selectedServer
        if (server == null) {
            config = savedConfig
            AppLog.i("refreshFromProfile: в подписке нет серверов, используется JSON-конфиг")
            return
        }
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
            synchronized(nativeLock) {
                // источник истины — нативное состояние ядра, а не поток статуса
                val alreadyRunning = try {
                    Xray.isRunning()
                } catch (_: Throwable) {
                    isRunning
                }
                if (alreadyRunning) {
                    AppLog.w("pingServer: ядро занято (VPN активен), пинг невозможен")
                    setPing(server.id, Ping.Failed("отключите VPN"))
                    return@synchronized
                }

                try {
                    GeoAssets.ensure(appContext)
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
     * Гасит TunVpnService по кнопке «Отключить». Ядро останавливает сам сервис —
     * в правильном порядке (сначала ядро с таймаутом, потом tun и stopSelf).
     * Если интент до сервиса не дошёл (он убит / старт из фона запрещён) —
     * глушим ядро напрямую, чтобы туннель гарантированно упал.
     */
    fun disconnectVpn(context: Context) {
        AppLog.i("disconnectVpn: останавливаю VPN-сервис")
        val intent = Intent(context, TunVpnService::class.java)
            .setAction(TunVpnService.ACTION_DISCONNECT)
        try {
            context.startService(intent)
        } catch (e: Exception) {
            AppLog.w("disconnectVpn: интент не доставлен, останавливаю ядро напрямую", e)
            Thread { stopBlocking() }.start()
        }
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
