package com.strela.onebox.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.strings
import com.strela.onebox.settings.ThemeMode
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconTile
import com.strela.onebox.ui.design.LocalOneBoxTypography
import com.strela.onebox.ui.design.Palette
import com.strela.onebox.ui.design.Panel
import com.strela.onebox.ui.design.SectionLabel
import com.strela.onebox.ui.design.Switch
import com.strela.onebox.ui.design.icons.OneBoxIcons
import kotlinx.coroutines.delay

/**
 * Экран «Настройки»: внешний вид, язык, параметры подключения,
 * диагностика и сведения о приложении.
 */
@Composable
fun SettingsScreen(
    theme: ThemeMode,
    language: AppLanguage,
    blockAds: Boolean,
    autoConnect: Boolean,
    coreVersion: String,
    activeProfile: String?,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onBlockAdsChange: (Boolean) -> Unit,
    onAutoConnectChange: (Boolean) -> Unit,
    onShareLogs: () -> Unit,
    onClearLogs: () -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typo = LocalOneBoxTypography.current
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
            .verticalScroll(rememberScrollState())
    ) {
        // Короткая подсказка вместо большого дублирующего заголовка.
        Panel(
            modifier = Modifier.fillMaxWidth(),
            padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon = OneBoxIcons.Info,
                    modifier = Modifier.size(14.dp),
                    tint = Palette.AccentPrimary
                )
                BasicText(
                    text = s.settingsHint,
                    style = typo.caption.copy(color = Palette.TextMuted),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        SettingsSection(title = s.appearance, icon = OneBoxIcons.Sun) {
            PreferenceBlock(title = s.themeTitle) {
                SegmentedControl(
                    options = listOf(
                        SegmentOption(ThemeMode.LIGHT, s.themeLight, OneBoxIcons.Sun),
                        SegmentOption(ThemeMode.DARK, s.themeDark, OneBoxIcons.Moon),
                        SegmentOption(ThemeMode.SYSTEM, s.themeSystemShort, OneBoxIcons.Monitor)
                    ),
                    selected = theme,
                    onSelect = onThemeChange
                )
            }

            SettingsDivider(startIndent = 14.dp)

            PreferenceBlock(title = s.language, subtitle = s.languageHint) {
                SegmentedControl(
                    options = listOf(
                        SegmentOption(AppLanguage.RU, s.languageRussian),
                        SegmentOption(AppLanguage.EN, s.languageEnglish)
                    ),
                    selected = language,
                    onSelect = onLanguageChange
                )
            }
        }

        SettingsSection(title = s.connection, icon = OneBoxIcons.Zap) {
            ToggleRow(
                icon = OneBoxIcons.Zap,
                title = s.blockAds,
                subtitle = s.blockAdsHint,
                checked = blockAds,
                onCheckedChange = onBlockAdsChange
            )
            SettingsDivider()
            ToggleRow(
                icon = OneBoxIcons.Refresh,
                title = s.autoConnect,
                subtitle = s.autoConnectHint,
                checked = autoConnect,
                onCheckedChange = onAutoConnectChange
            )
        }

        SettingsSection(title = s.diagnostics, icon = OneBoxIcons.Terminal) {
            BasicText(
                text = s.logsHint,
                style = typo.caption.copy(color = Palette.TextMuted),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
            SettingsDivider(startIndent = 14.dp)
            ActionRow(
                icon = OneBoxIcons.Share,
                title = s.shareLogs,
                subtitle = s.shareLogsHint,
                onClick = onShareLogs
            )
            SettingsDivider()
            ActionRow(
                icon = OneBoxIcons.Trash,
                title = s.clearLogs,
                subtitle = s.clearLogsHint,
                tint = Palette.Error,
                iconBackground = Palette.ErrorSoft,
                onClick = {
                    onClearLogs()
                    notice = s.logsCleared
                }
            )
        }

        SettingsSection(title = s.about, icon = OneBoxIcons.Info) {
            InfoRow(
                icon = OneBoxIcons.Info,
                title = s.appVersion,
                value = appVersion
            )
            SettingsDivider()
            InfoRow(
                icon = OneBoxIcons.Terminal,
                title = s.coreVersion,
                value = coreVersion
            )
            SettingsDivider()
            InfoRow(
                icon = OneBoxIcons.Folder,
                title = s.activeProfile,
                value = activeProfile ?: s.configMode
            )
            SettingsDivider()
            ActionRow(
                icon = OneBoxIcons.Refresh,
                title = s.resetSettings,
                subtitle = s.resetSettingsHint,
                tint = Palette.Error,
                iconBackground = Palette.ErrorSoft,
                onClick = {
                    onResetSettings()
                    notice = s.resetDone
                }
            )
        }

        notice?.let { message ->
            NoticeChip(text = message)
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Секция настроек: подпись сверху и карточка с содержимым. */
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    SectionLabel(text = title, icon = icon)
    Spacer(Modifier.height(8.dp))
    Panel(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(0.dp)
    ) {
        Column(content = content)
    }
    Spacer(Modifier.height(18.dp))
}

/** Блок с заголовком и содержимым внутри карточки. */
@Composable
private fun PreferenceBlock(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    val typo = LocalOneBoxTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        BasicText(
            text = title,
            style = typo.heading.copy(color = Palette.TextPrimary)
        )
        if (!subtitle.isNullOrBlank()) {
            BasicText(
                text = subtitle,
                style = typo.caption.copy(color = Palette.TextMuted),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(Modifier.height(9.dp))
        content()
    }
}

/** Компактный переключатель из нескольких вариантов. */
private data class SegmentOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null
)

@Composable
private fun <T> SegmentedControl(
    options: List<SegmentOption<T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    val typo = LocalOneBoxTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.SurfaceElevated)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { option ->
            val active = option.value == selected
            val shape = RoundedCornerShape(9.dp)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(if (active) Palette.Surface else Color.Transparent)
                    .then(
                        if (active) {
                            Modifier.border(
                                BorderStroke(1.dp, Palette.AccentPrimary.copy(alpha = 0.5f)),
                                shape
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onSelect(option.value) }
                    .padding(horizontal = 4.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                option.icon?.let { icon ->
                    Icon(
                        icon = icon,
                        modifier = Modifier.size(14.dp),
                        tint = if (active) Palette.AccentPrimary else Palette.TextMuted
                    )
                    Spacer(Modifier.width(5.dp))
                }
                BasicText(
                    text = option.label,
                    style = typo.caption.copy(
                        color = if (active) Palette.TextPrimary else Palette.TextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Строка с переключателем: иконка, заголовок, пояснение. */
@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val typo = LocalOneBoxTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon = icon,
            size = 32.dp,
            iconSize = 16.dp,
            tint = if (checked) Palette.AccentPrimary else Palette.TextSecondary,
            background = if (checked) Palette.AccentSoft else Palette.SurfaceElevated,
            corner = 9.dp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp)
        ) {
            BasicText(
                text = title,
                style = typo.heading.copy(color = Palette.TextPrimary)
            )
            BasicText(
                text = subtitle,
                style = typo.caption.copy(color = Palette.TextMuted),
                modifier = Modifier.padding(top = 2.dp)
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
    tint: Color = Palette.AccentPrimary,
    iconBackground: Color = Palette.AccentSoft
) {
    val typo = LocalOneBoxTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon = icon,
            size = 32.dp,
            iconSize = 16.dp,
            tint = tint,
            background = iconBackground,
            corner = 9.dp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp)
        ) {
            BasicText(
                text = title,
                style = typo.heading.copy(color = Palette.TextPrimary)
            )
            BasicText(
                text = subtitle,
                style = typo.caption.copy(color = Palette.TextMuted),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            icon = OneBoxIcons.ArrowRight,
            modifier = Modifier.size(16.dp),
            tint = Palette.TextMuted
        )
    }
}

/** Информационная строка: название слева, значение справа. */
@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    val typo = LocalOneBoxTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon = icon,
            size = 32.dp,
            iconSize = 16.dp,
            tint = Palette.TextSecondary,
            background = Palette.SurfaceElevated,
            corner = 9.dp
        )
        BasicText(
            text = title,
            style = typo.heading.copy(color = Palette.TextPrimary),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp)
        )
        BasicText(
            text = value,
            style = typo.mono.copy(color = Palette.TextSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsDivider(startIndent: Dp = 58.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startIndent)
            .height(1.dp)
            .background(Palette.Border.copy(alpha = 0.65f))
    )
}

/** Всплывающее подтверждение действия. */
@Composable
private fun NoticeChip(text: String) {
    val typo = LocalOneBoxTypography.current
    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Palette.RunningSoft)
            .border(BorderStroke(1.dp, Palette.Running.copy(alpha = 0.35f)), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon = OneBoxIcons.Check,
            modifier = Modifier.size(14.dp),
            tint = Palette.Running
        )
        BasicText(
            text = text,
            style = typo.caption.copy(color = Palette.Running),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
