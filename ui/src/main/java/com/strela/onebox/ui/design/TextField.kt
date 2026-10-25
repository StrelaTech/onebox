package com.strela.onebox.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxTheme

/**
 * Поле ввода на BasicTextField: заливка цветом surfaceContainer, скругление,
 * опциональные иконки по краям. Поддерживает многострочный ввод ([maxLines]).
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    placeholderColor: Color = OneBoxTheme.colorScheme.onSurfaceContainerVariant.copy(alpha = 0.5f),
    maxLines: Int = 1,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = OneBoxTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceContainer)
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke()

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            if (value.isEmpty()) {
                BasicText(
                    text = placeholder,
                    style = OneBoxTheme.textStyles.body1.copy(color = placeholderColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                textStyle = OneBoxTheme.textStyles.body1.copy(color = colors.onSurfaceContainer),
                cursorBrush = SolidColor(colors.primary),
                maxLines = maxLines,
            )
        }

        trailingIcon?.invoke()
    }
}
