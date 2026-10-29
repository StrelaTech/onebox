package com.strela.onebox.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strela.onebox.ui.components.StatusPill
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.ui.design.NavigationBar
import com.strela.onebox.ui.design.NavigationBarItem
import com.strela.onebox.ui.design.Scaffold
import com.strela.onebox.ui.design.SmallTopAppBar
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.screens.ConnectScreen
import com.strela.onebox.ui.screens.LogsConsoleScreen
import com.strela.onebox.ui.screens.PingSettingsScreen
import com.strela.onebox.ui.screens.ServersTab
import com.strela.onebox.ui.screens.SettingsTab

/** Вкладки навигации: outline-иконка, пока не выбрана, filled — когда выбрана. */
private enum class MainTab(val outline: ImageVector, val filled: ImageVector) {
    MAIN(OneBoxIcons.HomeOutline, OneBoxIcons.Home),
    SERVERS(OneBoxIcons.Globe, OneBoxIcons.GlobeFilled),
    SETTINGS(OneBoxIcons.SettingsOutline, OneBoxIcons.Settings),
}

@Composable
private fun MainTab.title(): String = when (this) {
    MainTab.MAIN -> strings.tabMain
    MainTab.SERVERS -> strings.tabServer
    MainTab.SETTINGS -> strings.tabSettings
}

/**
 * Оболочка приложения: шапка, нижняя навигация и переключение вкладок.
 * Поверх вкладок открывается консоль логов (см. [LogsConsoleScreen]).
 * Содержимое вкладок живёт в ui/screens.
 */
@Composable
fun OneBoxApp(
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit,
    onShareLogs: () -> Unit,
) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(MainTab.MAIN) }
    var showLogsConsole by rememberSaveable { mutableStateOf(false) }
    var showPingSettings by rememberSaveable { mutableStateOf(false) }
    val status by VpnConnector.status.collectAsState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = tab.title(),
                navigationIcon = {
                    Text(
                        text = "OneBox",
                        style = OneBoxTheme.textStyles.headline1,
                        color = OneBoxTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                },
                actions = {
                    StatusPill(
                        status = status,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = entry == tab,
                        onClick = { tab = entry },
                        outline = entry.outline,
                        filled = entry.filled,
                        label = entry.title(),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 22.dp),
        ) {
            when (tab) {
                MainTab.MAIN -> ConnectScreen(
                    onStartVpn = onStartVpn,
                    onStopVpn = onStopVpn,
                    onOpenSubscriptions = { tab = MainTab.SERVERS },
                    modifier = Modifier.fillMaxSize(),
                )

                MainTab.SERVERS -> ServersTab(
                    context = context,
                    modifier = Modifier.fillMaxSize(),
                )

                MainTab.SETTINGS -> SettingsTab(
                    context = context,
                    onOpenPing = { showPingSettings = true },
                    onOpenLogs = { showLogsConsole = true },
                    onShareLogs = onShareLogs,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    // Экран настроек пинга открывается поверх всего, как консоль логов
    if (showPingSettings) {
        PingSettingsScreen(
            onClose = { showPingSettings = false },
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Консоль логов открывается поверх всего, включая нижнюю навигацию
    if (showLogsConsole) {
        LogsConsoleScreen(
            onClose = { showLogsConsole = false },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
