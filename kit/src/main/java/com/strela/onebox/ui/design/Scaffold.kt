package com.strela.onebox.ui.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints

/**
 * Каркас экрана на чистом Compose: [topBar] рисуется сверху, [bottomBar] —
 * снизу, содержимое получает их высоту через [PaddingValues] (как в
 * material-Scaffold): контент размером с экран, а внутренние отступы
 * применяет вызывающий код.
 */
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val barConstraints = Constraints(maxWidth = constraints.maxWidth)

        val topPlaceables = topBar
            ?.let { bar -> subcompose("top", bar).first().measure(barConstraints) }
        val bottomPlaceables = bottomBar
            ?.let { bar -> subcompose("bottom", bar).first().measure(barConstraints) }

        val topHeight = topPlaceables?.height ?: 0
        val bottomHeight = bottomPlaceables?.height ?: 0

        val contentPadding = PaddingValues(
            top = topHeight.toDp(),
            bottom = bottomHeight.toDp(),
        )

        val contentMeasurables = subcompose("content") { content(contentPadding) }
        val contentConstraints = constraints.copy(minHeight = 0)
        val contentPlaceables = contentMeasurables.map { it.measure(contentConstraints) }

        val layoutHeight = if (constraints.hasBoundedHeight) {
            constraints.maxHeight
        } else {
            contentPlaceables.maxOfOrNull { it.height } ?: 0
        }

        layout(constraints.maxWidth, layoutHeight) {
            contentPlaceables.forEach { it.place(0, 0) }
            topPlaceables?.place(0, 0)
            bottomPlaceables?.place(0, layoutHeight - bottomHeight)
        }
    }
}
