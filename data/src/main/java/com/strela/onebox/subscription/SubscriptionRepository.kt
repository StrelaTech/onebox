package com.strela.onebox.subscription

import android.content.Context

/**
 * Результат слияния серверов с постоянным стандартным профилем
 * [Subscription.SERVERS_ID] «Серверы»: подписка со всем списком серверов
 * и то, сколько из переданных серверов оказались новыми.
 */
data class StandardProfileMerge(
    val subscription: Subscription,
    val addedCount: Int,
)

/**
 * Хранилище подписок по URL поверх [SubscriptionStore].
 *
 * Отделено от UseCase, чтобы сменить бэкенд (Room, синхронизация, кэш)
 * без изменения логики импорта. Единственная точка, где импорт подписок
 * трогает хранилище.
 */
interface SubscriptionRepository {

    /** Ищет уже импортированную подписку по её нормализованному URL. */
    fun findByUrl(url: String): Subscription?

    /**
     * Создаёт или обновляет подписку: при совпадении id серверы
     * заменяются свежими, метаданные (заголовок, описание, трафик, срок)
     * переписываются, выбор сервера сохраняется по rawUrl.
     */
    fun save(subscription: Subscription): Subscription

    /**
     * Дополняет стандартный профиль SERVERS_ID «Серверы» (все самодельные
     * серверы: URI-ссылки, расшифрованный base64, JSON-конфиги) — без
     * дубликатов. Возвращает null, если ни один сервер не оказался новым.
     */
    fun mergeServersIntoStandardProfile(servers: List<Server>): StandardProfileMerge?
}

/** Реализация на [SubscriptionStore] (SharedPreferences, JSON). */
class StoreSubscriptionRepository(context: Context) : SubscriptionRepository {

    private val appContext: Context = context.applicationContext

    override fun findByUrl(url: String): Subscription? = SubscriptionStore.findBySubscriptionUrl(url)

    override fun save(subscription: Subscription): Subscription =
        SubscriptionStore.upsertSubscription(appContext, subscription)

    override fun mergeServersIntoStandardProfile(servers: List<Server>): StandardProfileMerge? =
        SubscriptionStore.mergeServersIntoStandardProfile(appContext, servers)
}

/** Обёртка, чтобы repository работала с контекстом в любом месте (для DI/тестов). */
fun Context.subscriptionRepository(): SubscriptionRepository =
    StoreSubscriptionRepository(this)

/** Заглушка для тестов: хранит подписки в памяти. */
class InMemorySubscriptionRepository(
    initial: List<Subscription> = emptyList(),
) : SubscriptionRepository {

    private val subscriptions = initial.toMutableList()

    override fun findByUrl(url: String): Subscription? =
        subscriptions.firstOrNull { it.url == url }

    override fun save(subscription: Subscription): Subscription {
        val index = subscriptions.indexOfFirst { it.id == subscription.id }
        if (index >= 0) subscriptions[index] = subscription else subscriptions.add(subscription)
        return subscription
    }

    override fun mergeServersIntoStandardProfile(servers: List<Server>): StandardProfileMerge? {
        val existing = subscriptions.firstOrNull { it.id == Subscription.SERVERS_ID }
        val merged = (existing?.servers.orEmpty() + servers)
            .distinctByServer()
        val added = merged.size - (existing?.servers?.size ?: 0)
        if (added <= 0) return null

        val subscription = Subscription(
            id = Subscription.SERVERS_ID,
            title = existing?.title ?: "Servers",
            servers = merged,
            selectedServerId = merged.first().id,
        )
        save(subscription)
        return StandardProfileMerge(subscription = subscription, addedCount = added)
    }
}

/** Один и тот же сервер: совпадают протокол, адрес, порт и учётные данные. */
private fun Server.sameServerAs(other: Server): Boolean =
    protocol == other.protocol &&
        address == other.address &&
        port == other.port &&
        credential == other.credential

/** Убирает дубликаты серверов, сохраняя порядок и первое вхождение. */
private fun List<Server>.distinctByServer(): List<Server> {
    val unique = mutableListOf<Server>()
    for (server in this) {
        if (unique.none { it.sameServerAs(server) }) unique.add(server)
    }
    return unique
}
