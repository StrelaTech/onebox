package com.strela.onebox.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
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
import com.strela.onebox.api.ConnectionMode
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.log.AppLog
import com.strela.onebox.subscription.SubscriptionStore
import com.strela.onebox.settings.SettingsStore
import com.strela.onebox.settings.ThemeMode
import com.strela.onebox.api.VpnConnector
import com.strela.onebox.ui.OneBoxTheme
import com.strela.onebox.ui.design.Card
import com.strela.onebox.ui.design.HorizontalDivider
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.SmallTopAppBar
import com.strela.onebox.ui.design.Surface
import com.strela.onebox.ui.design.Switch
import com.strela.onebox.ui.design.Text
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.ui.kit.DropdownRow
import com.strela.onebox.ui.kit.IconTile
import com.strela.onebox.ui.kit.SectionTitle
import kotlinx.coroutines.delay

/**
 * Экран «Настройки» — хаб из четырёх секций; каждая открывает свой экран:
 * внешний вид, подключение, диагностика, о приложении.
 */
@Composable
fun SettingsScreen(
    onOpenAppearance: () -> Unit,
    onOpenConnection: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionCard(
            title = s.appearance,
            subtitle = s.appearanceHint,
            icon = OneBoxIcons.Sun,
            onClick = onOpenAppearance,
        )
        SectionCard(
            title = s.connection,
            subtitle = s.connectionHint,
            icon = OneBoxIcons.Zap,
            onClick = onOpenConnection,
        )
        SectionCard(
            title = s.diagnostics,
            subtitle = s.diagnosticsHint,
            icon = OneBoxIcons.Terminal,
            onClick = onOpenDiagnostics,
        )
        SectionCard(
            title = s.about,
            subtitle = s.aboutHint,
            icon = OneBoxIcons.Info,
            onClick = onOpenAbout,
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** Кнопка-секция настроек: карточка с иконкой, названием и стрелкой. */
@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val colors = OneBoxTheme.colorScheme

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconTile(
                icon = icon,
                tint = colors.primary,
                background = colors.primaryContainer,
                size = 38.dp,
                iconSize = 19.dp,
                corner = 12.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
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
    Spacer(Modifier.height(14.dp))
}

// ── Внешний вид ────────────────────────────────────────────────────────────

/** Экран «Внешний вид»: тема оформления, размер текста, язык. */
@Composable
fun AppearanceSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val theme by SettingsStore.theme.collectAsState()
    val language by SettingsStore.language.collectAsState()
    val fontScale by SettingsStore.fontScale.collectAsState()

    SettingsScreenScaffold(title = strings.appearance, onClose = onClose, modifier = modifier) {
        val themeOptions = listOf(
            ThemeMode.LIGHT to strings.themeLight,
            ThemeMode.DARK to strings.themeDark,
            ThemeMode.SYSTEM to strings.themeSystemShort,
        )
        DropdownRow(
            icon = OneBoxIcons.Sun,
            title = strings.themeTitle,
            subtitle = strings.settingsHint,
            selectedLabel = themeOptions.firstOrNull { it.first == theme }?.second
                ?: strings.themeSystemShort,
            options = themeOptions.map { (mode, label) ->
                label to { SettingsStore.setTheme(context, mode) }
            },
        )

        SettingsDivider(startIndent = 18.dp)

        val textSizeOptions = listOf(
            SettingsStore.FONT_SCALE_SMALL to strings.textSizeSmall,
            SettingsStore.FONT_SCALE_NORMAL to strings.textSizeNormal,
            SettingsStore.FONT_SCALE_LARGE to strings.textSizeLarge,
            SettingsStore.FONT_SCALE_HUGE to strings.textSizeHuge,
        )
        DropdownRow(
            icon = OneBoxIcons.FormatSize,
            title = strings.textSize,
            subtitle = strings.textSizeHint,
            selectedLabel = textSizeOptions.firstOrNull { it.first == fontScale }?.second
                ?: strings.textSizeNormal,
            options = textSizeOptions.map { (scale, label) ->
                label to { SettingsStore.setFontScale(context, scale) }
            },
        )

        SettingsDivider(startIndent = 18.dp)

        val languageOptions = listOf(
            AppLanguage.RU to strings.languageRussian,
            AppLanguage.EN to strings.languageEnglish,
        )
        DropdownRow(
            icon = OneBoxIcons.Chat,
            title = strings.language,
            selectedLabel = languageOptions.firstOrNull { it.first == language }?.second
                ?: strings.languageRussian,
            options = languageOptions.map { (lang, label) ->
                label to { SettingsStore.setLanguage(context, lang) }
            },
        )
    }
}

// ── Подключение ────────────────────────────────────────────────────────────

/** Экран «Подключение»: режим трафика, реклама, автозапуск, пинг. */
@Composable
fun ConnectionSettingsScreen(
    onClose: () -> Unit,
    onOpenPing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val s = strings
    val blockAds by SettingsStore.blockAds.collectAsState()
    val autoConnect by SettingsStore.autoConnect.collectAsState()
    val mode by SettingsStore.connectionMode.collectAsState()

    SettingsScreenScaffold(title = s.connection, onClose = onClose, modifier = modifier) {
        val modeOptions = listOf(
            ConnectionMode.TUN to s.modeTun,
            ConnectionMode.PROXY to s.modeProxy,
            ConnectionMode.TUN_PLUS_PROXY to s.modeTunPlusProxy,
        )
        DropdownRow(
            icon = OneBoxIcons.Zap,
            title = s.connectionMode,
            subtitle = s.connectionModeHint,
            selectedLabel = modeOptions.firstOrNull { it.first == mode }?.second
                ?: s.modeTun,
            options = modeOptions.map { (m, label) ->
                label to { SettingsStore.setConnectionMode(context, m) }
            },
        )
        SettingsDivider(startIndent = 18.dp)

        ToggleRow(
            icon = OneBoxIcons.Close,
            title = s.blockAds,
            subtitle = s.blockAdsHint,
            checked = blockAds,
            onCheckedChange = { SettingsStore.setBlockAds(context, it) },
        )
        SettingsDivider(startIndent = 58.dp)
        ToggleRow(
            icon = OneBoxIcons.Refresh,
            title = s.autoConnect,
            subtitle = s.autoConnectHint,
            checked = autoConnect,
            onCheckedChange = { SettingsStore.setAutoConnect(context, it) },
        )
        SettingsDivider(startIndent = 58.dp)
        ActionRow(
            icon = OneBoxIcons.Wifi,
            title = s.settingsPing,
            subtitle = s.settingsPingHint,
            onClick = onOpenPing,
        )
    }
}

// ── Диагностика ────────────────────────────────────────────────────────────

/** Экран «Диагностика»: журнал приложения и его обслуживание. */
@Composable
fun DiagnosticsSettingsScreen(
    onClose: () -> Unit,
    onOpenLogs: () -> Unit,
    onShareLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings
    val colors = OneBoxTheme.colorScheme

    var notice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(notice) {
        if (notice != null) {
            delay(2600)
            notice = null
        }
    }

    SettingsScreenScaffold(title = s.diagnostics, onClose = onClose, modifier = modifier) {
        Text(
            text = s.logsHint,
            style = OneBoxTheme.textStyles.footnote2,
            color = colors.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        )
        SettingsDivider(startIndent = 18.dp)
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
                AppLog.clear()
                notice = s.logsCleared
            },
        )
        NoticeBar(notice)
    }
}

