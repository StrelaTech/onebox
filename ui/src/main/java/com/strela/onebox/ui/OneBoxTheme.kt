package com.strela.onebox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.strela.onebox.i18n.AppLanguage
import com.strela.onebox.i18n.OneBoxStrings
import com.strela.onebox.i18n.RuStrings
import com.strela.onebox.i18n.stringsFor
import com.strela.sui.SuiTheme
import com.strela.sui.tokens.SuiColorScheme
import com.strela.sui.tokens.SuiTextStyles

/**
 * Мост между SUI-китом и i18n OneBox.
 *
 * Сам дизайн-кит больше не знает о языках: он принимает только тему и масштаб
 * текста. OneBox хранит свои переводы в :core-i18n, поэтому провайдер строк
 * живёт здесь, в UI-модуле, а компоненты читают его через [strings].
 */
val LocalStrings = staticCompositionLocalOf<OneBoxStrings> { RuStrings }

val strings: OneBoxStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalStrings.current

/** Доступ к токенам SUI под привычным для OneBox именем. */
object OneBoxTheme {
    val colorScheme: SuiColorScheme
        @Composable
        @ReadOnlyComposable
        get() = SuiTheme.colorScheme

    val textStyles: SuiTextStyles
        @Composable
        @ReadOnlyComposable
        get() = SuiTheme.textStyles
}

@Composable
fun OneBoxTheme(
    darkTheme: Boolean,
    language: AppLanguage,
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    SuiTheme(darkTheme = darkTheme, fontScale = fontScale) {
        CompositionLocalProvider(
            LocalStrings provides stringsFor(language),
            content = content,
        )
    }
}
