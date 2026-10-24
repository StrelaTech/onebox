package com.strela.onebox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.subscription.Server
import com.strela.onebox.subscription.Subscription
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.ui.ServerRow
import com.strela.onebox.ui.components.SubscriptionMetaLines
import com.strela.onebox.ui.components.statusColor
import com.strela.onebox.ui.components.statusDescription
import com.strela.onebox.ui.components.statusTitle
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.IconTile
import com.strela.onebox.ui.kit.SectionTitle
import com.strela.onebox.ui.strings
import com.strela.onebox.xray.XrayController
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Экран «Подключение»: большая кнопка VPN + серверы активной подписки.
 * Только отображение — управление VPN остаётся в :app (MainActivity).
 */
@Composable
fun ConnectScreen(
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val status by XrayController.status.collectAsState()
    val subscriptions by SubscriptionStore.subscriptions.collectAsState()
    val selectedSubscriptionId by SubscriptionStore.selectedSubscriptionId.collectAsState()
    val pings by XrayController.pings.collectAsState()
    val selected = remember(subscriptions, selectedSubscriptionId) {
        subscriptions.firstOrNull { it.id == selectedSubscriptionId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp))
        ConnectHero(
            status = status,
            onStartVpn = onStartVpn,
            onStopVpn = onStopVpn,
        )

        Spacer(Modifier.height(20.dp))

        SubscriptionServersSection(
            subscription = selected,
            hasSubscriptions = subscriptions.isNotEmpty(),
            pings = pings,
            onOpenSubscriptions = onOpenSubscriptions,
            onSelectServer = { server ->
                selected?.let {
                    SubscriptionStore.selectServer(context, it.id, server.id)
                    XrayController.refreshFromProfile()
                }
            },
            onPing = { server -> XrayController.pingServer(server) },
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ConnectHero(
    status: XrayController.Status,
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 22.dp, vertical = 26.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Surface(
                    modifier = Modifier.size(9.dp),
                    shape = CircleShape,
                    color = statusColor(status),
                ) {}
                Text(
                    text = statusTitle(status),
                    style = MiuixTheme.textStyles.footnote1,
                    color = statusColor(status),
                )
            }

            Spacer(Modifier.height(20.dp))

            BigConnectButton(
                status = status,
                onStartVpn = onStartVpn,
                onStopVpn = onStopVpn,
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = statusDescription(status),
                style = MiuixTheme.textStyles.body2,
                color = colors.onSurfaceContainerVariant,
                textAlign = TextAlign.Center,
            )

            if (status is XrayController.Status.Error) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.errorContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = OneBoxIcons.Alert,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(14.dp),
                            tint = colors.error,
                        )
                        Text(
                            text = status.message,
                            style = MiuixTheme.textStyles.footnote2.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = colors.error,
                        )
                    }
                }
            }
        }
    }
}

