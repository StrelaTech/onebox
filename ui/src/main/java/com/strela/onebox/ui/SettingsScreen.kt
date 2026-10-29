package com.strela.onebox.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.settings.ThemeMode
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.Card
import com.strela.onebox.ui.design.HorizontalDivider
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.Surface
import com.strela.onebox.ui.design.Switch
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.DropdownRow
import com.strela.onebox.ui.kit.IconTile
import com.strela.onebox.ui.kit.SectionTitle
import kotlinx.coroutines.delay

/**
 * Экран «Настройки»: внешний вид, язык, параметры подключения,
 * диагностика (консоль и файл логов) и сведения о приложении.
 */
@Composable
fun SettingsScreen(
    theme: ThemeMode,
    language: AppLanguage,
    fontScale: Float,
    blockAds: Boolean,
    autoConnect: Boolean,
    coreVersion: String,
    activeSubscription: String?,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onBlockAdsChange: (Boolean) -> Unit,
    onAutoConnectChange: (Boolean) -> Unit,
    onOpenPing: () -> Unit,
    onOpenLogs: () -> Unit,
    onShareLogs: () -> Unit,
    onClearLogs: () -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OneBoxTheme.colorScheme
    val context = LocalContext.current
    val s = strings

    val appVersion = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty().ifEmpty { "—" }
    }

    var notice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(notice) {
        if (notice != null) {
            delay(2600)
            notice = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsSection(title = s.appearance, icon = OneBoxIcons.Sun) {
            val themeOptions = listOf(
                ThemeMode.LIGHT to s.themeLight,
                ThemeMode.DARK to s.themeDark,
                ThemeMode.SYSTEM to s.themeSystemShort,
            )
            DropdownRow(
                icon = OneBoxIcons.Sun,
                title = s.themeTitle,
                subtitle = s.settingsHint,
                selectedLabel = themeOptions.firstOrNull { it.first == theme }?.second
                    ?: s.themeSystemShort,
                options = themeOptions.map { (mode, label) ->
                    label to { onThemeChange(mode) }
                },
            )

            SettingsDivider(startIndent = 18.dp)

            val textSizeOptions = listOf(
                SettingsStore.FONT_SCALE_SMALL to s.textSizeSmall,
                SettingsStore.FONT_SCALE_NORMAL to s.textSizeNormal,
                SettingsStore.FONT_SCALE_LARGE to s.textSizeLarge,
                SettingsStore.FONT_SCALE_HUGE to s.textSizeHuge,
            )
            DropdownRow(
                icon = OneBoxIcons.FormatSize,
                title = s.textSize,
                subtitle = s.textSizeHint,
                selectedLabel = textSizeOptions.firstOrNull { it.first == fontScale }?.second
                    ?: s.textSizeNormal,
                options = textSizeOptions.map { (scale, label) ->
                    label to { onFontScaleChange(scale) }
                },
            )

            SettingsDivider(startIndent = 18.dp)

            val languageOptions = listOf(
                AppLanguage.RU to s.languageRussian,
                AppLanguage.EN to s.languageEnglish,
            )
            DropdownRow(
                icon = OneBoxIcons.Chat,
                title = s.language,
                selectedLabel = languageOptions.firstOrNull { it.first == language }?.second
                    ?: s.languageRussian,
                options = languageOptions.map { (lang, label) ->
                    label to { onLanguageChange(lang) }
                },
            )
        }

        SettingsSection(title = s.connection, icon = OneBoxIcons.Zap) {
            ToggleRow(
                icon = OneBoxIcons.Zap,
                title = s.blockAds,
                subtitle = s.blockAdsHint,
                checked = blockAds,
                onCheckedChange = onBlockAdsChange,
            )
            SettingsDivider()
            ToggleRow(
                icon = OneBoxIcons.Refresh,
                title = s.autoConnect,
                subtitle = s.autoConnectHint,
                checked = autoConnect,
                onCheckedChange = onAutoConnectChange,
            )
        }

        SettingsSection(title = s.diagnostics, icon = OneBoxIcons.Terminal) {
            Text(
                text = s.logsHint,
                style = OneBoxTheme.textStyles.footnote2,
                color = colors.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            )
            SettingsDivider(startIndent = 18.dp)
            ActionRow(
                icon = OneBoxIcons.Wifi,
                title = s.settingsPing,
                subtitle = s.settingsPingHint,
                onClick = onOpenPing,
            )
            SettingsDivider()
            ActionRow(
                icon = OneBoxIcons.Eye,
                title = s.viewLogs,
                subtitle = s.viewLogsHint,
                onClick = onOpenLogs,
            )
            SettingsDivider()
            ActionRow(
                icon = OneBoxIcons.Upload,
                title = s.importLogs,
                subtitle = s.importLogsHint,
                onClick = onShareLogs,
            )
            SettingsDivider()
            ActionRow(
                icon = OneBoxIcons.Trash,
                title = s.clearLogs,
                subtitle = s.clearLogsHint,
                tint = colors.error,
                iconBackground = colors.errorContainer,
                onClick = {
                    onClearLogs()
                    notice = s.logsCleared
                },
            )
        }

        SettingsSection(title = s.about, icon = OneBoxIcons.Info) {
            InfoRow(
                icon = OneBoxIcons.Info,
                title = s.appVersion,
                value = appVersion,
            )
            SettingsDivider()
            // Точная версия ядра приходит от нативного xray-core через прослойку
            InfoRow(
                icon = OneBoxIcons.Terminal,
                title = s.coreVersion,
                value = coreVersion,
            )
            SettingsDivider()
            InfoRow(
                icon = OneBoxIcons.Folder,
                title = s.activeSubscription,
                value = activeSubscription ?: s.configMode,
            )
            SettingsDivider()
            ActionRow(
                icon = OneBoxIcons.Refresh,
                title = s.resetSettings,
                subtitle = s.resetSettingsHint,
                tint = colors.error,
                iconBackground = colors.errorContainer,
                onClick = {
                    onResetSettings()
                    notice = s.resetDone
                },
            )
        }

        notice?.let { message ->
            Spacer(Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = colors.primaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = OneBoxIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colors.primary,
                    )
                    Text(
                        text = message,
                        style = OneBoxTheme.textStyles.footnote2,
                        color = colors.primary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Секция настроек: подпись сверху и карточка с содержимым. */
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    SectionTitle(text = title, icon = icon)
    Spacer(Modifier.height(10.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(0.dp),
    ) {
        Column(content = content)
    }
    Spacer(Modifier.height(24.dp))
}

/** Секция настроек: подпись сверху и карточка с содержимым. */
@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = OneBoxTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Строка-действие со стрелкой справа. */
@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = OneBoxTheme.colorScheme.primary,
    iconBackground: Color = OneBoxTheme.colorScheme.primaryContainer,
) {
    val colors = OneBoxTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(
            icon = icon,
            tint = tint,
            background = iconBackground,
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
        Icon(
            imageVector = OneBoxIcons.ArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.onSurfaceContainerVariant,
        )
    }
}

/** Информационная строка: название слева, значение справа. */
@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String,
) {
    val colors = OneBoxTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(
            icon = icon,
            tint = colors.onSurfaceContainerVariant,
            background = colors.surfaceContainerHigh,
            size = 38.dp,
            iconSize = 19.dp,
            corner = 12.dp,
        )
        Text(
            text = title,
            style = OneBoxTheme.textStyles.headline2,
            color = colors.onSurfaceContainer,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 14.dp),
        )
        Text(
            text = value,
            style = OneBoxTheme.textStyles.footnote2.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
            color = colors.onSurfaceContainerVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingsDivider(startIndent: androidx.compose.ui.unit.Dp = 58.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startIndent),
        color = OneBoxTheme.colorScheme.dividerLine,
    )
}
