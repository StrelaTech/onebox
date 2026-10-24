package com.strela.onebox.subscription

import android.content.Context

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
}

/** Реализация на [SubscriptionStore] (SharedPreferences, JSON). */
class StoreSubscriptionRepository(context: Context) : SubscriptionRepository {

    private val appContext: Context = context.applicationContext

    override fun findByUrl(url: String): Subscription? = SubscriptionStore.findBySubscriptionUrl(url)

    override fun save(subscription: Subscription): Subscription =
        SubscriptionStore.upsertSubscription(appContext, subscription)
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
}
