package com.strela.onebox.ui.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Иконка из набора [com.strela.onebox.ui.design.icons.OneBoxIcons].
 * Цвет всегда задаётся через [tint] — векторы хранятся чёрными.
 */
@Composable
fun Icon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Palette.TextPrimary
) {
    Image(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint)
    )
}

/** Иконка в цветном квадрате — для заголовков, статусов и пустых состояний. */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = Palette.AccentPrimary,
    background: Color = Palette.AccentSoft,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    corner: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon = icon,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
