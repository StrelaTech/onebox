package com.strela.onebox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.strela.onebox.i18n.OneBoxStrings
import com.strela.onebox.i18n.RuStrings

/** Тексты текущего языка. Провайдер живёт в UI-модуле: core остаётся без Compose. */
val LocalStrings = staticCompositionLocalOf<OneBoxStrings> { RuStrings }

val strings: OneBoxStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalStrings.current
