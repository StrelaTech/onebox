package com.strela.onebox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strela.onebox.api.IpType
import com.strela.onebox.log.AppLog
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.strings
import com.strela.sui.design.Card
import com.strela.sui.design.Icon
import com.strela.sui.design.IconButton
import com.strela.sui.design.SmallTopAppBar
import com.strela.sui.design.Switch
import com.strela.sui.design.Text
import com.strela.sui.design.TextField
import com.strela.sui.design.icons.SuiIcons
import com.strela.sui.kit.DropdownRow
import com.strela.sui.kit.IconTile
import com.strela.sui.kit.SectionTitle

/**
 * Экран «Сеть»: MTU туннеля, предпочтительный тип IP, фрагментация TLS
 * и мультиплексирование (MUX). Настройки читаются ядром в момент сборки
 * конфига и tun-интерфейса, поэтому применяются при следующем подключении.
 */
@Composable
fun NetworkSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme
    val context = LocalContext.current
    val s = strings
    val options by SettingsStore.networkOptions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        SmallTopAppBar(
            title = s.network,
            navigationIcon = {
                IconButton(
                    onClick = {
                        AppLog.i("Настройки сети: закрыт экран")
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
            NetworkSection(title = s.mtu, icon = SuiIcons.Wifi) {
                NumberFieldRow(
                    icon = SuiIcons.Wifi,
                    title = s.mtu,
                    subtitle = s.mtuHint,
                    value = options.mtu.toString(),
                    onValueChange = { raw ->
                        raw.toIntOrNull()?.let { SettingsStore.setMtu(context, it) }
                    },
                )
            }

            NetworkSection(title = s.ipType, icon = SuiIcons.Globe) {
                val labels = listOf(
                    IpType.AUTO to s.ipAuto,
                    IpType.IPV4 to s.ipv4,
                    IpType.IPV6 to s.ipv6,
                )
                DropdownRow(
                    icon = SuiIcons.Globe,
                    title = s.ipType,
                    subtitle = s.ipTypeHint,
                    selectedLabel = labels.firstOrNull { it.first == options.ipType }?.second
                        ?: s.ipAuto,
                    options = labels.map { (type, label) ->
                        label to {
                            AppLog.i("Настройки сети: тип IP → $label")
                            SettingsStore.setIpType(context, type)
                        }
                    },
                )
            }

            NetworkSection(title = s.fragmentation, icon = SuiIcons.Layers) {
                SwitchRow(
                    icon = SuiIcons.Layers,
                    title = s.fragmentation,
                    subtitle = s.fragmentationHint,
                    checked = options.fragment.enabled,
                    onCheckedChange = { SettingsStore.setFragmentEnabled(context, it) },
                )
                if (options.fragment.enabled) {
                    TextFieldRow(
                        icon = SuiIcons.Edit,
                        title = s.fragmentPackets,
                        subtitle = s.fragmentPacketsHint,
                        value = options.fragment.packets,
                        onValueChange = { SettingsStore.setFragmentPackets(context, it) },
                    )
                    TextFieldRow(
                        icon = SuiIcons.Edit,
                        title = s.fragmentLength,
                        subtitle = s.fragmentLengthHint,
                        value = options.fragment.length,
                        onValueChange = { SettingsStore.setFragmentLength(context, it) },
                    )
                    TextFieldRow(
                        icon = SuiIcons.Edit,
                        title = s.fragmentInterval,
                        subtitle = s.fragmentIntervalHint,
                        value = options.fragment.interval,
                        onValueChange = { SettingsStore.setFragmentInterval(context, it) },
                    )
                }
            }

            NetworkSection(title = s.mux, icon = SuiIcons.Layers) {
                SwitchRow(
                    icon = SuiIcons.Layers,
                    title = s.mux,
                    subtitle = s.muxHint,
                    checked = options.mux.enabled,
                    onCheckedChange = { SettingsStore.setMuxEnabled(context, it) },
                )
                if (options.mux.enabled) {
                    NumberFieldRow(
                        icon = SuiIcons.Edit,
                        title = s.muxConcurrency,
                        subtitle = s.muxConcurrencyHint,
                        value = options.mux.concurrency.toString(),
                        onValueChange = { raw ->
                            raw.toIntOrNull()?.let { SettingsStore.setMuxConcurrency(context, it) }
                        },
                    )
                    NumberFieldRow(
                        icon = SuiIcons.Edit,
                        title = s.muxXudp,
                        subtitle = s.muxXudpHint,
                        value = options.mux.xudpConcurrency.toString(),
                        onValueChange = { raw ->
                            raw.toIntOrNull()?.let { SettingsStore.setMuxXudpConcurrency(context, it) }
                        },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Строка с подписью и полем ввода текста. */
@Composable
private fun TextFieldRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val colors = OneBoxTheme.colorScheme
    // Черновик не привязан к value remember-ключом: иначе курсор прыгал бы
    // в конец при каждом изменении сохранённого значения.
    var draft by remember { mutableStateOf(value) }

    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = icon,
                tint = colors.onSurfaceContainerVariant,
                background = colors.surfaceContainerHigh,
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
                    text = title,
                    style = OneBoxTheme.textStyles.headline2,
                    color = colors.onSurfaceContainer,
                )
                Text(
                    text = subtitle,
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
                onValueChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            placeholder = title,
        )
    }
}

/** То же, но с цифровой клавиатурой и числовым значением. */
@Composable
private fun NumberFieldRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val colors = OneBoxTheme.colorScheme
    var draft by rememberSaveable { mutableStateOf(value) }

    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = icon,
                tint = colors.onSurfaceContainerVariant,
                background = colors.surfaceContainerHigh,
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
                    text = title,
                    style = OneBoxTheme.textStyles.headline2,
                    color = colors.onSurfaceContainer,
                )
                Text(
                    text = subtitle,
                    style = OneBoxTheme.textStyles.footnote2,
                    color = colors.onSurfaceContainerVariant,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        TextField(
            value = draft,
            onValueChange = { raw ->
                if (raw.all { it.isDigit() }) {
                    draft = raw
                    onValueChange(raw)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            placeholder = title,
        )
    }
}

/** Строка-переключатель с иконкой, названием и подписью. */
@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = OneBoxTheme.colorScheme
    val toggle: (Boolean) -> Unit = { value ->
        AppLog.i("Настройки сети: «$title» → ${if (value) "включено" else "выключено"}")
        onCheckedChange(value)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { toggle(!checked) }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(
            icon = icon,
            tint = if (checked) colors.primary else colors.onSurfaceContainerVariant,
            background = if (checked) colors.primaryContainer else colors.surfaceContainerHigh,
            size = 38.dp,
            iconSize = 19.dp,
            corner = 12.dp,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 14.dp),
        ) {
            Text(
                text = title,
                style = OneBoxTheme.textStyles.headline2,
                color = colors.onSurfaceContainer,
            )
            Text(
                text = subtitle,
                style = OneBoxTheme.textStyles.footnote2,
                color = colors.onSurfaceContainerVariant,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Switch(checked = checked, onCheckedChange = toggle)
    }
}

/** Секция экрана сети: подпись сверху и карточка с содержимым. */
@Composable
private fun NetworkSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    SectionTitle(text = title, icon = icon)
    Spacer(Modifier.height(10.dp))
    Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
        Column(content = content)
    }
    Spacer(Modifier.height(24.dp))
}
