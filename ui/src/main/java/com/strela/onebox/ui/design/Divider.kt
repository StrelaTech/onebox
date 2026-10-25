package com.strela.onebox.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxTheme

/** Тонкая линия-разделитель. */
@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = OneBoxTheme.colorScheme.dividerLine,
) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}
