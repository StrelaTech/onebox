package com.strela.onebox.subscription

import com.strela.onebox.log.AppLog
import kotlinx.coroutines.CancellationException

/**
 * Кнопка «Добавить» — единая точка входа импорта произвольного текста.
 * Тип вставленных данных определяется автоматически и раскладывается
 * по правилам:
 *
 *  1. Ссылка https:// — URL-подписка: скачивается и образует ОТДЕЛЬНЫЙ
 *     профиль (повторная вставка того же URL обновляет этот профиль,
 *     дедупликация по URL).
 *  2. URI серверов (vless://, vmess://, trojan://, ss://, socks://) —
 *     стандартный профиль [Subscription.SERVERS_ID] «Серверы»,
 *     куда складываются все самоделки без дубликатов.
 *  3. JSON-конфиг xray-core (объект или массив) — тоже в стандартный профиль:
 *     каждый конфиг раскладывается на сервер.
 *  4. base64 — декодируется, а затем содержимое разбирается теми же
 *     правилами: ссылки и JSON уходят в «Серверы», а спрятанная внутри
 *     ссылка https:// — в отдельный URL-профиль (и так далее).
 *  5. http:// отклоняется как небезопасное соединение — подписка может
 *     содержать учётные данные серверов.
 */
class AddSubscriptionUseCase(
    private val fetcher: SubscriptionFetcher,
    private val repository: SubscriptionRepository,
) {

    private val urlImport = ImportSubscriptionByUrlUseCase(fetcher, repository)

    suspend operator fun invoke(raw: String): SubscriptionImportResult {
        val text = raw.trim().removePrefix("\uFEFF").trim()
        if (text.isEmpty()) {
            return SubscriptionImportResult.Failure(SubscriptionImportReason.EMPTY_CONTENT)
        }
        return route(text, depth = 0)
    }

    /**
     * Разбирает текст по правилам класса. [depth] защищает от base64
     * внутри base64; произвольный мусор не циклится, глубины хватает.
     */
    private suspend fun route(text: String, depth: Int): SubscriptionImportResult {
        // 1) Ссылка https:// — URL-подписка: отдельный профиль на каждый URL.
        val url = SubscriptionUrl.validateOrNull(text)
        if (url != null) {
            AppLog.i("AddSubscriptionUseCase: распознана URL-подписка $url — отдельный профиль")
            return urlImport(url)
        }

        // 2) http:// в аудит данных не годится: plaintext с учётными данными.
        if (text.startsWith("http://", ignoreCase = true)) {
            AppLog.w("AddSubscriptionUseCase: отклонено как небезопасное соединение")
            return SubscriptionImportResult.Failure(SubscriptionImportReason.INSECURE_URL)
        }

        // 3) URI-ссылки серверов или JSON — стандартный профиль «Серверы».
        //    base64-подписка декодируется внутри detect, поэтому завернутая
        //    в base64 пачка ссылок тоже приходит сюда.
        val content = ImportDetector.detect(text)
        if (content != null) {
            when (content) {
                is ImportContent.Servers -> return mergeIntoStandard(content.servers)
                is ImportContent.Config -> return mergeIntoStandard(
                    content.configs.mapNotNull { XrayConfigParser.parse(it.json, it.name) }
                )
            }
        }

        // 4) «и так далее»: base64 может скрывать даже ссылку URL-подписки.
        //    Сами ссылки и JSON выше распознает detect; здесь остаётся только URL.
        if (depth < MAX_DEPTH) {
            val decoded = ImportDetector.decodeBase64OrNull(text)
            if (decoded != null) {
                val decodedUrl = SubscriptionUrl.validateOrNull(decoded)
                if (decodedUrl != null) {
                    AppLog.i("AddSubscriptionUseCase: base64 оказался ссылкой $decodedUrl")
                    return urlImport(decodedUrl)
                }
            }
        }

        return SubscriptionImportResult.Failure(SubscriptionImportReason.UNRECOGNIZED_CONTENT)
    }

    /** Дополняет стандартный профиль «Серверы» самодельными серверами. */
    private fun mergeIntoStandard(servers: List<Server>): SubscriptionImportResult {
        if (servers.isEmpty()) {
            return SubscriptionImportResult.Failure(SubscriptionImportReason.UNRECOGNIZED_CONTENT)
        }

        val merge = repository.mergeServersIntoStandardProfile(servers.distinctServers())
        return if (merge == null) {
            SubscriptionImportResult.Failure(SubscriptionImportReason.NO_NEW_SERVERS)
        } else {
            AppLog.i(
                "AddSubscriptionUseCase: в профиль «${merge.subscription.title}» " +
                    "добавлено=${merge.addedCount}, всего=${merge.subscription.servers.size}"
            )
            SubscriptionImportResult.Success(
                subscription = merge.subscription,
                created = false,
                serversCount = merge.subscription.servers.size,
                serversAdded = merge.addedCount,
            )
        }
    }

    private companion object {
        /** Защита от base64 внутри base64 без конца, как в [ImportDetector]. */
        private const val MAX_DEPTH = 3
    }
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
