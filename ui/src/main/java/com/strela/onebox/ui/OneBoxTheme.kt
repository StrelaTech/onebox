package com.strela.onebox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.stringsFor
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * Тема приложения на Miuix. Material Design не используется:
 * цвета, типографика и компоненты берутся из HyperOS-набора Miuix.
 *
 * Акцент — монохром вместо стандартного голубого:
 * чёрный на светлой теме и белый на тёмной.
 */

private val LightMono: Colors = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryVariant = Color.Black,
    onPrimaryVariant = Color.White,
    primaryContainer = Color(0xFFE3E3E3),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryVariant = Color.Black,
    onSecondaryVariant = Color.White,
    secondaryContainer = Color(0xFFEDEDED),
    onSecondaryContainer = Color.Black,
    tertiaryContainer = Color(0xFFE3E3E3),
    onTertiaryContainer = Color.Black,
    disabledPrimary = Color(0xFFB3B3B3),
    disabledOnPrimary = Color.White,
    disabledPrimaryButton = Color(0xFFC7C7C7),
    disabledOnPrimaryButton = Color.White,
)

private val DarkMono: Colors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryVariant = Color.White,
    onPrimaryVariant = Color.Black,
    primaryContainer = Color(0xFF2C2C2C),
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryVariant = Color.White,
    onSecondaryVariant = Color.Black,
    secondaryContainer = Color(0xFF262626),
    onSecondaryContainer = Color.White,
    tertiaryContainer = Color(0xFF2C2C2C),
    onTertiaryContainer = Color.White,
    disabledPrimary = Color(0xFF4A4A4A),
    disabledOnPrimary = Color(0xFF9A9A9A),
    disabledPrimaryButton = Color(0xFF3A3A3A),
    disabledOnPrimaryButton = Color(0xFF8A8A8A),
)

@Composable
fun OneBoxTheme(
    darkTheme: Boolean,
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val controller = remember(darkTheme) {
        ThemeController(
            colorSchemeMode = if (darkTheme) ColorSchemeMode.Dark else ColorSchemeMode.Light,
            lightColors = LightMono,
            darkColors = DarkMono,
        )
    }

    MiuixTheme(controller = controller) {
        CompositionLocalProvider(LocalStrings provides stringsFor(language)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.background),
            ) {
                content()
            }
        }
    }
}
