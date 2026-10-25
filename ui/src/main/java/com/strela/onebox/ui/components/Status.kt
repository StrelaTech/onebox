package com.strela.onebox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.Surface
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.strings
import com.strela.onebox.xray.XrayController

/**
 * Визуальное состояние VPN: цвет и тексты для пилюли в шапке и карточки подключения.
 * Общие для :ui компоненты — экраны не должны дублировать маппинг статуса.
 */

private val RunningColor = Color(0xFF34C759)
private val StartingColor = Color(0xFFFF9F0A)

fun statusColor(status: XrayController.Status): Color = when (status) {
    is XrayController.Status.Stopped -> Color(0xFF8E8E93)
    is XrayController.Status.Starting -> StartingColor
    is XrayController.Status.Running -> RunningColor
    is XrayController.Status.Error -> Color(0xFFFF3B30)
}

/** Короткая метка статуса для пилюли в шапке. */
@Composable
fun statusShort(status: XrayController.Status): String = when (status) {
    is XrayController.Status.Stopped -> strings.statusStoppedShort
    is XrayController.Status.Starting -> strings.statusStartingShort
    is XrayController.Status.Running -> strings.statusRunningShort
    is XrayController.Status.Error -> strings.statusErrorShort
}

/** Полный заголовок статуса для карточки подключения. */
@Composable
fun statusTitle(status: XrayController.Status): String = when (status) {
    is XrayController.Status.Stopped -> strings.statusStoppedTitle
    is XrayController.Status.Starting -> strings.statusStartingTitle
    is XrayController.Status.Running -> strings.statusRunningTitle
    is XrayController.Status.Error -> strings.statusErrorTitle
}

/** Описание статуса для карточки подключения. */
@Composable
fun statusDescription(status: XrayController.Status): String = when (status) {
    is XrayController.Status.Stopped -> strings.statusStoppedDescription
    is XrayController.Status.Starting -> strings.statusStartingDescription
    is XrayController.Status.Running -> strings.statusRunningDescription
    is XrayController.Status.Error -> "Проверьте конфиг и посмотрите логи"
}

/** Мини-пилюля с индикатором и подписью состояния VPN — в шапке приложения. */
@Composable
fun StatusPill(status: XrayController.Status, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Surface(
                modifier = Modifier.size(6.dp),
                shape = CircleShape,
                color = color,
            ) {}
            Text(
                text = statusShort(status),
                style = OneBoxTheme.textStyles.footnote2,
                color = color,
            )
        }
    }
}
