package com.strela.onebox.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.ui.SettingsScreen

/**
 * Вкладка «Настройки»: подключает [SettingsScreen] к стору настроек, логу
 * и ядру (точная версия xray-core).
 */
@Composable
fun SettingsTab(
    context: Context,
    onOpenPing: () -> Unit,
    onOpenLogs: () -> Unit,
    onShareLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme by SettingsStore.theme.collectAsState()
    val language by SettingsStore.language.collectAsState()
    val fontScale by SettingsStore.fontScale.collectAsState()
    val blockAds by SettingsStore.blockAds.collectAsState()
    val autoConnect by SettingsStore.autoConnect.collectAsState()
    val coreVersion by VpnConnector.version.collectAsState()
    val subscriptions by SubscriptionStore.subscriptions.collectAsState()
    val selectedSubscriptionId by SubscriptionStore.selectedSubscriptionId.collectAsState()
    val activeSubscription = remember(subscriptions, selectedSubscriptionId) {
        subscriptions.firstOrNull { it.id == selectedSubscriptionId }?.title
    }

    SettingsScreen(
        theme = theme,
        language = language,
        fontScale = fontScale,
        blockAds = blockAds,
        autoConnect = autoConnect,
        coreVersion = coreVersion,
        activeSubscription = activeSubscription,
        onThemeChange = { SettingsStore.setTheme(context, it) },
        onLanguageChange = { SettingsStore.setLanguage(context, it) },
        onFontScaleChange = { SettingsStore.setFontScale(context, it) },
        onBlockAdsChange = { SettingsStore.setBlockAds(context, it) },
        onAutoConnectChange = { SettingsStore.setAutoConnect(context, it) },
        onOpenPing = onOpenPing,
        onOpenLogs = onOpenLogs,
        onShareLogs = onShareLogs,
        onClearLogs = { AppLog.clear() },
        onResetSettings = {
            // Сбрасывается всё-всё: настройки, подписки (вместе с выбором)
            // и сохранённый JSON-конфиг; после сброса конфиг пересобирается.
            SettingsStore.reset(context)
            SubscriptionStore.clearAll(context)
            VpnConnector.clearUserConfig()
        },
        modifier = modifier,
    )
}
