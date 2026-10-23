package com.strela.onebox.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.i18n.strings
import com.strela.onebox.profile.Server
import com.strela.onebox.ui.design.Badge
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.IconTile
import com.strela.onebox.ui.design.LocalOneBoxTypography
import com.strela.onebox.ui.design.Palette
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.xray.XrayController

/**
 * Строка сервера: выбор, пинг и (опционально) удаление.
 * Используется и на главном экране, и в списке всех профилей.
 */
@Composable
fun ServerRow(
    server: Server,
    selected: Boolean,
    ping: XrayController.Ping?,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val typo = LocalOneBoxTypography.current
    val s = strings
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (selected) Palette.AccentPrimary else Palette.Border

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Palette.SurfaceElevated else Palette.Surface)
            .border(BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor), shape)
            .clickable(onClick = onSelect)
            .padding(start = 9.dp, end = 5.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon = if (selected) OneBoxIcons.Check else OneBoxIcons.Globe,
            size = 34.dp,
            iconSize = 17.dp,
            tint = if (selected) Palette.Running else Palette.TextSecondary,
            background = if (selected) Palette.RunningSoft else Palette.SurfaceElevated,
            corner = 10.dp
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 6.dp)
        ) {
            BasicText(
                text = server.name,
                style = typo.heading.copy(color = Palette.TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp)
            ) {
                Badge(server.protocol.displayName)
                val transport = buildString {
                    append(server.network)
                    if (server.security != "none") append(" / ").append(server.security)
                }
                BasicText(
                    text = transport,
                    style = typo.caption.copy(color = Palette.TextMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .weight(1f, fill = false)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    icon = OneBoxIcons.Link,
                    modifier = Modifier.size(10.dp),
                    tint = Palette.TextMuted
                )
                BasicText(
                    text = server.endpoint,
                    style = typo.mono.copy(color = Palette.TextMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }

        PingControl(ping = ping, onPing = onPing)

        if (onDelete != null) {
            IconButton(
                icon = OneBoxIcons.Trash,
                contentDescription = s.deleteServer(server.name),
                onClick = onDelete,
                tint = Palette.TextMuted,
                size = 32.dp,
                iconSize = 16.dp
            )
        }
    }
}

/** Кнопка «Пинг» либо результат в зависимости от состояния. */
@Composable
private fun PingControl(ping: XrayController.Ping?, onPing: () -> Unit) {
    val typo = LocalOneBoxTypography.current
    val s = strings

    val label: String
    val color: Color
    val icon: ImageVector
    val clickable: Boolean
    when (ping) {
        is XrayController.Ping.InProgress -> {
            label = "…"
            color = Palette.Starting
            icon = OneBoxIcons.Refresh
            clickable = false
        }
        is XrayController.Ping.Ok -> {
            label = s.pingMs(ping.millis)
            color = Palette.Running
            icon = OneBoxIcons.Zap
            clickable = true
        }
        is XrayController.Ping.Failed -> {
            label = s.pingFailed
            color = Palette.Error
            icon = OneBoxIcons.Close
            clickable = true
        }
        null -> {
            label = s.ping
            color = Palette.TextSecondary
            icon = OneBoxIcons.Zap
            clickable = true
        }
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Palette.SurfaceElevated)
            .then(if (clickable) Modifier.clickable(onClick = onPing) else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (ping is XrayController.Ping.InProgress) {
            SpinningIcon(icon = icon, tint = color)
        } else {
            Icon(
                icon = icon,
                modifier = Modifier.size(12.dp),
                tint = color
            )
        }
        BasicText(
            text = label,
            style = typo.caption.copy(color = color)
        )
    }
}

/** Вращающаяся иконка — индикатор пинга в процессе. */
@Composable
private fun SpinningIcon(icon: ImageVector, tint: Color) {
    val transition = rememberInfiniteTransition(label = "ping")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "pingAngle"
    )
    Icon(
        icon = icon,
        modifier = Modifier
            .size(12.dp)
            .graphicsLayer { rotationZ = angle },
        tint = tint
    )
}
