package com.strela.onebox.xray

import android.content.Context
import com.strela.onebox.log.AppLog
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import xray.Xray

/**
 * Ассеты ядра geoip.dat/geosite.dat: ленивое скачивание при первом
 * подключении (п.9 ТЗ) — в APK они не вшиваются, чтобы не раздувать пакет
 * и не хранить устаревшие данные.
 *
 * xray-core ищет эти файлы в каталоге из переменной окружения
 * «xray.location.asset», по умолчанию — рядом с «исполняемым файлом», которым
 * на Android оказывается /system/bin (отсюда ошибка «open /system/bin/geoip.dat:
 * no such file or directory» у конфигов с geoip:/geosite:-правилами).
 *
 * Поведение: файлы уже скачаны → только каталог ядру; нет → скачиваются
 * с зеркал и кэшируются в noBackupFilesDir; после неудачи повтор не чаще
 * раза в [RETRY_INTERVAL_MS] (ensure зовётся при каждом включении VPN).
 * Ошибка сети не мешает подключению по обычному протоколу — только
 * geoip:/geosite:-правила провайдерских конфигов не заработают до успешной
 * загрузки.
 */
internal object GeoAssets {

    /** Каталог кэша внутри noBackupFilesDir. */
    private const val DIR = "xray"

    /** Минимальный правдоподобный размер датасета — отсекает пустые и HTML-ответы. */
    private const val MIN_BYTES = 1024L

    /** Как часто повторять неудачную загрузку в рамках одного процесса. */
    private const val RETRY_INTERVAL_MS = 5 * 60 * 1000L

    /**
     * Источники: CDN-зеркала и GitHub.
     *
     * В v2fly/domain-list-community ветка `release` больше не содержит
     * `geosite.dat` (файл отдаёт 404) — собранный датасет публикуется ассетом
     * `dlc.dat` в GitHub Releases, откуда его и берём. Дополнительно есть
     * зеркало Loyalsoldier/v2ray-rules-dat: формат тот же, поэтому подменять
     * им v2fly-датасеты безопасно.
     */
    private val SOURCES = mapOf(
        "geoip.dat" to listOf(
            "https://fastly.jsdelivr.net/gh/v2fly/geoip@release/geoip.dat",
            "https://github.com/v2fly/geoip/raw/release/geoip.dat",
            "https://raw.githubusercontent.com/v2fly/geoip/release/geoip.dat",
            // запасное зеркало на случай, если v2fly уберёт и эту ветку
            "https://fastly.jsdelivr.net/gh/Loyalsoldier/v2ray-rules-dat@release/geoip.dat",
        ),
        "geosite.dat" to listOf(
            "https://fastly.jsdelivr.net/gh/Loyalsoldier/v2ray-rules-dat@release/geosite.dat",
            // официальный v2fly-датасет: ассет dlc.dat, кладём его как geosite.dat
            "https://github.com/v2fly/domain-list-community/releases/latest/download/dlc.dat",
            "https://github.com/Loyalsoldier/v2ray-rules-dat/raw/release/geosite.dat",
            "https://raw.githubusercontent.com/Loyalsoldier/v2ray-rules-dat/release/geosite.dat",
        ),
    )

    /** Каталог ассетов уже передан ядру. */
    @Volatile
    private var locationSet = false

    /** Время последней попытки скачивания (мс) — чтобы не долбить сеть при каждом старте. */
    private var lastAttemptMs = 0L

    private val lock = Any()

    /**
     * Скачивает файлы, если их ещё нет в кэше, и указывает ядру каталог.
     * Идемпотентно; первый вызов может занять сеть — звать не с главного потока.
     */
    fun ensure(context: Context) {
        val app = context.applicationContext
        val dir = File(app.noBackupFilesDir, DIR)
        synchronized(lock) {
            dir.mkdirs()

            val missing = SOURCES.keys
                .map { File(dir, it) }
                .filterNot { it.exists() && it.length() > 0L }

            if (missing.isNotEmpty() && canRetry()) {
                lastAttemptMs = System.currentTimeMillis()
                for (file in missing) {
                    extractOrDownload(file, file.name)
                }
            }

            // Каталог задаётся в любом случае: конфиг без geoip:/geosite:-правил —
            // подключение всё равно работает.
            if (!locationSet) {
                Xray.setAssetLocation(dir.absolutePath)
                AppLog.i("GeoAssets: каталог ассетов $dir")
                locationSet = true
            }
        }
    }

    /** Не чаще [RETRY_INTERVAL_MS]: ensure зовётся и при инициализации, и при каждом старте VPN. */
    private fun canRetry(): Boolean {
        val now = System.currentTimeMillis()
        return lastAttemptMs == 0L || now - lastAttemptMs >= RETRY_INTERVAL_MS
    }

    /** Забирает файл: есть в кэше — ничего не делает; нет — пробует все источники. */
    private fun extractOrDownload(target: File, name: String) {
        if (target.exists() && target.length() > 0L) return

        val urls = SOURCES[name].orEmpty()
        try {
            for (url in urls) {
                if (download(url, target)) {
                    AppLog.i("GeoAssets: $name скачан с $url (${target.length()} байт)")
                    return
                }
            }
            AppLog.w("GeoAssets: $name не скачан ни с одного источника — " +
                "geoip/geosite-правила не заработают; повтор не раньше чем через " +
                "${RETRY_INTERVAL_MS / 60_000} мин")
        } catch (e: Exception) {
            AppLog.w("GeoAssets: скачивание $name не удалось", e)
        }
        // Ошибка сети не ломает подключение — подключаемся без ассетов.
    }

    /**
     * Скачивает файл во временный, затем атомарно переименовывает в целевой,
     * чтобы подключение не прочло полускачанный датасет.
     */
    private fun download(url: String, target: File): Boolean {
        val part = File(target.parentFile, target.name + ".part")
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 60_000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "OneBox/geo-assets")
            val code = connection.responseCode
            if (code !in 200..299) {
                AppLog.d("GeoAssets: источник $url ответил HTTP $code")
                connection.disconnect()
                return false
            }
            connection.getInputStream().use { input ->
                part.outputStream().use { output ->
                    input.copyTo(output, 64 * 1024)
                }
            }
            connection.disconnect()
            if (part.length() < MIN_BYTES) {
                AppLog.d("GeoAssets: $url вернул подозрительно маленький файл " +
                    "(${part.length()} байт)")
                part.delete()
                return false
            }
            if (!part.renameTo(target)) {
                // rename может отказать на разделах другой ФС — заменяем копией
                target.delete()
                part.copyTo(target, overwrite = true)
                part.delete()
            }
            true
        } catch (e: Exception) {
            AppLog.d("GeoAssets: источник $url не отвечает (${e.message})")
            part.delete()
            false
        }
    }
}