// ── О приложении ───────────────────────────────────────────────────────────

/** Экран «О приложении»: версия, ядро, GitHub, Telegram, лицензии, сброс. */
@Composable
fun AboutSettingsScreen(
    onClose: () -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val s = strings
    val colors = OneBoxTheme.colorScheme
    val coreVersion by VpnConnector.version.collectAsState()

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

    SettingsScreenScaffold(title = s.about, onClose = onClose, modifier = modifier) {
        Text(
            text = s.aboutDescription,
            style = OneBoxTheme.textStyles.footnote2,
            color = colors.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        )
        SettingsDivider(startIndent = 18.dp)
        InfoRow(
            icon = OneBoxIcons.Info,
            title = s.appVersion,
            value = appVersion,
        )
        SettingsDivider()
        InfoRow(
            icon = OneBoxIcons.Terminal,
            title = s.coreVersion,
            value = coreVersion,
        )
        SettingsDivider()
        LinkRow(
            icon = OneBoxIcons.Zap,
            title = s.githubProject,
            subtitle = s.githubProjectHint,
            url = "https://github.com/StrelaTech/onebox",
        )
        SettingsDivider(startIndent = 58.dp)
        LinkRow(
            icon = OneBoxIcons.Chat,
            title = s.telegramChannel,
            subtitle = s.telegramChannelHint,
            url = "https://t.me/StrelaTech",
        )
        SettingsDivider(startIndent = 58.dp)
        ActionRow(
            icon = OneBoxIcons.File,
            title = s.licensesTitle,
            subtitle = s.licensesHint,
            onClick = onOpenLicenses,
        )
        SettingsDivider(startIndent = 58.dp)
        ActionRow(
            icon = OneBoxIcons.Refresh,
            title = s.resetSettings,
            subtitle = s.resetSettingsHint,
            tint = colors.error,
            iconBackground = colors.errorContainer,
            onClick = {
                SettingsStore.reset(context)
                SubscriptionStore.clearAll(context)
                VpnConnector.clearUserConfig()
                notice = s.resetDone
            },
        )
        NoticeBar(notice)
    }
}

/** Экран «Лицензии open-source»: состав кода и библиотек. */
@Composable
fun LicensesScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = strings
    val colors = OneBoxTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        SmallTopAppBar(
            title = s.licensesTitle,
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = OneBoxIcons.ArrowLeft,
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
                .padding(horizontal = 22.dp, vertical = 14.dp),
        ) {
            Text(
                text = s.licensesDescription,
                style = OneBoxTheme.textStyles.body2,
                color = colors.onSurfaceContainerVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Общие построители ──────────────────────────────────────────────────────

/** Каркас подэкрана настроек: appbar со стрелкой назад и скроллящееся тело. */
@Composable
private fun SettingsScreenScaffold(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val s = strings
    val colors = OneBoxTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        SmallTopAppBar(
            title = title,
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = OneBoxIcons.ArrowLeft,
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
                .padding(vertical = 14.dp),
            content = content,
        )
    }
}

/** Полоска краткого уведомления (действие выполнено). */
@Composable
private fun NoticeBar(notice: String?) {
    if (notice == null) return
    val colors = OneBoxTheme.colorScheme

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
                text = notice,
                style = OneBoxTheme.textStyles.footnote2,
                color = colors.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
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

/** Строка-переключатель с подписью. */
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

/** Ссылка (сайт/канал): открывается во внешнем браузере. */
@Composable
private fun LinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    url: String,
) {
    val context = LocalContext.current
    val colors = OneBoxTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openUrl(context, url) }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(
            icon = icon,
            tint = colors.primary,
            background = colors.primaryContainer,
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

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
