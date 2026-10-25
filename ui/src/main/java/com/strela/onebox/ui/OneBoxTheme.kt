package com.strela.onebox.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.stringsFor
import com.strela.onebox.ui.design.LocalContentColor
import com.strela.onebox.ui.design.PressIndication

/**
 * Тема приложения на чистом Jetpack Compose (foundation + ui).
 * Material (androidx.compose.material*) не используется: цвета, типографика
 * и компоненты живут в пакете ui.design.
 *
 * Акцент — монохром вместо стандартного голубого:
 * чёрный на светлой теме и белый на тёмной.
 */

/** Палитра OneBox: нейтральные поверхности + монохромный акцент. */
@Immutable
data class OneBoxColors(
    val background: Color,
    val onBackground: Color,
    val onBackgroundVariant: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurfaceContainer: Color,
    val onSurfaceContainerVariant: Color,
    val onSurfaceVariantSummary: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val disabledPrimary: Color,
    val disabledOnPrimary: Color,
    val disabledPrimaryButton: Color,
    val disabledOnPrimaryButton: Color,
    val dividerLine: Color,
    val switchTrackUnchecked: Color,
    val switchThumbUnchecked: Color,
    val pressedOverlay: Color,
)

private val LightMono: OneBoxColors = OneBoxColors(
    background = Color.White,
    onBackground = Color.Black,
    onBackgroundVariant = Color(0xFF6E6E73),
    surfaceContainer = Color(0xFFF4F4F6),
    surfaceContainerHigh = Color(0xFFEAEAED),
    onSurfaceContainer = Color(0xFF1C1C1E),
    onSurfaceContainerVariant = Color(0xFF6E6E73),
    onSurfaceVariantSummary = Color(0xFF8E8E93),
    // монохром: чёрная заливка, белый текст
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E3E3),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDEDED),
    onSecondaryContainer = Color.Black,
    error = Color(0xFFFF3B30),
    errorContainer = Color(0xFFFFEAE8),
    disabledPrimary = Color(0xFFB3B3B3),
    disabledOnPrimary = Color.White,
    disabledPrimaryButton = Color(0xFFC7C7C7),
    disabledOnPrimaryButton = Color.White,
    dividerLine = Color(0xFFE3E3E4),
    switchTrackUnchecked = Color(0xFFE3E3E6),
    switchThumbUnchecked = Color.White,
    pressedOverlay = Color(0x14000000),
)

private val DarkMono: OneBoxColors = OneBoxColors(
    background = Color(0xFF242424),
    onBackground = Color.White,
    onBackgroundVariant = Color(0xFF9A9A9E),
    surfaceContainer = Color(0xFF2D2D2F),
    surfaceContainerHigh = Color(0xFF38383B),
    onSurfaceContainer = Color(0xFFF2F2F4),
    onSurfaceContainerVariant = Color(0xFF9A9A9E),
    onSurfaceVariantSummary = Color(0xFF8E8E93),
    // монохром: белая заливка, чёрный текст
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF38383A),
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF303033),
    onSecondaryContainer = Color.White,
    error = Color(0xFFFF453A),
    errorContainer = Color(0xFF3C1D1A),
    disabledPrimary = Color(0xFF4A4A4A),
    disabledOnPrimary = Color(0xFF9A9A9A),
    disabledPrimaryButton = Color(0xFF3A3A3A),
    disabledOnPrimaryButton = Color(0xFF8A8A8A),
    dividerLine = Color(0xFF3A3A3C),
    switchTrackUnchecked = Color(0xFF464649),
    switchThumbUnchecked = Color(0xFFAEADB3),
    pressedOverlay = Color(0x14FFFFFF),
)

/** Типографика OneBox. Цвет текста не зашит — Text берёт его из LocalContentColor. */
@Immutable
data class OneBoxTextStyles(
    val headline1: TextStyle,
    val headline2: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val footnote1: TextStyle,
    val footnote2: TextStyle,
    val button: TextStyle,
)

private fun defaultOneBoxTextStyles() = OneBoxTextStyles(
    headline1 = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    headline2 = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    body1 = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    body2 = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Normal),
    footnote1 = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
    footnote2 = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    button = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
)

val LocalOneBoxColors = staticCompositionLocalOf { LightMono }
val LocalOneBoxTextStyles = staticCompositionLocalOf { defaultOneBoxTextStyles() }

/** Доступ к теме: OneBoxTheme.colorScheme / OneBoxTheme.textStyles. */
object OneBoxTheme {
    val colorScheme: OneBoxColors
        @Composable
        @ReadOnlyComposable
        get() = LocalOneBoxColors.current

    val textStyles: OneBoxTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalOneBoxTextStyles.current
}

@Composable
fun OneBoxTheme(
    darkTheme: Boolean,
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkMono else LightMono
    val indication = remember(colors) { PressIndication(colors.pressedOverlay) }

    CompositionLocalProvider(
        LocalOneBoxColors provides colors,
        LocalOneBoxTextStyles provides defaultOneBoxTextStyles(),
        LocalContentColor provides colors.onBackground,
        LocalIndication provides indication,
        LocalStrings provides stringsFor(language),
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
