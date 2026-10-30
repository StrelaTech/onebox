package com.strela.onebox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strela.onebox.log.AppLog
import com.strela.onebox.log.LogEntry
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.Card
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.SmallTopAppBar
import com.strela.onebox.ui.design.Surface
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.TextField
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Интервал перечитывания журнала, мс. */
private const val REFRESH_MILLIS = 700L

/** Хвост журнала в консоли, байт (чтобы не тащить в текст весь файл). */
private const val CONSOLE_TAIL_BYTES = 256L * 1024

/** Полный набор уровней: фильтр «Все» и стартовое состояние чипов. */
private val ALL_SEVERITIES = setOf('D', 'I', 'W', 'E')

/** Чипы фильтра по уровням — в порядке убывания полезности. */
private val FILTER_SEVERITIES = listOf('D', 'I', 'W', 'E')

/**
 * Консоль логов: хвост журнала построчно, с подсветкой по уровню, поиском
 * и фильтром уровней. Обновляется в фоне, пока экран открыт; содержимое
 * совпадает с файлом, который уходит по кнопке «Импортировать логи».
 */
@Composable
fun LogsConsoleScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme
    val s = strings
    val scope = rememberCoroutineScope()

    var tail by remember { mutableStateOf(AppLog.tailEntries(CONSOLE_TAIL_BYTES)) }
    var query by remember { mutableStateOf("") }
    var severities by remember { mutableStateOf(ALL_SEVERITIES) }

    // Консоль живая: перечитываем хвост журнала, пока экран открыт
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(REFRESH_MILLIS)
            tail = AppLog.tailEntries(CONSOLE_TAIL_BYTES)
        }
    }

    val search = query.trim()
    val lines = remember(tail, search, severities) {
        tail.filter { entry ->
            val levelOk = if (entry.severity == null) {
                // служебные маркеры «── сессия начата ──» уместны только целиком
                severities == ALL_SEVERITIES
            } else {
                entry.severity in severities
            }
            levelOk && (search.isEmpty() || entry.text.contains(search, ignoreCase = true))
        }
    }

    val listState = rememberLazyListState()
    var autoFollow by remember { mutableStateOf(true) }
    val atEnd by remember { derivedStateOf { isAtEnd(listState) } }

    // Листает пользователь: ушёл от конца — автопрокрутка замирает, вернулся — оживает
    LaunchedEffect(listState) {
        snapshotFlow { isAtEnd(listState) }.collect { atEnd -> autoFollow = atEnd }
    }

    // Новые строки и смены фильтров: дотягиваем до последней записи, только если и так у конца
    LaunchedEffect(lines.size, search, severities) {
        if (autoFollow && lines.isNotEmpty()) {
            listState.scrollToItem(lines.lastIndex)
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
            actions = {
                IconButton(
                    onClick = {
                        AppLog.clear()
                        tail = AppLog.tailEntries(CONSOLE_TAIL_BYTES)
                    },
                ) {
                    Icon(
                        imageVector = OneBoxIcons.Trash,
                        contentDescription = s.clearLogs,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )

        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = s.logsSearch,
            leadingIcon = {
                Icon(imageVector = OneBoxIcons.Search, contentDescription = null, modifier = Modifier.size(20.dp))
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = OneBoxIcons.Close,
                            contentDescription = s.close,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LevelChip(
                label = s.logsFilterAll,
                selected = severities == ALL_SEVERITIES,
                onClick = { severities = ALL_SEVERITIES },
            )
            FILTER_SEVERITIES.forEach { severity ->
                LevelChip(
                    label = severity.toString(),
                    selected = severity in severities,
                    onClick = {
                        severities = if (severity in severities) {
                            severities - severity
                        } else {
                            severities + severity
                        }
                    },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                insideMargin = PaddingValues(14.dp),
            ) {
                if (tail.isEmpty()) {
                    EmptyLabel(text = s.logsConsoleEmpty)
                } else if (lines.isEmpty()) {
                    EmptyLabel(text = s.logsNoMatches)
                } else {
                    LogList(lines = lines, listState = listState)
                }
            }

            if (lines.isNotEmpty() && !atEnd) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = CircleShape,
                    color = colors.surfaceContainerHigh,
                    onClick = {
                        autoFollow = true
                        scope.launch {
                            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
                        }
                    },
                ) {
                    Icon(
                        imageVector = OneBoxIcons.ArrowDown,
                        contentDescription = s.logsJumpToEnd,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp),
                    )
                }
            }
        }
    }
}

/** Последний элемент списка виден (или список пуст) — считаем, что мы у конца. */
private fun isAtEnd(state: LazyListState): Boolean {
    val info = state.layoutInfo
    val last = info.visibleItemsInfo.lastOrNull()
    return last == null || last.index == info.totalItemsCount - 1
}

/** Построчный журнал: каждая запись — свой текст, покрашенный по уровню. */
@Composable
private fun LogList(lines: List<LogEntry>, listState: LazyListState) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(lines) { _, entry ->
            Text(
                text = entry.text,
                style = OneBoxTheme.textStyles.footnote2.copy(fontFamily = FontFamily.Monospace),
                color = entryColor(entry.severity),
            )
        }
    }
}

/** Цвет записи по уровню: E — красный, W — янтарный, D и служебные — тусклый. */
@Composable
private fun entryColor(severity: Char?): Color {
    val colors = OneBoxTheme.colorScheme
    return when (severity) {
        'E' -> colors.error
        'W' -> colors.warning
        'D', null -> colors.onSurfaceContainerVariant
        else -> colors.onSurfaceContainer
    }
}

/** Круглый чип фильтра уровней: выбранный — чёрная заливка, иначе — серый контейнер. */
@Composable
private fun LevelChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (selected) {
            OneBoxTheme.colorScheme.primary
        } else {
            OneBoxTheme.colorScheme.surfaceContainerHigh
        },
        onClick = onClick,
    ) {
        Text(
            text = label,
            style = OneBoxTheme.textStyles.footnote1,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

/** Центрированная подпись вместо списка (журнал пуст / фильтр ничего не нашёл). */
@Composable
private fun EmptyLabel(text: String) {
    Text(
        text = text,
        style = OneBoxTheme.textStyles.body2,
        color = OneBoxTheme.colorScheme.onSurfaceContainerVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxSize(),
    )
}
