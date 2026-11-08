package com.strela.onebox.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.strela.onebox.ui.SettingsScreen

/**
 * Вкладка «Настройки»: хаб из четырёх секций — внешний вид, подключение,
 * диагностика, о приложении. Каждый раздел открывает свой экран (App.kt).
 */
@Composable
fun SettingsTab(
    context: Context,
    onOpenAppearance: () -> Unit,
    onOpenConnection: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsScreen(
        onOpenAppearance = onOpenAppearance,
        onOpenConnection = onOpenConnection,
        onOpenDiagnostics = onOpenDiagnostics,
        onOpenAbout = onOpenAbout,
        modifier = modifier,
    )
}
