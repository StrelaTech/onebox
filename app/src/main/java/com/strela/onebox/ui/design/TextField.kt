package com.strela.onebox.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Обычное текстовое поле (в отличие от [ConfigField] — пропорциональный шрифт,
 * подходит для ссылок и коротких значений). Без Material.
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    mono: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 6,
    leadingIcon: ImageVector? = null
) {
    val typo = LocalOneBoxTypography.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(12.dp)

    val borderColor = when {
        !enabled -> Palette.ButtonDisabled
        focused -> Palette.AccentPrimary
        else -> Palette.Border
    }
    val textStyle = (if (mono) typo.mono else typo.body).copy(
        color = if (enabled) Palette.TextPrimary else Palette.TextMuted
    )

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            BasicText(
                text = label.uppercase(),
                style = typo.label.copy(
                    color = when {
                        !enabled -> Palette.TextMuted
                        focused -> Palette.TextPrimary
                        else -> Palette.TextSecondary
                    }
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Palette.FieldBackground)
                .border(BorderStroke(if (focused) 1.5.dp else 1.dp, borderColor), shape)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = if (minLines > 1) Alignment.Top else Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        icon = leadingIcon,
                        modifier = Modifier
                            .padding(top = if (minLines > 1) 2.dp else 0.dp)
                            .size(17.dp),
                        tint = if (focused) Palette.AccentPrimary else Palette.TextMuted
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        BasicText(
                            text = placeholder,
                            style = textStyle.copy(color = Palette.TextMuted),
                            modifier = Modifier.padding(vertical = if (mono) 0.dp else 1.dp)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = (minLines * 18).dp),
                        enabled = enabled,
                        textStyle = textStyle,
                        cursorBrush = SolidColor(Palette.AccentPrimary),
                        maxLines = maxLines,
                        interactionSource = interaction
                    )
                }
            }
        }
    }
}
