package com.strela.onebox.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.ui.SettingsScreen

/**
 * Вкладка «Настройки»: подключает [SettingsScreen] к стору настроек и логу.
 */
@Composable
fun SettingsTab(
    context: Context,
    onShareLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme by SettingsStore.theme.collectAsState()
    val language by SettingsStore.language.collectAsState()
    val blockAds by SettingsStore.blockAds.collectAsState()
    val autoConnect by SettingsStore.autoConnect.collectAsState()
    val subscriptions by SubscriptionStore.subscriptions.collectAsState()
    val selectedSubscriptionId by SubscriptionStore.selectedSubscriptionId.collectAsState()
    val activeSubscription = remember(subscriptions, selectedSubscriptionId) {
        subscriptions.firstOrNull { it.id == selectedSubscriptionId }?.title
    }

    SettingsScreen(
        theme = theme,
        language = language,
        blockAds = blockAds,
        autoConnect = autoConnect,
        activeSubscription = activeSubscription,
        onThemeChange = { SettingsStore.setTheme(context, it) },
        onLanguageChange = { SettingsStore.setLanguage(context, it) },
        onBlockAdsChange = { SettingsStore.setBlockAds(context, it) },
        onAutoConnectChange = { SettingsStore.setAutoConnect(context, it) },
        onShareLogs = onShareLogs,
        onClearLogs = { AppLog.clear() },
        onResetSettings = { SettingsStore.reset(context) },
        modifier = modifier,
    )
}