/** Главная кнопка: круг с иконкой и подписью, цвет зависит от состояния VPN. */
@Composable
private fun BigConnectButton(
    status: XrayController.Status,
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val s = strings
    val starting = status is XrayController.Status.Starting

    val fill = when (status) {
        is XrayController.Status.Running -> RunningColor
        is XrayController.Status.Starting -> StartingColor
        is XrayController.Status.Error -> colors.error
        is XrayController.Status.Stopped -> colors.primary
    }
    val contentColor = if (status is XrayController.Status.Stopped) colors.onPrimary else Color.White
    val label = when (status) {
        is XrayController.Status.Running -> s.disconnect
        is XrayController.Status.Starting -> s.starting
        is XrayController.Status.Error -> s.retry
        is XrayController.Status.Stopped -> s.connect
    }
    val icon = when (status) {
        is XrayController.Status.Running -> OneBoxIcons.Pause
        is XrayController.Status.Starting -> OneBoxIcons.Refresh
        is XrayController.Status.Error -> OneBoxIcons.Refresh
        is XrayController.Status.Stopped -> OneBoxIcons.Play
    }

    Surface(
        onClick = {
            if (status is XrayController.Status.Running) onStopVpn() else onStartVpn()
        },
        enabled = !starting,
        modifier = Modifier.size(216.dp),
        shape = CircleShape,
        color = fill,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (starting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(38.dp),
                        size = 38.dp,
                        strokeWidth = 3.dp,
                        colors = ProgressIndicatorDefaults.progressIndicatorColors(
                            foregroundColor = contentColor,
                            backgroundColor = Color.Transparent,
                        ),
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = contentColor,
                    )
                }
                Text(
                    text = label,
                    style = MiuixTheme.textStyles.headline2,
                    color = contentColor,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

/** Список серверов активной подписки — сразу под большой кнопкой подключения. */
@Composable
private fun SubscriptionServersSection(
    subscription: Subscription?,
    hasSubscriptions: Boolean,
    pings: Map<String, XrayController.Ping>,
    onOpenSubscriptions: () -> Unit,
    onSelectServer: (Server) -> Unit,
    onPing: (Server) -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val s = strings

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionTitle(
            text = s.subscriptionServers,
            icon = OneBoxIcons.Globe,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onOpenSubscriptions) {
            Icon(
                imageVector = OneBoxIcons.Folder,
                contentDescription = s.chooseSubscription,
                modifier = Modifier.size(22.dp),
                tint = colors.primary,
            )
        }
    }

    Spacer(Modifier.height(14.dp))

    if (subscription == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(horizontal = 22.dp, vertical = 28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconTile(
                    icon = if (hasSubscriptions) OneBoxIcons.Code else OneBoxIcons.Folder,
                    tint = if (hasSubscriptions) colors.primary else colors.onSurfaceContainerVariant,
                    background = if (hasSubscriptions) colors.primaryContainer else colors.surfaceContainerHigh,
                    size = 56.dp,
                    iconSize = 26.dp,
                    corner = 17.dp,
                )
                Text(
                    text = if (hasSubscriptions) s.configMode else s.noSubscriptions,
                    style = MiuixTheme.textStyles.headline1,
                    color = colors.onSurfaceContainer,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = if (hasSubscriptions) s.configModeHint else s.noSubscriptionsHint,
                    style = MiuixTheme.textStyles.footnote2,
                    color = colors.onSurfaceContainerVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onOpenSubscriptions,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (hasSubscriptions) OneBoxIcons.Folder else OneBoxIcons.Plus,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                    )
                    Text(
                        text = if (hasSubscriptions) s.chooseSubscription else s.importAction,
                        style = MiuixTheme.textStyles.button,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
        }
        return
    }

    Surface(
        onClick = onOpenSubscriptions,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(
                    icon = OneBoxIcons.Folder,
                    tint = colors.primary,
                    background = colors.primaryContainer,
                    size = 44.dp,
                    iconSize = 22.dp,
                    corner = 13.dp,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 8.dp),
                ) {
                    Text(
                        text = subscription.title,
                        style = MiuixTheme.textStyles.headline1,
                        color = colors.onSurfaceContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (subscription.isJson) s.configMode
                        else s.serversCount(subscription.servers.size),
                        style = MiuixTheme.textStyles.footnote2,
                        color = colors.onSurfaceContainerVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Icon(
                    imageVector = OneBoxIcons.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.onSurfaceContainerVariant,
                )
            }

            SubscriptionMetaLines(
                subscription = subscription,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }

    Spacer(Modifier.height(14.dp))

    if (subscription.servers.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(22.dp),
        ) {
            Text(
                text = if (subscription.isJson) s.configModeHint else s.noServersInSubscription,
                style = MiuixTheme.textStyles.footnote2,
                color = colors.onSurfaceContainerVariant,
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            subscription.servers.forEach { server ->
                ServerRow(
                    server = server,
                    selected = server.id == subscription.selectedServer?.id,
                    ping = pings[server.id],
                    onSelect = { onSelectServer(server) },
                    onPing = { onPing(server) },
                )
            }
        }
    }
}

private val RunningColor = Color(0xFF34C759)
private val StartingColor = Color(0xFFFF9F0A)
