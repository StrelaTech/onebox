package com.strela.onebox.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.HttpSubscriptionFetcher
import com.strela.onebox.subscription.ImportContent
import com.strela.onebox.subscription.ImportDetector
import com.strela.onebox.subscription.ImportSubscriptionByUrlUseCase
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.subscription.StoreSubscriptionRepository
import com.strela.onebox.subscription.SubscriptionImportReason
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.subscription.SubscriptionUrl
import com.strela.onebox.ui.ServersScreen
import com.strela.onebox.xray.XrayController
import kotlinx.coroutines.launch

/**
 * Вкладка «Серверы»: подключает [ServersScreen] к хранилищу подписок и ядру Xray.
 * Вся бизнес-логика импорта здесь — экран остаётся «глупым».
 */
@Composable
fun ServersTab(context: Context, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val subscriptions by SubscriptionStore.subscriptions.collectAsState()
    val selectedSubscriptionId by SubscriptionStore.selectedSubscriptionId.collectAsState()
    val pings by XrayController.pings.collectAsState()

    val importSubscription = remember(context) {
        ImportSubscriptionByUrlUseCase(
            fetcher = HttpSubscriptionFetcher(),
            repository = StoreSubscriptionRepository(context),
        )
    }

    // URL подписок, которые сейчас обновляются — для индикатора на карточке
    var refreshingUrls by remember { mutableStateOf<Set<String>>(emptySet()) }
    val refresh: (String) -> Unit = { url ->
        scope.launch {
            refreshingUrls = refreshingUrls + url
            try {
                val result = importSubscription(url)
                when (result) {
                    is SubscriptionImportResult.Success -> {
                        AppLog.i(
                            "ServersTab: подписка «${result.subscription.title}» обновлена, " +
                                "серверов=${result.serversCount}"
                        )
                        // обновлённая подписка могла стать активной — перечитываем конфиг ядра
                        XrayController.refreshFromProfile()
                    }

                    is SubscriptionImportResult.Failure -> {
                        AppLog.w("ServersTab: не удалось обновить подписку $url: ${result.reason}")
                    }
                }
            } finally {
                refreshingUrls = refreshingUrls - url
            }
        }
    }

    ServersScreen(
        subscriptions = subscriptions,
        selectedSubscriptionId = selectedSubscriptionId,
        pings = pings,
        refreshingUrls = refreshingUrls,
        onImportServers = { servers ->
            AppLog.i("ServersTab: распознаны ссылки, серверов=${servers.size}")
            val added = SubscriptionStore.importServers(context, servers)?.servers?.size ?: 0
            if (added > 0) XrayController.refreshFromProfile()
            added
        },
        onImportConfig = { configs, subscriptionTitle ->
            AppLog.i("ServersTab: распознано JSON-конфигов=${configs.size}")
            // все конфиги попадают в ОДНУ подписку — каждый конфиг становится сервером
            val added = SubscriptionStore.importJsons(context, configs, subscriptionTitle)
                .firstOrNull()?.servers?.size ?: 0
            if (added > 0) XrayController.refreshFromProfile()
            added
        },
        onDeleteSubscription = { id ->
            SubscriptionStore.removeSubscription(context, id)
            XrayController.refreshFromProfile()
        },
        onSelectServer = { subscriptionId, serverId ->
            SubscriptionStore.selectServer(context, subscriptionId, serverId)
            XrayController.refreshFromProfile()
        },
        onDeleteServer = { subscriptionId, serverId ->
            SubscriptionStore.removeServer(context, subscriptionId, serverId)
            XrayController.refreshFromProfile()
        },
        onPingAll = { subscription ->
            subscription.servers.forEach { XrayController.pingServer(it) }
        },
        onRefreshSubscription = refresh,
        onPing = { server -> XrayController.pingServer(server) },
        onImportSubscription = { text, title ->
            // Ссылка https:// — импорт по URL; всё остальное, что удалось
            // распознать (JSON-конфиги, список ссылок, base64), — отдельная подписка
            // из вставленного содержимого.
            val result = when {
                SubscriptionUrl.validateOrNull(text) != null -> importSubscription(text)

                else -> when (val content = ImportDetector.detect(text)) {
                    is ImportContent.Config -> {
                        SubscriptionStore.importJsons(context, content.configs, title)
                            .firstOrNull()
                            ?.let { SubscriptionImportResult.Success(it, created = true, serversCount = it.servers.size) }
                            ?: SubscriptionImportResult.Failure(SubscriptionImportReason.EMPTY_CONTENT)
                    }

                    is ImportContent.Servers -> {
                        SubscriptionStore.importServersAsSubscription(context, content.servers, title)
                            ?.let { SubscriptionImportResult.Success(it, created = true, serversCount = it.servers.size) }
                            ?: SubscriptionImportResult.Failure(SubscriptionImportReason.EMPTY_CONTENT)
                    }

                    null -> SubscriptionImportResult.Failure(SubscriptionImportReason.INVALID_URL)
                }
            }
            when (result) {
                is SubscriptionImportResult.Success -> {
                    AppLog.i(
                        "ServersTab: подписка «${result.subscription.title}» " +
                            "${if (result.created) "добавлена" else "обновлена"}, " +
                            "серверов=${result.serversCount}"
                    )
                    // обновлённая подписка могла стать активной — перечитываем конфиг ядра
                    XrayController.refreshFromProfile()
                }

                is SubscriptionImportResult.Failure -> {
                    AppLog.w("ServersTab: импорт подписки не удался: ${result.reason}")
                }
            }
            result
        },
        modifier = modifier,
    )
}
