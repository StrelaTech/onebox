package com.strela.onebox.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxColors
import com.strela.onebox.ui.OneBoxTheme

/**
 * Контейнер-поверхность на чистом Compose: скруглённый фон, рамка,
 * необязательный клик с индикацией нажатия из [LocalIndication].
 * Детям задаёт [LocalContentColor] по цвету заливки.
 */
@Composable
fun Surface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = OneBoxTheme.colorScheme.surfaceContainer,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = OneBoxTheme.colorScheme
    val contentColor = colors.contentColorFor(color).takeOrElse { colors.onBackground }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(elevation = shadowElevation, shape = shape)
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(color)
            .then(if (border != null) Modifier.border(border = border, shape = shape) else Modifier)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        enabled = enabled,
                        onClickLabel = onClickLabel,
                    ) { onClick() }
                } else {
                    Modifier
                },
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

/** Цвет содержимого под поверхность: контраст паре «заливка/текст». */
internal fun OneBoxColors.contentColorFor(container: Color): Color = when (container) {
    primary -> onPrimary
    secondary -> onSecondary
    primaryContainer -> onPrimaryContainer
    secondaryContainer -> onSecondaryContainer
    surfaceContainer, surfaceContainerHigh -> onSurfaceContainer
    else -> Color.Unspecified
}
