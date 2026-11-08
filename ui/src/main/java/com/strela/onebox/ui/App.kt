package com.strela.onebox.ui

import androidx.activity.compose.BackHandler
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
 * Подэкраны, которые открываются поверх вкладок: системная «Назад»
 * возвращает на предыдущий экран (первое нажатие — подэкран из стека,
 * затем обычная навигация по вкладкам).
 */
private enum class Overlay {
    LOGS,
    PING,
    APPEARANCE,
    CONNECTION,
    DIAGNOSTICS,
    ABOUT,
    LICENSES,
}

/**
 * Оболочка приложения: шапка, нижняя навигация и переключение вкладок.
 * Поверх вкладок живёт стек подэкранов — так системная «Назад» всегда
 * возвращает на предыдущий экран.
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
    // Стек подэкранов: последняя запись — активный экран
    val overlays = composableStack()
    val status by VpnConnector.status.collectAsState()

    // Системная «Назад»: сначала закрываем подэкраны, потом работаем по стеку вкладок
    BackHandler(enabled = overlays.isNotEmpty()) {
        popOverlay(overlays)
    }

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
                    onOpenAppearance = { overlays += Overlay.APPEARANCE },
                    onOpenConnection = { overlays += Overlay.CONNECTION },
                    onOpenDiagnostics = { overlays += Overlay.DIAGNOSTICS },
                    onOpenAbout = { overlays += Overlay.ABOUT },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    overlays.forEach { overlay ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (overlay) {
                Overlay.LOGS -> LogsConsoleScreen(
                    onClose = { popOverlay(overlays) },
                    modifier = Modifier.fillMaxSize(),
                )

                Overlay.PING -> PingSettingsScreen(
                    onClose = { popOverlay(overlays) },
                    modifier = Modifier.fillMaxSize(),
                )

                Overlay.APPEARANCE -> AppearanceSettingsScreen(
                    onClose = { popOverlay(overlays) },
                    modifier = Modifier.fillMaxSize(),
                )

                Overlay.CONNECTION -> ConnectionSettingsScreen(
                    onClose = { popOverlay(overlays) },
                    onOpenPing = { overlays += Overlay.PING },
                    modifier = Modifier.fillMaxSize(),
                )

                Overlay.DIAGNOSTICS -> DiagnosticsSettingsScreen(
                    onClose = { popOverlay(overlays) },
                    onOpenLogs = { overlays += Overlay.LOGS },
                    onShareLogs = onShareLogs,
                    modifier = Modifier.fillMaxSize(),
                )

                Overlay.ABOUT -> AboutSettingsScreen(
                    onClose = { popOverlay(overlays) },
                    onOpenLicenses = { overlays += Overlay.LICENSES },
                    modifier = Modifier.fillMaxSize(),
                )

                Overlay.LICENSES -> LicensesScreen(
                    onClose = { popOverlay(overlays) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * Стек подэкранов как список-состояние: push — добавить, pop — оставить
 * все элементы, кроме последнего.
 */
private fun composableStack(): androidx.compose.runtime.snapshots.SnapshotStateList<Overlay> =
    androidx.compose.runtime.mutableStateListOf()

private fun popOverlay(stack: androidx.compose.runtime.snapshots.SnapshotStateList<Overlay>) {
    if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
}
