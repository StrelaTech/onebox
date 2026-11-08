package com.strela.onebox.ui.kit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.Surface
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons

/**
 * Строка с выпадающим меню: нажатие открывает всплывающий список вариантов.
 * Справа — текущее выбранное значение и стрелка раскрытия.
 */
@Composable
fun DropdownRow(
    icon: ImageVector,
    title: String,
    selectedLabel: String,
    options: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = OneBoxTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(
            icon = icon,
            tint = colors.primary,
            background = colors.primaryContainer,
            size = 38.dp,
            iconSize = 19.dp,
            corner = 12.dp,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 12.dp),
        ) {
            Text(
                text = title,
                style = OneBoxTheme.textStyles.headline2,
                color = colors.onSurfaceContainer,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = OneBoxTheme.textStyles.footnote2,
                    color = colors.onSurfaceContainerVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text(
            text = selectedLabel,
            style = OneBoxTheme.textStyles.footnote2,
            color = colors.onSurfaceContainerVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = if (expanded) OneBoxIcons.ArrowUp else OneBoxIcons.ArrowDown,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(18.dp),
            tint = colors.onSurfaceContainerVariant,
        )

        if (expanded) {
            Popup(onDismissRequest = { expanded = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainerHigh,
                    shadowElevation = 12.dp,
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        options.forEach { (label, onSelect) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded = false
                                        onSelect()
                                    }
                                    .padding(horizontal = 18.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = label,
                                    style = OneBoxTheme.textStyles.body2,
                                    color = colors.onSurfaceContainer,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                )
                                if (label == selectedLabel) {
                                    Icon(
                                        imageVector = OneBoxIcons.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = colors.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
