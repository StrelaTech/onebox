package com.strela.onebox.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.subscription.Server
import com.strela.onebox.api.PingState
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.CircularProgressIndicator
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.Surface
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.Badge
import com.strela.onebox.ui.kit.IconTile

/**
 * Строка сервера: выбор, пинг и (опционально) удаление.
 * Используется и на главном экране, и в списке всех профилей.
 */
@Composable
fun ServerRow(
    server: Server,
    selected: Boolean,
    ping: PingState?,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    val colors = OneBoxTheme.colorScheme
    val s = strings

    Surface(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.secondaryContainer else colors.surfaceContainer,
        border = if (selected) BorderStroke(1.dp, colors.primary) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 10.dp, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconTile(
                icon = if (selected) OneBoxIcons.Check else OneBoxIcons.Globe,
                tint = if (selected) colors.primary else colors.onSurfaceVariantSummary,
                background = if (selected) colors.primaryContainer else colors.surfaceContainerHigh,
                size = 42.dp,
                iconSize = 21.dp,
                corner = 12.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = server.name,
                    style = OneBoxTheme.textStyles.headline2,
                    color = colors.onSurfaceContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Badge(server.protocol.displayName)
                    val transport = buildString {
                        append(server.network)
                        if (server.security != "none") append(" / ").append(server.security)
                    }
                    Text(
                        text = transport,
                        style = OneBoxTheme.textStyles.footnote2,
                        color = colors.onSurfaceContainerVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = server.endpoint,
                    style = OneBoxTheme.textStyles.footnote2.copy(fontFamily = FontFamily.Monospace),
                    color = colors.onSurfaceContainerVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            PingControl(ping = ping, onPing = onPing)

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = OneBoxIcons.Trash,
                        contentDescription = s.deleteServer(server.name),
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSurfaceContainerVariant,
                    )
                }
            }
        }
    }
}

/** Компактный индикатор пинга — кликабельная поверхность дизайн-кита. */
@Composable
private fun PingControl(ping: PingState?, onPing: () -> Unit) {
    val colors = OneBoxTheme.colorScheme
    val s = strings

    val label: String
    val color: Color
    val inProgress: Boolean
    when (ping) {
        is PingState.InProgress -> {
            label = "…"
            color = colors.primary
            inProgress = true
        }

        is PingState.Ok -> {
            label = s.pingMs(ping.millis)
            color = colors.primary
            inProgress = false
        }

        is PingState.Failed -> {
            label = s.pingFailed
            color = colors.error
            inProgress = false
        }

        null -> {
            label = s.ping
            color = colors.onSurfaceVariantSummary
            inProgress = false
        }
    }

    Surface(
        onClick = onPing,
        enabled = !inProgress,
        shape = RoundedCornerShape(9.dp),
        color = colors.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (inProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 1.8.dp,
                )
            } else {
                Icon(
                    imageVector = if (ping is PingState.Failed) {
                        OneBoxIcons.Close
                    } else {
                        OneBoxIcons.Zap
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = color,
                )
            }
            Text(
                text = label,
                style = OneBoxTheme.textStyles.footnote2,
                color = color,
            )
        }
    }
}
