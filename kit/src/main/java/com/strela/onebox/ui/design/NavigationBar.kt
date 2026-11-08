package com.strela.onebox.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.OneBoxTheme

/**
 * Нижняя панель навигации: фон тянется под системную панель (инсеты
 * добавляются отдельным спейсером), разделитель сверху и ряд элементов
 * [NavigationBarItem].
 */
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = OneBoxTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.dividerLine.copy(alpha = 0.6f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars),
        )
    }
}

/**
 * Элемент нижней навигации: иконка + подпись. Выбранная вкладка показывает
 * filled-иконку, не выбранная — outline.
 */
@Composable
fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    outline: ImageVector,
    filled: ImageVector,
    label: String,
) {
    val colors = OneBoxTheme.colorScheme
    val tint = if (selected) colors.onBackground else colors.onBackgroundVariant

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = if (selected) filled else outline,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(24.dp),
            tint = tint,
        )
        Text(
            text = label,
            style = OneBoxTheme.textStyles.footnote2,
            color = tint,
            maxLines = 1,
            modifier = Modifier.padding(top = 3.dp, bottom = 7.dp),
        )
    }
}
