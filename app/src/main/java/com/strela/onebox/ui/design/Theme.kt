package com.strela.onebox.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.LocalStrings
import com.strela.onebox.i18n.stringsFor

/**
 * Тема OneBox. Material Design не используется: только foundation/ui,
 * собственная палитра и типографика через CompositionLocal.
 */
@Composable
fun OneBoxTheme(
    darkTheme: Boolean = true,
    language: AppLanguage = AppLanguage.RU,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette

    CompositionLocalProvider(
        LocalPalette provides palette,
        LocalOneBoxTypography provides OneBoxTypography(),
        LocalStrings provides stringsFor(language)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(palette.BackgroundTop, palette.Background, palette.BackgroundBottom)
                    )
                )
        ) {
            content()
        }
    }
}
