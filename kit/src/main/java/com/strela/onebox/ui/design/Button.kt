package com.strela.onebox.ui.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxTheme

/**
 * Кнопка-«пилюля»: заливка цветом primary (или серым в выключенном виде),
 * высота от 48dp, контент — [RowScope].
 */
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = OneBoxTheme.colorScheme
    val containerColor = if (enabled) colors.primary else colors.disabledPrimaryButton
    val contentColor = if (enabled) colors.onPrimary else colors.disabledOnPrimaryButton

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, content = content)
            }
        }
    }
}

/** Круглая иконка-кнопка 40dp. */
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides OneBoxTheme.colorScheme.onSurfaceContainer,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
