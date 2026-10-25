package com.strela.onebox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strela.onebox.subscription.Server
import com.strela.onebox.subscription.Subscription
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.components.SubscriptionImportDialog
import com.strela.onebox.ui.design.Button
import com.strela.onebox.ui.design.Card
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.Badge
import com.strela.onebox.ui.kit.IconTile
import com.strela.onebox.ui.kit.SectionTitle
import com.strela.onebox.xray.XrayController

/**
 * Экран «Серверы»: в начале страницы — все подписки со всеми их серверами,
 * в конце — кнопка «Добавить». Тип вставленного в диалог содержимого
 * определяется автоматически: URL — отдельный профиль, URI-ссылки и JSON —
 * стандартный профиль «Серверы», base64 — сначала расшифровывается
 * (см. AddSubscriptionUseCase).
 *
 * Список серверов разворачивается из шапки карточки: у активной подписки
 * он раскрыт, у остальных свернут («просто шапка»).
 */
@Composable
fun ServersScreen(
    subscriptions: List<Subscription>,
    selectedSubscriptionId: String?,
    pings: Map<String, XrayController.Ping>,
    refreshingUrls: Set<String>,
    onDeleteSubscription: (String) -> Unit,
    onSelectServer: (subscriptionId: String, serverId: String) -> Unit,
    onDeleteServer: (subscriptionId: String, serverId: String) -> Unit,
    onPingAll: (Subscription) -> Unit,
    onRefreshSubscription: (sourceUrl: String) -> Unit,
    onPing: (Server) -> Unit,
    onImportSubscription: suspend (text: String) -> SubscriptionImportResult,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val s = strings

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // В начале страницы — все подписки
        if (subscriptions.isEmpty()) {
            EmptySubscriptions()
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle(
                    text = s.allSubscriptions,
                    icon = OneBoxIcons.Folder,
                )
                Badge(
                    text = s.subscriptionsServersBadge(
                        subscriptions.size,
                        subscriptions.sumOf { it.servers.size },
                    ),
                    color = OneBoxTheme.colorScheme.onSurfaceContainerVariant,
                    background = OneBoxTheme.colorScheme.surfaceContainerHigh,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                subscriptions.forEach { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        servers = subscription.servers,
                        selected = subscription.id == selectedSubscriptionId,
                        pings = pings,
                        onSelectServer = { server -> onSelectServer(subscription.id, server.id) },
                        onPing = onPing,
                        onPingAll = { onPingAll(subscription) },
                        refreshing = subscription.url?.let { refreshingUrls.contains(it) } == true,
                        onRefresh = {
                            subscription.url?.let(onRefreshSubscription)
                        },
                        onDeleteSubscription = { onDeleteSubscription(subscription.id) },
                        onDeleteServer = { server -> onDeleteServer(subscription.id, server.id) },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // В конце страницы — кнопка «Добавить»
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = OneBoxIcons.Plus,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
            )
            Text(
                text = s.addSubscriptionAction,
                modifier = Modifier.padding(start = 10.dp),
                style = OneBoxTheme.textStyles.button,
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showAddDialog) {
        SubscriptionImportDialog(
            onDismiss = { showAddDialog = false },
            onImport = onImportSubscription,
        )
    }
}

@Composable
private fun EmptySubscriptions() {
    val colors = OneBoxTheme.colorScheme
    val s = strings

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconTile(
                icon = OneBoxIcons.Folder,
                tint = colors.onSurfaceContainerVariant,
                background = colors.surfaceContainerHigh,
                size = 60.dp,
                iconSize = 28.dp,
                corner = 18.dp,
            )
            Text(
                text = s.noSubscriptions,
                style = OneBoxTheme.textStyles.headline1,
                color = colors.onSurfaceContainer,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = s.noSubscriptionsHint,
                style = OneBoxTheme.textStyles.body2,
                color = colors.onSurfaceContainerVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
