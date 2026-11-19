package com.strela.onebox.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.OneBoxStrings
import com.strela.onebox.i18n.RuStrings
import com.strela.onebox.i18n.stringsFor
import com.strela.onebox.settings.ThemeMode
import com.strela.sui.LocalSuiColors
import com.strela.sui.LocalSuiTextStyles
import com.strela.sui.design.LocalContentColor
import com.strela.sui.design.PressIndication
import com.strela.sui.tokens.SuiColorScheme
import com.strela.sui.tokens.SuiTextStyles
import com.strela.sui.tokens.SuiTokens

/**
 * Мост между SUI-китом и i18n OneBox.
 *
 * Сам дизайн-кит не знает о языках: он принимает только тему и масштаб
 * текста. OneBox хранит свои переводы в :core-i18n, поэтому провайдер строк
 * живёт здесь, в UI-модуле, а компоненты читают его через [strings].
 */
val LocalStrings = staticCompositionLocalOf<OneBoxStrings> { RuStrings }

val strings: OneBoxStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalStrings.current

/** Базовое уменьшение типографики на 10 % (17→15, 14→13, 13→12, 12→11, 11→10, 10→9 sp). */
const val BASE_FONT_SCALE = 0.9f

/** Доступ к токенам SUI под привычным для OneBox именем. */
object OneBoxTheme {
    val colorScheme: SuiColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalSuiColors.current

    val textStyles: SuiTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalSuiTextStyles.current
}

/** Зелёная тёмная палитра: нейтральные поверхности + зелёный акцент. */
private val GreenColors = SuiColorScheme(
    background = Color(0xFF0B1510),
    onBackground = Color(0xFFE8F5EC),
    onBackgroundVariant = Color(0xFF8FAE9B),
    surfaceContainer = Color(0xFF14231A),
    surfaceContainerHigh = Color(0xFF1D3226),
    onSurfaceContainer = Color(0xFFEAF7EF),
    onSurfaceContainerVariant = Color(0xFF93B5A0),
    onSurfaceVariantSummary = Color(0xFF7E9E8B),
    primary = Color(0xFF34C77B),
    onPrimary = Color(0xFF04130A),
    primaryContainer = Color(0xFF1D4A31),
    onPrimaryContainer = Color(0xFFD6F5E3),
    secondary = Color(0xFF34C77B),
    onSecondary = Color(0xFF04130A),
    secondaryContainer = Color(0xFF173828),
    onSecondaryContainer = Color(0xFFD6F5E3),
    error = Color(0xFFFF5C55),
    errorContainer = Color(0xFF3A1715),
    warning = Color(0xFFE7C15A),
    disabledPrimary = Color(0xFF2E4A3B),
    disabledOnPrimary = Color(0xFF7D9A8A),
    disabledPrimaryButton = Color(0xFF22382C),
    disabledOnPrimaryButton = Color(0xFF7D9A8A),
    dividerLine = Color(0xFF243B2E),
    switchTrackUnchecked = Color(0xFF2C4436),
    switchThumbUnchecked = Color(0xFF9BB8A8),
    pressedOverlay = Color(0x1F34C77B),
)

/** AMOLED-чёрная палитра: чистый чёрный фон, поверхности минимально светлее. */
private val AmoledColors = SuiColorScheme(
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onBackgroundVariant = Color(0xFF8A8A8E),
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerHigh = Color(0xFF141414),
    onSurfaceContainer = Color(0xFFF5F5F5),
    onSurfaceContainerVariant = Color(0xFF8A8A8E),
    onSurfaceVariantSummary = Color(0xFF6E6E73),
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E1E1E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF141414),
    onSecondaryContainer = Color(0xFFFFFFFF),
    error = Color(0xFFFF453A),
    errorContainer = Color(0xFF2A0F0D),
    warning = Color(0xFFE7A23E),
    disabledPrimary = Color(0xFF333333),
    disabledOnPrimary = Color(0xFF777777),
    disabledPrimaryButton = Color(0xFF222222),
    disabledOnPrimaryButton = Color(0xFF666666),
    dividerLine = Color(0xFF1F1F1F),
    switchTrackUnchecked = Color(0xFF2A2A2A),
    switchThumbUnchecked = Color(0xFF9A9A9A),
    pressedOverlay = Color(0x14FFFFFF),
)

@Composable
fun OneBoxTheme(
    themeMode: ThemeMode,
    systemDark: Boolean,
    language: AppLanguage,
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val resolvedLanguage = AppLanguage.resolve(language)
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK, ThemeMode.GREEN, ThemeMode.AMOLED -> true
    }
    val colors = when (themeMode) {
        ThemeMode.GREEN -> GreenColors
        ThemeMode.AMOLED -> AmoledColors
        else -> if (darkTheme) SuiTokens.darkColors else SuiTokens.lightColors
    }
    val indication = remember(colors) { PressIndication(colors.pressedOverlay) }
    val textStyles = remember(fontScale) { SuiTokens.textStyles(fontScale * BASE_FONT_SCALE) }
    val layoutDirection = if (resolvedLanguage == AppLanguage.FA) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalSuiColors provides colors,
        LocalSuiTextStyles provides textStyles,
        LocalContentColor provides colors.onBackground,
        LocalIndication provides indication,
        LocalStrings provides stringsFor(resolvedLanguage),
        LocalLayoutDirection provides layoutDirection,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            content()
        }
    }
}
