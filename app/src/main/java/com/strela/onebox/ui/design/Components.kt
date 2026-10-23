package com.strela.onebox.ui.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Основная кнопка: заливка акцентным цветом, опциональная иконка слева. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val background = when {
        !enabled -> Palette.ButtonDisabled
        pressed -> Palette.AccentPrimaryPressed
        else -> Palette.AccentPrimary
    }
    val contentColor = if (enabled) Palette.OnAccent else Palette.TextMuted

    ButtonSurface(
        modifier = modifier,
        background = background,
        border = null,
        enabled = enabled,
        interaction = interaction,
        onClick = onClick
    ) {
        ButtonContent(text = text, color = contentColor, icon = icon)
    }
}

/** Второстепенная кнопка: только контур. */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val background = if (pressed && enabled) Palette.SurfaceElevated else Color.Transparent
    val borderColor = if (enabled) Palette.ButtonOutline else Palette.ButtonDisabled

    ButtonSurface(
        modifier = modifier,
        background = background,
        border = BorderStroke(1.dp, borderColor),
        enabled = enabled,
        interaction = interaction,
        onClick = onClick
    ) {
        ButtonContent(
            text = text,
            color = if (enabled) Palette.TextPrimary else Palette.TextMuted,
            icon = icon
        )
    }
}

/** Компактная иконка-кнопка (удалить, пинг, закрыть и т.п.). */
@Composable
fun IconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Palette.TextSecondary,
    background: Color = Color.Transparent,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(if (pressed && enabled) Palette.SurfaceElevated else background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) tint else Palette.TextMuted
        )
    }
}

@Composable
private fun ButtonSurface(
    modifier: Modifier,
    background: Color,
    border: BorderStroke?,
    enabled: Boolean,
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.975f else 1f,
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(background)
            .then(
                if (border != null) Modifier.border(border, shape) else Modifier
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(PaddingValues(horizontal = 16.dp, vertical = 13.dp)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ButtonContent(text: String, color: Color, icon: ImageVector?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                icon = icon,
                modifier = Modifier.size(17.dp),
                tint = color
            )
        }
        ButtonLabel(text, color)
    }
}

@Composable
private fun ButtonLabel(text: String, color: Color) {
    val typo = LocalOneBoxTypography.current
    BasicText(
        text = text,
        style = TextStyle(
            fontFamily = typo.heading.fontFamily,
            fontWeight = typo.heading.fontWeight,
            fontSize = typo.heading.fontSize,
            color = color
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Небольшой бейдж: протокол, транспорт, состояние. */
@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Palette.TextPrimary,
    background: Color = Palette.AccentSoft
) {
    val typo = LocalOneBoxTypography.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        BasicText(
            text = text,
            style = typo.label.copy(color = color)
        )
    }
}

/** Заголовок секции: «ПРОФИЛИ», «СПИСОК» и т.п. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = Palette.TextSecondary
) {
    val typo = LocalOneBoxTypography.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                icon = icon,
                modifier = Modifier.size(14.dp),
                tint = tint
            )
            Spacer(Modifier.width(7.dp))
        }
        BasicText(
            text = text.uppercase(),
            style = typo.label.copy(color = tint)
        )
    }
}

/** Панель с рамкой — замена материальной Card. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    background: Color = Palette.Surface,
    borderColor: Color = Palette.Border,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(BorderStroke(1.dp, borderColor), shape)
            .padding(padding)
    ) {
        content()
    }
}

/** Переключатель без Material: дорожка с анимированным бегунком. */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        label = "switchProgress"
    )
    val trackColor = when {
        !enabled -> Palette.ButtonDisabled
        checked -> Palette.AccentPrimary
        else -> Palette.SurfaceElevated
    }
    val borderColor = when {
        !enabled -> Palette.Border
        checked -> Palette.AccentPrimary
        else -> Palette.BorderFocused
    }

    Box(
        modifier = modifier
            .size(width = 46.dp, height = 28.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = 18.dp * progress)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) Palette.OnAccent else Palette.TextMuted)
        )
    }
}
