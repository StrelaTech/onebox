package com.strela.onebox.ui.design

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Индикация нажатия в стиле HyperOS: полупрозрачный цветовой слой поверх
 * содержимого — замена Material-ripple на чистом Compose.
 * Провайдится через [androidx.compose.foundation.LocalIndication] в OneBoxTheme,
 * поэтому `clickable` и компоненты дизайн-кита получают её автоматически.
 */
class PressIndication(private val overlayColor: Color) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): Modifier.Node =
        PressNode(interactionSource, overlayColor)

    override fun equals(other: Any?): Boolean =
        other is PressIndication && other.overlayColor == overlayColor

    override fun hashCode(): Int = overlayColor.hashCode()
}

/**
 * Состояние нажатия — snapshot-состояние: чтение в [draw] само включает
 * перерисовку, поэтому никаких invalidateDraw не требуется.
 */
private class PressNode(
    private val interactionSource: InteractionSource,
    private val overlayColor: Color,
) : Modifier.Node(), DrawModifierNode {

    private var pressed by mutableStateOf(false)
    private var job: Job? = null

    override fun onAttach() {
        super.onAttach()
        job = coroutineScope.launch {
            var heldPresses = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> heldPresses++
                    is PressInteraction.Release, is PressInteraction.Cancel ->
                        heldPresses = (heldPresses - 1).coerceAtLeast(0)
                    else -> return@collect
                }
                pressed = heldPresses > 0
            }
        }
    }

    override fun onDetach() {
        job?.cancel()
        job = null
        pressed = false
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (pressed) {
            drawRect(overlayColor)
        }
    }
}
