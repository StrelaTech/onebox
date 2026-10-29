package com.strela.onebox.log

import android.content.Context
import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Логгер приложения: пишет сообщения в файл `filesDir/logs.log` (в дополнение к logcat).
 * Файл — обычный текстовый, доступен через `run-as com.strela.onebox` или через share.
 *
 * Пишем синхронно под блокировкой: объёмы небольшие, а так гарантирован порядок строк
 * и отсутствие гонок между потоками (UI, сервис, поток ядра).
 */
object AppLog {

    private const val TAG = "OneBox"
    private const val FILE_NAME = "logs.log"
    private const val MAX_BYTES = 2L * 1024 * 1024 // 2 МБ — затем ротация

    private val lock = Any()
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    @Volatile
    private var logFile: File? = null

    /** Включает запись в файл. Идемпотентно — можно звать из Activity и из сервиса. */
    fun init(context: Context) {
        synchronized(lock) {
            if (logFile != null) return
            logFile = File(context.applicationContext.filesDir, FILE_NAME)
            append("── сессия начата ──")
        }
    }

    val file: File?
        get() = logFile

    fun d(message: String) = log(Log.DEBUG, message, null)

    fun i(message: String) = log(Log.INFO, message, null)

    fun w(message: String, throwable: Throwable? = null) = log(Log.WARN, message, throwable)

    fun e(message: String, throwable: Throwable? = null) = log(Log.ERROR, message, throwable)

    /** Очищает файл журнала, оставляя отметку о сбросе. */
    fun clear() {
        val target = logFile ?: return
        synchronized(lock) {
            try {
                target.writeText("")
            } catch (_: Exception) {
                // если файл занят — просто не чистим, логирование продолжит работать
            }
        }
        append("── журнал очищен пользователем ──")
    }

    /**
     * Хвост журнала для консоли (последние [limitBytes]), с разрыва первой
     * обрубленной строки. Весь файл, если он меньше лимита; "" — файла нет.
     */
    fun tail(limitBytes: Long = 256L * 1024): String {
        val target = logFile ?: return ""
        synchronized(lock) {
            return try {
                val length = target.length()
                if (length <= limitBytes) {
                    target.readText()
                } else {
                    RandomAccessFile(target, "r").use { raf ->
                        raf.seek(length - limitBytes)
                        val bytes = ByteArray(limitBytes.toInt())
                        raf.readFully(bytes)
                        String(bytes, Charsets.UTF_8).substringAfter('\n', "")
                    }
                }
            } catch (_: Exception) {
                ""
            }
        }
    }

    private fun log(level: Int, message: String, throwable: Throwable?) {
        // logcat — как обычно (фильтруются по тегу OneBox)
        when (level) {
            Log.ERROR -> Log.e(TAG, message, throwable)
            Log.WARN -> Log.w(TAG, message, throwable)
            Log.INFO -> Log.i(TAG, message, throwable)
            else -> Log.d(TAG, message, throwable)
        }

        val severity = when (level) {
            Log.ERROR -> "E"
            Log.WARN -> "W"
            Log.INFO -> "I"
            else -> "D"
        }
        val line = buildString {
            append(timeFormat.format(Date()))
            append(' ').append(severity).append('/').append(TAG).append(": ").append(message)
            if (throwable != null) {
                append('\n').append(throwable.stackTraceToString())
            }
        }
        append(line)
    }

    private fun append(line: String) {
        val target = logFile ?: return
        synchronized(lock) {
            try {
                rotateIfNeeded(target)
                target.appendText(line + "\n")
            } catch (_: Exception) {
                // не логируем ошибку логирования самим логированием
            }
        }
    }

    private fun rotateIfNeeded(target: File) {
        if (target.length() <= MAX_BYTES) return
        val backup = File(target.parentFile, "$FILE_NAME.1")
        if (backup.exists()) backup.delete()
        target.renameTo(backup)
    }
}
