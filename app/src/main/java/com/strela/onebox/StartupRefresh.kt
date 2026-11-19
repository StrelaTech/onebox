package com.strela.onebox

import android.content.Context
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.HttpSubscriptionFetcher
import com.strela.onebox.subscription.ImportSubscriptionByUrlUseCase
import com.strela.onebox.subscription.StoreSubscriptionRepository
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.subscription.SubscriptionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Фоновая синхронизация при запуске: обновляет все URL-подписки и заново
 * пингует серверы. Один общий поток на процесс — повторные вызовы (запуск +
 * boot) не плодят параллельные обновления.
 */
object StartupRefresh {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Обновление при открытии приложения. */
    fun onLaunch(context: Context) = refresh(context, "launch")

    /** Обновление после загрузки устройства. */
    fun onBoot(context: Context) = refresh(context, "boot")

    fun refresh(context: Context, source: String) {
        scope.launch { refreshNow(context, source) }
    }

    /** Обновление подписок и пинг; suspend-версия для WorkManager. */
    suspend fun refreshNow(context: Context, source: String) {
        val subscriptions = SubscriptionStore.subscriptions.value
        val urlSubscriptions = subscriptions.filter { !it.url.isNullOrBlank() }
        if (urlSubscriptions.isEmpty()) {
            AppLog.d("StartupRefresh($source): URL-подписок нет, обновление пропущено")
        } else {
            val repository = StoreSubscriptionRepository(context)
            val useCase = ImportSubscriptionByUrlUseCase(
                fetcher = HttpSubscriptionFetcher(),
                repository = repository,
            )
            var updated = 0
            for (subscription in urlSubscriptions) {
                val url = subscription.url ?: continue
                when (val result = useCase(url)) {
                    is SubscriptionImportResult.Success -> {
                        updated++
                        AppLog.i(
                            "StartupRefresh($source): подписка «${result.subscription.title}» " +
                                "обновлена, серверов=${result.serversCount}"
                        )
                    }

                    is SubscriptionImportResult.Failure -> AppLog.w(
                        "StartupRefresh($source): подписка «${subscription.title}» " +
                            "не обновлена: ${result.reason}"
                    )
                }
            }
            if (updated > 0) VpnConnector.refreshFromProfile()
            AppLog.i("StartupRefresh($source): обновлено подписок=$updated из ${urlSubscriptions.size}")
        }

        pingAllServers(source)
    }

    /** Пингует все серверы всех подписок (ядро само решает, как мерить). */
    fun pingAllServers(source: String) {
        val servers = SubscriptionStore.subscriptions.value.flatMap { it.servers }
        if (servers.isEmpty()) return
        AppLog.i("StartupRefresh($source): пингую серверов=${servers.size}")
        servers.forEach { VpnConnector.pingServer(it) }
    }
}
