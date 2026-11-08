package com.strela.onebox.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxTheme

/** Переключатель в стиле HyperOS: дорожка + подвижный бегунок. */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme
    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.primary else colors.switchTrackUnchecked,
        animationSpec = tween(durationMillis = 150),
        label = "oneboxSwitchTrack",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) colors.onPrimary else colors.switchThumbUnchecked,
        animationSpec = tween(durationMillis = 150),
        label = "oneboxSwitchThumb",
    )

    val trackWidth = 52.dp
    val trackHeight = 32.dp
    val thumbSize = 26.dp
    val thumbMargin = 3.dp
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbMargin else thumbMargin,
        animationSpec = tween(durationMillis = 150),
        label = "oneboxSwitchOffset",
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(enabled = onCheckedChange != null) { onCheckedChange?.invoke(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .background(thumbColor, CircleShape),
        )
    }
}
