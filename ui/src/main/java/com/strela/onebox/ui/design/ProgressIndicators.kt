package com.strela.onebox.ui.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxTheme

/** Круговой индикатор неопределённого ожидания на чистом Canvas. */
@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    fillColor: Color = OneBoxTheme.colorScheme.primary,
    trackColor: Color = Color.Transparent,
    strokeWidth: Dp = 3.dp,
) {
    val spin = rememberInfiniteTransition(label = "oneboxSpin")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
        ),
        label = "oneboxAngle",
    )

    Canvas(modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        if (trackColor != Color.Transparent) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
            )
        }
        drawArc(
            color = fillColor,
            startAngle = angle,
            sweepAngle = 270f,
            useCenter = false,
            style = stroke,
        )
    }
}

/** Линейный индикатор неопределённой загрузки: бегущий сегмент. */
@Composable
fun InfiniteProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = OneBoxTheme.colorScheme.primary,
) {
    val transition = rememberInfiniteTransition(label = "oneboxLinear")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
        ),
        label = "oneboxLinearSlide",
    )

    Canvas(modifier.height(3.dp).clipToBounds()) {
        val barWidth = size.width * 0.33f
        val travel = size.width + barWidth
        val x = travel * (progress + 1f) / 2f - barWidth
        drawRoundRect(
            color = color,
            topLeft = Offset(x, 0f),
            size = Size(barWidth, size.height),
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
        )
    }
}
