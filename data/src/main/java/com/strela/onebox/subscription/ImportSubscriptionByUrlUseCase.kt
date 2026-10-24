package com.strela.onebox.subscription

import android.content.Context
import com.strela.onebox.log.AppLog
import kotlinx.coroutines.CancellationException

/** Результат импорта подписки — Success всегда несёт подписку, Failure — причину. */
sealed interface SubscriptionImportResult {
    data class Success(
        val subscription: Subscription,
        /** false — обновляли уже существующую подписку с этим URL */
        val created: Boolean,
        val serversCount: Int,
    ) : SubscriptionImportResult

    data class Failure(val reason: SubscriptionImportReason) : SubscriptionImportResult
}

/** Причина отказа импорта — UI мапит её в локализованное сообщение. */
enum class SubscriptionImportReason { INSECURE_URL, INVALID_URL, NETWORK, EMPTY_CONTENT }

/**
 * Импорт подписки по URL — вся логика в одном месте:
 *
 * 1. Валидация: только https://, корректный формат URL.
 * 2. Скачивание (асинхронно, отменяемо).
 * 3. Метаданные из заголовков (Profile-Title / Announce / Subscription-Userinfo /
 *    Profile-Update-Interval): заголовок, описание, дата окончания,
 *    потраченный трафик и лимит. Fallback заголовка — домен из URL.
 * 4. Дедупликация по URL: уже известная подписка обновляется
 *    (серверы и метаданные заменяются свежими),
 *    новая — создаёт отдельную подписку.
 * 5. Тело ответа разбирается через ImportDetector: base64-подписка,
 *    обычный список ссылок или JSON-конфиг (один объект или массив —
 *    каждый конфиг становится отдельным сервером подписки, см. XrayConfigParser).
 */
class ImportSubscriptionByUrlUseCase(
    private val fetcher: SubscriptionFetcher,
    private val repository: SubscriptionRepository,
) {

    suspend operator fun invoke(rawUrl: String): SubscriptionImportResult {
        val url = try {
            SubscriptionUrl.validate(rawUrl)
        } catch (e: SubscriptionUrl.InsecureUrlException) {
            AppLog.w("Импорт подписки отклонён: ${e.message}")
            return SubscriptionImportResult.Failure(SubscriptionImportReason.INSECURE_URL)
        } catch (e: SubscriptionUrl.InvalidUrlException) {
            AppLog.w("Импорт подписки отклонён: ${e.message}")
            return SubscriptionImportResult.Failure(SubscriptionImportReason.INVALID_URL)
        }

        val response = try {
            fetcher.fetch(url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("Импорт подписки: ошибка загрузки $url", e)
            return SubscriptionImportResult.Failure(SubscriptionImportReason.NETWORK)
        }

        val meta = SubscriptionHeaderParser.parse(response.headers)

        val content = try {
            ImportDetector.detect(response.body)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("Импорт подписки: тело ответа не разобрано", e)
            null
        }
        if (content == null) {
            return SubscriptionImportResult.Failure(SubscriptionImportReason.EMPTY_CONTENT)
        }

        // Заголовок подписки: из заголовков ответа, иначе домен URL. Каждый URL —
        // отдельная подписка, даже если метаданных нет: не сливаем незнакомые подписки.
        val title = meta.title ?: SubscriptionUrl.hostOf(url) ?: url
        val existing = repository.findByUrl(url)

        val subscription = when (content) {
            is ImportContent.Servers -> {
                if (content.servers.isEmpty()) {
                    return SubscriptionImportResult.Failure(SubscriptionImportReason.EMPTY_CONTENT)
                }
                Subscription(
                    id = existing?.id ?: newId(),
                    title = title,
                    // Описание: из заголовка Announce; иначе пустая — у подписки
                    // нет описания, источников подставок как таковых не видно.
                    description = meta.announce ?: "",
                    usedBytes = meta.usedBytes,
                    trafficLimitBytes = meta.totalBytes,
                    expireAtMillis = meta.expireAtMillis,
                    servers = content.servers,
                    selectedServerId = existing?.selectedServerId,
                    url = url,
                    lastUpdateMillis = System.currentTimeMillis(),
                )
            }

            // Подписка вернула JSON-конфиг xray (один объект или массив): каждый
            // конфиг раскладывается на сервер с сохранением исходного JSON в
            // Server.json — таким образом в подписке может быть сразу много серверов.
            is ImportContent.Config -> {
                val servers = content.configs.mapNotNull { config ->
                    XrayConfigParser.parse(config.json, config.name)
                }.distinctServers()
                if (servers.isEmpty()) {
                    return SubscriptionImportResult.Failure(SubscriptionImportReason.EMPTY_CONTENT)
                }
                Subscription(
                    id = existing?.id ?: newId(),
                    title = title,
                    description = meta.announce ?: url,
                    usedBytes = meta.usedBytes,
                    trafficLimitBytes = meta.totalBytes,
                    expireAtMillis = meta.expireAtMillis,
                    servers = servers,
                    selectedServerId = existing?.selectedServerId,
                    url = url,
                    lastUpdateMillis = System.currentTimeMillis(),
                )
            }
        }

        val saved = repository.save(subscription)
        return SubscriptionImportResult.Success(
            subscription = saved,
            created = existing == null,
            serversCount = saved.servers.size,
        )
    }

    private fun newId(): String = java.util.UUID.randomUUID().toString()
}

/** Один и тот же сервер: совпадают протокол, адрес, порт и учётные данные. */
private fun Server.sameServerAs(other: Server): Boolean =
    protocol == other.protocol &&
        address == other.address &&
        port == other.port &&
        credential == other.credential

/** Убирает дубликаты серверов, сохраняя порядок и первое вхождение. */
private fun List<Server>.distinctServers(): List<Server> {
    val unique = mutableListOf<Server>()
    for (server in this) {
        if (unique.none { it.sameServerAs(server) }) unique.add(server)
    }
    return unique
}
