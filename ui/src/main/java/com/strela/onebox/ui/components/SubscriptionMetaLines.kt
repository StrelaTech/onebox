package com.strela.onebox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.subscription.Subscription
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.strings
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Метаданные подписки под заголовком карточки: описание, дата окончания
 * и трафик (сколько потрачено и какой лимит). Линия появляется, только если
 * у подписки заполнены соответствующие данные.
 */
@Composable
fun SubscriptionMetaLines(
    subscription: Subscription,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    val s = strings
    val muted = colors.onSurfaceContainerVariant

    val hasMeta = subscription.description.isNotBlank() ||
        subscription.expireAtMillis != null ||
        subscription.usedBytes != null
    if (!hasMeta) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (subscription.description.isNotBlank()) {
            MetaLine(
                icon = OneBoxIcons.Info,
                text = subscription.description,
            )
        }

        subscription.expireAtMillis?.let { expireAt ->
            val expired = expireAt < System.currentTimeMillis()
            MetaLine(
                icon = OneBoxIcons.Clock,
                text = s.expiresAt(s.formatDate(expireAt)),
                color = if (expired) colors.error else muted,
            )
        }

        subscription.usedBytes?.let { used ->
            val text = subscription.trafficLimitBytes
                ?.takeIf { it > 0 }
                ?.let { limit -> s.trafficUsed(s.formatBytes(used), s.formatBytes(limit)) }
                ?: s.trafficUsedNoLimit(s.formatBytes(used))
            MetaLine(icon = OneBoxIcons.Download, text = text)
        }
    }
}

/** Строка метаданных: маленькая иконка + текст. */
@Composable
private fun MetaLine(
    icon: ImageVector,
    text: String,
    color: Color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = color,
        )
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote2,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
