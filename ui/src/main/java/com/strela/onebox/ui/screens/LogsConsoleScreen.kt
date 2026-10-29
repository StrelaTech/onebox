package com.strela.onebox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strela.onebox.log.AppLog
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.Card
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.SmallTopAppBar
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Интервал перечитывания журнала, мс. */
private const val REFRESH_MILLIS = 700L

/** Хвост журнала в консоли, байт (чтобы не тащить в текст весь файл). */
private const val CONSOLE_TAIL_BYTES = 256L * 1024

/**
 * Консоль логов: хвост журнала моноширинным блоком с авто-прокруткой вниз.
 * Обновляется в фоне, пока экран открыт; содержимое совпадает с файлом,
 * который уходит по кнопке «Импортировать логи».
 */
@Composable
fun LogsConsoleScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme
    val s = strings
    var content by remember { mutableStateOf(AppLog.tail(CONSOLE_TAIL_BYTES)) }

    // Консоль живая: перечитываем хвост журнала, пока экран открыт
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(REFRESH_MILLIS)
            content = AppLog.tail(CONSOLE_TAIL_BYTES)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        SmallTopAppBar(
            title = s.logsConsole,
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = OneBoxIcons.Close,
                        contentDescription = s.close,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            insideMargin = PaddingValues(14.dp),
        ) {
            if (content.isBlank()) {
                Text(
                    text = s.logsConsoleEmpty,
                    style = OneBoxTheme.textStyles.body2,
                    color = colors.onSurfaceContainerVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ConsoleContent(content = content)
            }
        }
    }
}

/** Прокручиваемый моноширинный текст журнала; всегда «залипает» на последней строке. */
@Composable
private fun ConsoleContent(content: String) {
    val scroll = rememberScrollState()

    LaunchedEffect(content) {
        if (scroll.maxValue > 0) scroll.animateScrollTo(scroll.maxValue)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
    ) {
        Text(
            text = content,
            style = OneBoxTheme.textStyles.footnote2.copy(fontFamily = FontFamily.Monospace),
            color = OneBoxTheme.colorScheme.onSurfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
