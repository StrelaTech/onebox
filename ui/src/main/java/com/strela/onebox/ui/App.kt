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
import com.strela.onebox.ui.design.NavigationBar
import com.strela.onebox.ui.design.NavigationBarItem
import com.strela.onebox.ui.design.Scaffold
import com.strela.onebox.ui.design.SmallTopAppBar
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.screens.ConnectScreen
import com.strela.onebox.ui.screens.ServersTab
import com.strela.onebox.ui.screens.SettingsTab
import com.strela.onebox.xray.XrayController

private enum class MainTab(val icon: ImageVector) {
    MAIN(OneBoxIcons.Home),
    SERVERS(OneBoxIcons.Globe),
    SETTINGS(OneBoxIcons.Settings),
}

@Composable
private fun MainTab.title(): String = when (this) {
    MainTab.MAIN -> strings.tabMain
    MainTab.SERVERS -> strings.tabServer
    MainTab.SETTINGS -> strings.tabSettings
}

/**
 * Оболочка приложения: шапка, нижняя навигация и переключение вкладок.
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
    val status by XrayController.status.collectAsState()

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
                        icon = entry.icon,
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
                    onShareLogs = onShareLogs,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
