package com.strela.onebox.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.subscription.Server
import com.strela.onebox.subscription.Subscription
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.Badge
import com.strela.onebox.ui.kit.IconTile
import com.strela.onebox.xray.XrayController
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Карточка одной подписки, сверху вниз:
 * шапка (заголовок → описание → дата окончания + кнопки → трафик) → список
 * серверов по одному.
 *
 * Шапка сворачивается по тапу: остаётся только заголовочная часть, список
 * серверов скрывается (см. [strings]). По умолчанию список показан у активной
 * подписки ([selected]) и скрыт у остальных; состояние тапов переживает
 * поворот экрана.
 *
 * Необязательные коллбэки [onPingAll], [onRefresh], [onDeleteSubscription],
 * [onDeleteServer]: null — соответствующая кнопка не рисуется.
 * [servers] — список серверов к показу: полный на вкладке «Серверы»,
 * только выбранный — на главном экране.
 */
@Composable
fun SubscriptionCard(
    subscription: Subscription,
    servers: List<Server>,
    selected: Boolean,
    pings: Map<String, XrayController.Ping>,
    onSelectServer: (Server) -> Unit,
    onPing: (Server) -> Unit,
    onPingAll: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    refreshing: Boolean = false,
    onDeleteSubscription: (() -> Unit)? = null,
    onDeleteServer: ((Server) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    val s = strings
    var expanded by rememberSaveable(subscription.id) { mutableStateOf(selected) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) colors.secondaryContainer else colors.surfaceContainer,
        border = if (selected) BorderStroke(1.dp, colors.primary) else null,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Шапка карточки: тап в любое место (кроме кнопок) сворачивает/разворачивает список
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = subscription.title,
                        style = MiuixTheme.textStyles.headline1,
                        color = colors.onSurfaceContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (selected) {
                        Badge(text = s.active, modifier = Modifier.padding(start = 7.dp))
                    }
                    Icon(
                        imageVector = if (expanded) OneBoxIcons.ArrowUp else OneBoxIcons.ArrowDown,
                        contentDescription = if (expanded) s.hideServers else s.showServers,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp),
                        tint = colors.onSurfaceContainerVariant,
                    )
                }

                // Описание из метаданных (заголовок Announce); панель не отправила
                // его — показываем источник (URL), откуда пришёл список.
                if (subscription.description.isNotBlank()) {
                    Text(
                        text = subscription.description,
                        style = MiuixTheme.textStyles.footnote2,
                        color = colors.onSurfaceContainerVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // Дата окончания из метаданных; справа — кнопки: пинг всех, обновление, удаление
                val pingingAll = subscription.servers.any { pings[it.id] is XrayController.Ping.InProgress }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = subscription.expireAtMillis
                            ?.let { expireAt -> s.expiresAt(s.formatDate(expireAt)) }
                            ?: s.noExpiry,
                        style = MiuixTheme.textStyles.footnote2,
                        color = if (subscription.expireAtMillis?.let { expireAt ->
                                expireAt < System.currentTimeMillis()
                            } == true
                        ) colors.error else colors.onSurfaceContainerVariant,
                        modifier = Modifier.weight(1f, fill = true),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (onPingAll != null && !subscription.isJson && servers.isNotEmpty()) {
                        PingAllButton(
                            inProgress = pingingAll,
                            modifier = Modifier.padding(start = 8.dp),
                            onClick = onPingAll,
                        )
                    }
                    if (onRefresh != null && subscription.url != null) {
                        IconButton(
                            onClick = onRefresh,
                            enabled = !refreshing,
                            modifier = Modifier.padding(start = 2.dp),
                        ) {
                            if (refreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    size = 18.dp,
                                    strokeWidth = 1.8.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = OneBoxIcons.Refresh,
                                    contentDescription = s.refreshSubscription,
                                    modifier = Modifier.size(20.dp),
                                    tint = colors.primary,
                                )
                            }
                        }
                    }
                    if (onDeleteSubscription != null) {
                        IconButton(
                            onClick = onDeleteSubscription,
                            modifier = Modifier.padding(start = 2.dp),
                        ) {
                            Icon(
                                imageVector = OneBoxIcons.Trash,
                                contentDescription = s.deleteSubscription(subscription.title),
                                modifier = Modifier.size(20.dp),
                                tint = colors.onSurfaceContainerVariant,
                            )
                        }
                    }
                }

                // Потрачено/лимит
                subscription.usedBytes?.let { used ->
                    val traffic = subscription.trafficLimitBytes
                        ?.takeIf { it > 0 }
                        ?.let { limit -> s.trafficUsed(s.formatBytes(used), s.formatBytes(limit)) }
                        ?: s.trafficUsedNoLimit(s.formatBytes(used))
                    Text(
                        text = traffic,
                        style = MiuixTheme.textStyles.footnote2,
                        color = colors.onSurfaceContainerVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            // Список серверов: скрывается вместе со свёрнутой шапкой
            if (expanded) {
                when {
                    subscription.isJson && servers.isEmpty() ->
                        Text(
                            text = s.configMode,
                            style = MiuixTheme.textStyles.footnote2,
                            color = colors.onSurfaceContainerVariant,
                            modifier = Modifier.padding(top = 12.dp, start = 2.dp),
                        )

                    servers.isEmpty() ->
                        Text(
                            text = s.noServersInSubscription,
                            style = MiuixTheme.textStyles.footnote2,
                            color = colors.onSurfaceContainerVariant,
                            modifier = Modifier.padding(top = 12.dp, start = 2.dp),
                        )

                    else -> Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        servers.forEach { server ->
                            ServerRow(
                                server = server,
                                selected = selected && server.id == subscription.selectedServer?.id,
                                ping = pings[server.id],
                                onSelect = { onSelectServer(server) },
                                onPing = { onPing(server) },
                                onDelete = onDeleteServer?.let { remove -> { remove(server) } },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Кнопка «Пинг всех»: замеряет задержку каждого сервера подписки. */
@Composable
private fun PingAllButton(
    inProgress: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    val s = strings

    Surface(
        onClick = onClick,
        enabled = !inProgress,
        shape = RoundedCornerShape(9.dp),
        color = colors.surfaceContainerHigh,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (inProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    size = 14.dp,
                    strokeWidth = 1.8.dp,
                )
            } else {
                Icon(
                    imageVector = OneBoxIcons.Zap,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colors.primary,
                )
            }
            Text(
                text = s.pingAll,
                style = MiuixTheme.textStyles.footnote2,
                color = colors.primary,
            )
        }
    }
}
