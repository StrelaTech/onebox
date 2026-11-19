package com.strela.onebox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.ServerSort
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.settings.UpdateInterval
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.strings
import com.strela.sui.design.Card
import com.strela.sui.design.Icon
import com.strela.sui.design.IconButton
import com.strela.sui.design.SmallTopAppBar
import com.strela.sui.design.Text
import com.strela.sui.design.TextField
import com.strela.sui.design.icons.SuiIcons
import com.strela.sui.kit.DropdownRow
import com.strela.sui.kit.IconTile
import com.strela.sui.kit.SectionTitle

/**
 * Экран «Подписки»: параметры загрузки подписок. Сейчас — User-Agent запросов,
 * по умолчанию `OneBox/<версия>`; значение читается [HttpSubscriptionFetcher]
 * при каждом скачивании, поэтому применяется сразу.
 */
@Composable
fun SubscriptionSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme
    val context = LocalContext.current
    val s = strings
    val userAgent by SettingsStore.subscriptionUserAgent.collectAsState()
    val serverSort by SettingsStore.serverSort.collectAsState()
    val updateInterval by SettingsStore.updateInterval.collectAsState()
    var draft by remember { mutableStateOf(userAgent) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        SmallTopAppBar(
            title = s.subscriptionSettings,
            navigationIcon = {
                IconButton(
                    onClick = {
                        AppLog.i("Настройки подписок: закрыт экран")
                        onClose()
                    },
                ) {
                    Icon(
                        imageVector = SuiIcons.Close,
                        contentDescription = s.close,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            SectionTitle(text = s.serverSort, icon = SuiIcons.Filter)
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
                Column {
                    val sortOptions = listOf(
                        ServerSort.SUBSCRIPTION to s.sortSubscription,
                        ServerSort.PING to s.sortPing,
                        ServerSort.NAME to s.sortName,
                    )
                    DropdownRow(
                        icon = SuiIcons.Filter,
                        title = s.serverSort,
                        subtitle = s.serverSortHint,
                        selectedLabel = sortOptions.firstOrNull { it.first == serverSort }?.second
                            ?: s.sortSubscription,
                        options = sortOptions.map { (sort, label) ->
                            label to {
                                AppLog.i("Настройки подписок: сортировка → $label")
                                SettingsStore.setServerSort(context, sort)
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            SectionTitle(text = s.updateInterval, icon = SuiIcons.Clock)
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
                Column {
                    val intervalOptions = listOf(
                        UpdateInterval.NEVER to s.intervalNever,
                        UpdateInterval.MIN30 to s.interval30m,
                        UpdateInterval.MIN60 to s.interval60m,
                        UpdateInterval.H2 to s.interval2h,
                        UpdateInterval.H6 to s.interval6h,
                        UpdateInterval.H12 to s.interval12h,
                        UpdateInterval.H24 to s.interval24h,
                    )
                    DropdownRow(
                        icon = SuiIcons.Clock,
                        title = s.updateInterval,
                        subtitle = s.updateIntervalHint,
                        selectedLabel = intervalOptions.firstOrNull { it.first == updateInterval }?.second
                            ?: s.intervalNever,
                        options = intervalOptions.map { (interval, label) ->
                            label to {
                                AppLog.i("Настройки подписок: интервал → $label")
                                SettingsStore.setUpdateInterval(context, interval)
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            SectionTitle(text = s.userAgent, icon = SuiIcons.Link)
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(
                            icon = SuiIcons.Link,
                            tint = colors.primary,
                            background = colors.primaryContainer,
                            size = 38.dp,
                            iconSize = 19.dp,
                            corner = 12.dp,
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp),
                        ) {
                            Text(
                                text = s.userAgent,
                                style = OneBoxTheme.textStyles.headline2,
                                color = colors.onSurfaceContainer,
                            )
                            Text(
                                text = s.userAgentHint,
                                style = OneBoxTheme.textStyles.footnote2,
                                color = colors.onSurfaceContainerVariant,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                    }
                    TextField(
                        value = draft,
                        onValueChange = {
                            draft = it
                            SettingsStore.setSubscriptionUserAgent(context, it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        placeholder = SettingsStore.defaultUserAgent(context),
                    )
                    Text(
                        text = SettingsStore.defaultUserAgent(context),
                        style = OneBoxTheme.textStyles.footnote2,
                        color = colors.onSurfaceContainerVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
