package com.otus.designsystem.shared.input.elements

import androidx.compose.runtime.Composable

/**
 * Whether the keyboard is open or opening. It turns false as soon as the keyboard starts to
 * close, so what replaces it, such as a page's bar, moves along with the keyboard instead of
 * after it.
 */
@Composable
public expect fun isKeyboardUp(): Boolean
