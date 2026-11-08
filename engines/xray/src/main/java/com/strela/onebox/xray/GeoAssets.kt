package com.strela.onebox.xray

import android.content.Context
import com.strela.onebox.log.AppLog
import java.io.File
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
 * Поведение: файлы уже скачаны → только каталог ядру; нет → один раз
 * скачиваются и кэшируются в noBackupFilesDir. Ошибка сети не мешает
 * подключению по обычному протоколу — только geoip:/geosite:-правила
 * провайдерских конфигов не заработают до успешной загрузки.
 */
internal object GeoAssets {

    /** Каталог кэша внутри noBackupFilesDir. */
    private const val DIR = "xray"

    /** Источники: репозиторий v2fly и CDN-зеркала к его release-веткам. */
    private val SOURCES = mapOf(
        "geoip.dat" to listOf(
            "https://fastly.jsdelivr.net/gh/v2fly/geoip@release/geoip.dat",
            "https://github.com/v2fly/geoip/raw/release/geoip.dat",
            "https://raw.githubusercontent.com/v2fly/geoip/release/geoip.dat",
        ),
        "geosite.dat" to listOf(
            "https://fastly.jsdelivr.net/gh/v2fly/domain-list-community@release/geosite.dat",
            "https://github.com/v2fly/domain-list-community/raw/release/geosite.dat",
            "https://raw.githubusercontent.com/v2fly/domain-list-community/release/geosite.dat",
        ),
    )

    @Volatile
    private var prepared = false

    private val lock = Any()

    /**
     * Скачивает файлы, если их ещё нет в кэше, и указывает ядру каталог.
     * Идемпотентно; первый вызов может занять сеть — звать не с главного потока.
     */
    fun ensure(context: Context) {
        if (prepared) return
        synchronized(lock) {
            if (prepared) return
            val app = context.applicationContext
            val dir = File(app.noBackupFilesDir, DIR)
            dir.mkdirs()

            for (name in SOURCES.keys) {
                extractOrDownload(File(dir, name), name)
            }
            // Каталог задаётся в любом случае: конфиг без geoip:/geosite:-правил —
            // подключение всё равно работает.
            Xray.setAssetLocation(dir.absolutePath)
            AppLog.i("GeoAssets: каталог ассетов $dir")
            prepared = true
        }
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
                "geoip/geosite-правила не заработают: следующая попытка при включении VPN")
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
            val connection = URL(url).openConnection().apply {
                connectTimeout = 10_000
                readTimeout = 60_000
            }
            connection.getInputStream().use { input ->
                part.outputStream().use { output ->
                    input.copyTo(output, 64 * 1024)
                }
            }
            if (part.length() <= 0L) {
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