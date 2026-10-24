package com.strela.onebox.subscription

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Ответ сервера подписки: заголовки (для метаданных) и тело (список серверов). */
data class SubscriptionResponse(
    val headers: Map<String, String>,
    val body: String,
)

/**
 * Скачивание подписки по URL. Выделено в интерфейс, чтобы подменять транспорт
 * (OkHttp сейчас) и подключать кэш/ретраи без изменения UseCase.
 */
interface SubscriptionFetcher {
    /**
     * Скачивает подписку.
     * @throws IOException сетевые ошибки и не-2xx ответы
     */
    suspend fun fetch(url: String): SubscriptionResponse
}

/**
 * Реализация на OkHttp: заголовки ответа сохраняются как есть (регистр не важен —
 * парсер метаданных ищет их без учёта регистра), запрос отменяется вместе с корутиной.
 */
class HttpSubscriptionFetcher(
    private val client: OkHttpClient = DEFAULT_CLIENT,
) : SubscriptionFetcher {

    override suspend fun fetch(url: String): SubscriptionResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        val response = client.newCall(request).await()
        response.use {
            if (!it.isSuccessful) throw IOException("HTTP ${it.code}")
            val headers = it.headers.names().associateWith { name -> it.header(name).orEmpty() }
            val body = it.body?.string().orEmpty()
            if (body.isBlank()) throw IOException("пустое тело ответа")
            SubscriptionResponse(headers = headers, body = body)
        }
    }

    /** await вместо execute(), чтобы отмена корутины отменяла и запрос. */
    private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (continuation.isActive) continuation.resume(response)
            }
        })
        continuation.invokeOnCancellation { cancel() }
    }

    companion object {
        private const val USER_AGENT = "OneBox/1.0"

        private val DEFAULT_CLIENT = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
