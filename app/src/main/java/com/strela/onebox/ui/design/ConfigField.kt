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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.strela.onebox.i18n.strings
import com.strela.onebox.ui.design.icons.OneBoxIcons

/**
 * Многострочное поле для JSON-конфига. Моноширинный шрифт, рамка подсвечивается
 * при фокусе. Реализовано на BasicTextField — без Material.
 */
@Composable
fun ConfigField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minLines: Int = 10
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

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(
                text = label,
                icon = OneBoxIcons.Code,
                tint = when {
                    !enabled -> Palette.TextMuted
                    focused -> Palette.TextPrimary
                    else -> Palette.TextSecondary
                }
            )
            BasicText(
                text = strings.charCount(value.length),
                style = typo.caption.copy(color = Palette.TextMuted)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Palette.FieldBackground)
                .border(BorderStroke(if (focused) 1.5.dp else 1.dp, borderColor), shape)
                .padding(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = (minLines * 18).dp),
                enabled = enabled,
                textStyle = typo.mono.copy(
                    color = if (enabled) Palette.TextPrimary else Palette.TextMuted
                ),
                cursorBrush = SolidColor(Palette.AccentPrimary),
                visualTransformation = VisualTransformation.None,
                keyboardOptions = KeyboardOptions.Default,
                interactionSource = interaction
            )
        }
    }
}
