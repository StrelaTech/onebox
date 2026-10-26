package com.strela.onebox.xray

import android.content.Context
import com.strela.onebox.log.AppLog
import java.io.File
import xray.Xray

/**
 * Распаковка geoip.dat/geosite.dat из APK и передача каталога ядру.
 *
 * xray-core ищет эти файлы в каталоге из переменной окружения
 * «xray.location.asset», по умолчанию — рядом с «исполняемым файлом», которым
 * на Android оказывается /system/bin (отсюда ошибка «open /system/bin/geoip.dat:
 * no such file or directory» у конфигов с geoip:/geosite:-правилами).
 *
 * Файлы лежат в assets/xray/ и при необходимости копируются в noBackupFilesDir
 * (19 МБ в облачный backup не тащим), после чего ядру передаётся этот каталог.
 */
internal object GeoAssets {

    /** Каталог внутри assets APK и внутри noBackupFilesDir. */
    private const val DIR = "xray"

    private val FILES = listOf("geoip.dat", "geosite.dat")

    @Volatile
    private var prepared = false

    private val lock = Any()

    /**
     * Извлекает файлы (если ещё не извлечены или изменились) и указывает ядру
     * каталог. Идемпотентно; первый вызов может скопировать десятки мегабайт —
     * звать не с главного потока.
     */
    fun ensure(context: Context) {
        if (prepared) return
        synchronized(lock) {
            if (prepared) return
            val app = context.applicationContext
            val dir = File(app.noBackupFilesDir, DIR)
            dir.mkdirs()
            for (name in FILES) {
                extract(app, File(dir, name), "$DIR/$name")
            }
            Xray.setAssetLocation(dir.absolutePath)
            AppLog.i("GeoAssets: каталог ассетов $dir")
            prepared = true
        }
    }

    /** Копирует файл из assets, если его нет или он отличается по размеру. */
    private fun extract(context: Context, target: File, assetPath: String) {
        val expected = assetSize(context, assetPath)
        if (expected <= 0L) {
            AppLog.w("GeoAssets: $assetPath отсутствует в APK — geoip/geosite-правила не заработают")
            return
        }
        if (target.exists() && target.length() == expected) return
        try {
            context.assets.open(assetPath).use { input ->
                target.outputStream().use { input.copyTo(it) }
            }
            AppLog.i("GeoAssets: $assetPath извлечён (${target.length()} байт)")
        } catch (e: Exception) {
            AppLog.w("GeoAssets: не удалось извлечь $assetPath", e)
        }
    }

    /**
     * Размер файла в assets. openFd работает только для несжатых ассетов
     * (в gradle стоит noCompress "dat"); для сжатых берём available().
     */
    private fun assetSize(context: Context, assetPath: String): Long {
        val fromFd = runCatching {
            context.assets.openFd(assetPath).use { it.length }
        }.getOrNull()
        if (fromFd != null) return fromFd
        return runCatching {
            context.assets.open(assetPath).use { it.available().toLong() }
        }.getOrDefault(0L)
    }
}
