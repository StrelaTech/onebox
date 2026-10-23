package com.strela.onebox.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Палитра OneBox. Material Design не используется — цвета хранятся в простом
 * data-классе, а текущая палитра доступна через [Palette].
 */
data class OneBoxPalette(
    // Фон и поверхности
    val Background: Color,
    val BackgroundTop: Color,
    val BackgroundBottom: Color,
    val Surface: Color,
    val SurfaceElevated: Color,
    val Border: Color,
    val BorderFocused: Color,

    // Текст
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextMuted: Color,

    // Акценты состояний
    val Running: Color,
    val Starting: Color,
    val Stopped: Color,
    val Error: Color,

    // Мягкие подложки под иконки/статусы
    val AccentSoft: Color,
    val RunningSoft: Color,
    val StartingSoft: Color,
    val ErrorSoft: Color,
    val StoppedSoft: Color,

    // Кнопки
    val AccentPrimary: Color,
    val AccentPrimaryPressed: Color,
    val OnAccent: Color,
    val ButtonOutline: Color,
    val ButtonDisabled: Color,

    // Поле ввода
    val FieldBackground: Color
)

/** Тёмная тема — исторический вид приложения. */
val DarkPalette = OneBoxPalette(
    Background = Color(0xFF0E1116),
    BackgroundTop = Color(0xFF10151D),
    BackgroundBottom = Color(0xFF0A0D12),
    Surface = Color(0xFF161B22),
    SurfaceElevated = Color(0xFF1C232C),
    Border = Color(0xFF2A333F),
    BorderFocused = Color(0xFF3D4C5C),

    TextPrimary = Color(0xFFFFFFFF),
    TextSecondary = Color(0xFFB3B3B3),
    TextMuted = Color(0xFF7A7A7A),

    Running = Color(0xFF3DD68C),
    Starting = Color(0xFFE3B341),
    Stopped = Color(0xFF6B7683),
    Error = Color(0xFFF85149),

    AccentSoft = Color(0x262F81F7),
    RunningSoft = Color(0x213DD68C),
    StartingSoft = Color(0x21E3B341),
    ErrorSoft = Color(0x21F85149),
    StoppedSoft = Color(0x216B7683),

    AccentPrimary = Color(0xFF2F81F7),
    AccentPrimaryPressed = Color(0xFF1F6FEB),
    OnAccent = Color(0xFFFFFFFF),
    ButtonOutline = Color(0xFF30363D),
    ButtonDisabled = Color(0xFF21262D),

    FieldBackground = Color(0xFF0D1117)
)

/** Светлая тема: белый фон и тёмный текст. */
val LightPalette = OneBoxPalette(
    Background = Color(0xFFF2F5F8),
    BackgroundTop = Color(0xFFFAFCFE),
    BackgroundBottom = Color(0xFFE8EDF3),
    Surface = Color(0xFFFFFFFF),
    SurfaceElevated = Color(0xFFF0F4F8),
    Border = Color(0xFFD0D7DE),
    BorderFocused = Color(0xFF8C959F),

    TextPrimary = Color(0xFF000000),
    TextSecondary = Color(0xFF4D4D4D),
    TextMuted = Color(0xFF707070),

    Running = Color(0xFF1A7F37),
    Starting = Color(0xFF9A6700),
    Stopped = Color(0xFF57606A),
    Error = Color(0xFFCF222E),

    AccentSoft = Color(0x1A1F6FEB),
    RunningSoft = Color(0x1A1A7F37),
    StartingSoft = Color(0x1A9A6700),
    ErrorSoft = Color(0x1ACF222E),
    StoppedSoft = Color(0x1A57606A),

    AccentPrimary = Color(0xFF1F6FEB),
    AccentPrimaryPressed = Color(0xFF1557B0),
    OnAccent = Color(0xFFFFFFFF),
    ButtonOutline = Color(0xFFD0D7DE),
    ButtonDisabled = Color(0xFFE7EBF0),

    FieldBackground = Color(0xFFF6F8FA)
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

/** Текущая палитра: тёмная или светлая, в зависимости от [OneBoxTheme]. */
val Palette: OneBoxPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalPalette.current
