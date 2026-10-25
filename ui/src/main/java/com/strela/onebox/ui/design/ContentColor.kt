package com.strela.onebox.ui.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Основной цвет содержимого (текст/иконки). Контейнеры (Surface, Button,
 * IconButton) переопределяют его для своих детей.
 */
val LocalContentColor = staticCompositionLocalOf { Color.Unspecified }
