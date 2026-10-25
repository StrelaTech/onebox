package com.strela.onebox.ui.design

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

/**
 * Векторная иконка с тонировкой — чистый Compose вместо material.Icon.
 * По умолчанию берёт [LocalContentColor], задаваемый контейнером.
 */
@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val resolvedTint = tint.takeOrElse { Color.Unspecified }
    Image(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = if (resolvedTint == Color.Unspecified) {
            null
        } else {
            ColorFilter.tint(resolvedTint)
        },
    )
}
