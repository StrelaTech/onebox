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
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.subscription.AddSubscriptionUseCase
import com.strela.onebox.subscription.HttpSubscriptionFetcher
import com.strela.onebox.subscription.ImportSubscriptionByUrlUseCase
import com.strela.onebox.subscription.StoreSubscriptionRepository
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.ui.ServersScreen
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
    val pings by VpnConnector.pings.collectAsState()

    val repository = remember(context) { StoreSubscriptionRepository(context) }
    val importSubscription = remember(context) {
        // обновление уже знакомых URL-подписок с карточки
        ImportSubscriptionByUrlUseCase(
            fetcher = HttpSubscriptionFetcher(),
            repository = repository,
        )
    }
    val addSubscription = remember(context) {
        // кнопка «Добавить»: тип вставленного текста определяется автоматически —
        // URL даёт отдельный профиль, URI/JSON идут в стандартный профиль
        // «Серверы», base64 сначала расшифровывается
        AddSubscriptionUseCase(
            fetcher = HttpSubscriptionFetcher(),
            repository = repository,
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
                        VpnConnector.refreshFromProfile()
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
        onDeleteSubscription = { id ->
            SubscriptionStore.removeSubscription(context, id)
            VpnConnector.refreshFromProfile()
        },
        onSelectServer = { subscriptionId, serverId ->
            SubscriptionStore.selectServer(context, subscriptionId, serverId)
            VpnConnector.refreshFromProfile()
        },
        onDeleteServer = { subscriptionId, serverId ->
            SubscriptionStore.removeServer(context, subscriptionId, serverId)
            VpnConnector.refreshFromProfile()
        },
        onPingAll = { subscription ->
            subscription.servers.forEach { VpnConnector.pingServer(it) }
        },
        onRefreshSubscription = refresh,
        onPing = { server -> VpnConnector.pingServer(server) },
        onImportSubscription = { text ->
            val result = addSubscription(text)
            when (result) {
                is SubscriptionImportResult.Success -> {
                    AppLog.i(
                        "ServersTab: добавлено в «${result.subscription.title}», " +
                            "новых серверов=${result.serversAdded}"
                    )
                    // новая подписка могла стать активной — перечитываем конфиг ядра
                    VpnConnector.refreshFromProfile()
                }

                is SubscriptionImportResult.Failure -> {
                    AppLog.w("ServersTab: импорт не удался: ${result.reason}")
                }
            }
            result
        },
        modifier = modifier,
    )
}
