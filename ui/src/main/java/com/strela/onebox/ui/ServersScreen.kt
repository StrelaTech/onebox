package com.strela.onebox.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.subscription.ImportContent
import com.strela.onebox.subscription.ImportDetector
import com.strela.onebox.subscription.JsonConfig
import com.strela.onebox.subscription.Server
import com.strela.onebox.subscription.Subscription
import com.strela.onebox.subscription.SubscriptionImportResult
import com.strela.onebox.ui.components.SubscriptionImportDialog
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.Badge
import com.strela.onebox.ui.kit.IconTile
import com.strela.onebox.ui.kit.InlineMessage
import com.strela.onebox.ui.kit.SectionTitle
import com.strela.onebox.xray.XrayController
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Экран «Серверы»: единый импорт (ссылки / JSON / base64) и полный список
 * подписок со всеми их серверами. Формат вставленного текста определяется
 * автоматически. Всё, что пользователь импортировал вручную (ссылки, base64,
 * JSON-серверы), попадает в одну постоянную подписку «Серверы».
 */
@Composable
fun ServersScreen(
    subscriptions: List<Subscription>,
    selectedSubscriptionId: String?,
    pings: Map<String, XrayController.Ping>,
    refreshingUrls: Set<String>,
    onImportServers: (servers: List<Server>) -> Int,
    onImportConfig: (configs: List<JsonConfig>, subscriptionTitle: String) -> Int,
    onDeleteSubscription: (String) -> Unit,
    onSelectServer: (subscriptionId: String, serverId: String) -> Unit,
    onDeleteServer: (subscriptionId: String, serverId: String) -> Unit,
    onPingAll: (Subscription) -> Unit,
    onRefreshSubscription: (sourceUrl: String) -> Unit,
    onPing: (Server) -> Unit,
    onImportSubscription: suspend (text: String, title: String) -> SubscriptionImportResult,
    modifier: Modifier = Modifier,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var subscriptionTitle by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<ImportMessage?>(null) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    val s = strings

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        ImportPanel(
            input = input,
            onInputChange = {
                input = it
                message = null
            },
            subscriptionTitle = subscriptionTitle,
            onSubscriptionTitleChange = { subscriptionTitle = it },
            message = message,
            onImport = {
                when (val content = ImportDetector.detect(input)) {
                    is ImportContent.Servers -> {
                        // ручной импорт всегда попадает в подписку «Серверы»
                        val added = onImportServers(content.servers)
                        message = if (added > 0) {
                            ImportMessage(s.addedServers(added), ImportMessage.Tone.SUCCESS)
                        } else {
                            ImportMessage(s.noNewServers, ImportMessage.Tone.ERROR)
                        }
                        if (added > 0) {
                            input = ""
                            subscriptionTitle = ""
                        }
                    }

                    is ImportContent.Config -> {
                        // один объект — одна подписка, массив — каждый конфиг отдельно
                        val added = onImportConfig(content.configs, subscriptionTitle)
                        message = when {
                            added <= 0 -> ImportMessage(s.noConfigImported, ImportMessage.Tone.ERROR)
                            added == 1 -> ImportMessage(s.configImported, ImportMessage.Tone.SUCCESS)
                            else -> ImportMessage(s.configsImported(added), ImportMessage.Tone.SUCCESS)
                        }
                        if (added > 0) {
                            input = ""
                            subscriptionTitle = ""
                        }
                    }

                    null -> message = ImportMessage(
                        text = if (input.isBlank()) s.pasteLinkOrLines else s.unrecognizedInput,
                        tone = if (input.isBlank()) ImportMessage.Tone.INFO
                        else ImportMessage.Tone.ERROR,
                    )
                }
            },
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { showSubscriptionDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = OneBoxIcons.Globe,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
            )
            Text(
                text = s.importSubscriptionAction,
                modifier = Modifier.padding(start = 10.dp),
                style = MiuixTheme.textStyles.button,
            )
        }

        Spacer(Modifier.height(26.dp))

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
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    background = MiuixTheme.colorScheme.surfaceContainerHigh,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                subscriptions.forEach { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        selected = subscription.id == selectedSubscriptionId,
                        refreshing = subscription.url?.let { refreshingUrls.contains(it) } == true,
                        pings = pings,
                        onDeleteSubscription = { onDeleteSubscription(subscription.id) },
                        onSelectServer = { server -> onSelectServer(subscription.id, server.id) },
                        onDeleteServer = { server -> onDeleteServer(subscription.id, server.id) },
                        onPingAll = { onPingAll(subscription) },
                        onRefresh = {
                            subscription.url?.let(onRefreshSubscription)
                        },
                        onPing = onPing,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showSubscriptionDialog) {
        SubscriptionImportDialog(
            onDismiss = { showSubscriptionDialog = false },
            onImport = onImportSubscription,
        )
    }
}

