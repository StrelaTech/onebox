package com.strela.onebox.ui.design

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.strela.onebox.ui.OneBoxTheme

/**
 * Текст на BasicText. Цвет по умолчанию — [LocalContentColor], который
 * переопределяют контейнеры (Surface, Button); аргумент [color] перекрывает оба.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = OneBoxTheme.textStyles.body1,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val contentColor = LocalContentColor.current
    val resolvedStyle = remember(style, color, contentColor, textAlign) {
        val aligned = if (textAlign == null) style else style.copy(textAlign = textAlign)
        aligned.copy(color = color.takeOrElse { contentColor })
    }
    BasicText(
        text = text,
        modifier = modifier,
        style = resolvedStyle,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout,
    )
}