@Composable
private fun ImportPanel(
    input: String,
    onInputChange: (String) -> Unit,
    subscriptionTitle: String,
    onSubscriptionTitleChange: (String) -> Unit,
    message: ImportMessage?,
    onImport: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val s = strings

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(22.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconTile(
                icon = OneBoxIcons.Link,
                tint = colors.primary,
                background = colors.primaryContainer,
                size = 44.dp,
                iconSize = 22.dp,
                corner = 13.dp,
            )
            Text(
                text = s.addServers,
                style = MiuixTheme.textStyles.headline1,
                color = colors.onSurfaceContainer,
            )
        }

        Spacer(Modifier.height(20.dp))

        // Заголовок используется только JSON-подписками: ручной импорт ссылок
        // всегда попадает в постоянную подписку «Серверы»
        TextField(
            value = subscriptionTitle,
            onValueChange = onSubscriptionTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = s.subscriptionTitleOptional,
            useLabelAsPlaceholder = true,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = OneBoxIcons.Tag,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.onSecondaryContainer,
                )
            },
        )

        Spacer(Modifier.height(14.dp))

        TextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = "Введите ссылку...",
            useLabelAsPlaceholder = true,
            minLines = 6,
            maxLines = 10,
            textStyle = MiuixTheme.textStyles.body2.copy(fontFamily = FontFamily.Monospace),
            leadingIcon = {
                Icon(
                    imageVector = OneBoxIcons.Link,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.onSecondaryContainer,
                )
            },
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onImport,
                modifier = Modifier.weight(1f),
                enabled = input.isNotBlank(),
            ) {
                Icon(
                    imageVector = OneBoxIcons.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
                Text(
                    text = s.importAction,
                    modifier = Modifier.padding(start = 10.dp),
                    style = MiuixTheme.textStyles.button,
                )
            }
            if (input.isNotBlank()) {
                IconButton(onClick = { onInputChange("") }) {
                    Icon(
                        imageVector = OneBoxIcons.Close,
                        contentDescription = s.clearField,
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSurfaceContainerVariant,
                    )
                }
            }
        }

        message?.let { msg ->
            Spacer(Modifier.height(14.dp))
            val color = when (msg.tone) {
                ImportMessage.Tone.SUCCESS -> colors.primary
                ImportMessage.Tone.INFO -> colors.onSurfaceContainerVariant
                ImportMessage.Tone.ERROR -> colors.error
            }
            val icon = when (msg.tone) {
                ImportMessage.Tone.SUCCESS -> OneBoxIcons.Check
                ImportMessage.Tone.INFO -> OneBoxIcons.Info
                ImportMessage.Tone.ERROR -> OneBoxIcons.Alert
            }
            InlineMessage(text = msg.text, icon = icon, color = color)
        }
    }
}

/**
 * Карточка одной подписки, сверху вниз:
 * заголовок → описание (Announce из метаданных) → дата окончания +
 * кнопки (пинг всех / обновление / удаление) →
 * потрачено/лимит трафика → все серверы по одному.
 *
 * Всё, кроме кнопок, — обычный текст без кликов: шапка не выглядит как кнопка,
 * а подписка выбирается выбором сервера внутри неё.
 */
@Composable
private fun SubscriptionCard(
    subscription: Subscription,
    selected: Boolean,
    refreshing: Boolean,
    pings: Map<String, XrayController.Ping>,
    onDeleteSubscription: () -> Unit,
    onSelectServer: (Server) -> Unit,
    onDeleteServer: (Server) -> Unit,
    onPingAll: () -> Unit,
    onRefresh: () -> Unit,
    onPing: (Server) -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val s = strings

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) colors.secondaryContainer else colors.surfaceContainer,
        border = if (selected) BorderStroke(1.dp, colors.primary) else null,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок из метаданных (+ бейдж активной подписки)
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
                if (!subscription.isJson && subscription.servers.isNotEmpty()) {
                    PingAllButton(
                        inProgress = pingingAll,
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = onPingAll,
                    )
                }
                if (subscription.url != null) {
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

            // Все серверы подписки — по одному
            when {
                subscription.isJson && subscription.servers.isEmpty() ->
                    Text(
                        text = s.configMode,
                        style = MiuixTheme.textStyles.footnote2,
                        color = colors.onSurfaceContainerVariant,
                        modifier = Modifier.padding(top = 12.dp, start = 2.dp),
                    )

                subscription.servers.isEmpty() ->
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
                    subscription.servers.forEach { server ->
                        ServerRow(
                            server = server,
                            selected = selected && server.id == subscription.selectedServer?.id,
                            ping = pings[server.id],
                            onSelect = { onSelectServer(server) },
                            onPing = { onPing(server) },
                            onDelete = { onDeleteServer(server) },
                        )
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

@Composable
private fun EmptySubscriptions() {
    val colors = MiuixTheme.colorScheme
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
                style = MiuixTheme.textStyles.headline1,
                color = colors.onSurfaceContainer,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = s.noSubscriptionsHint,
                style = MiuixTheme.textStyles.body2,
                color = colors.onSurfaceContainerVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Сообщение под кнопкой импорта. */
private data class ImportMessage(val text: String, val tone: Tone) {
    enum class Tone { SUCCESS, INFO, ERROR }
}
