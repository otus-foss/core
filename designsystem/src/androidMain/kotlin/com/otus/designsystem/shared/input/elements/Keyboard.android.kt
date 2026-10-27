package com.otus.designsystem.shared.input.elements

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

// Where the keyboard's animation ends, so a closing keyboard counts as down from its first frame.
@OptIn(ExperimentalLayoutApi::class)
@Composable
public actual fun isKeyboardUp(): Boolean = WindowInsets.imeAnimationTarget.getBottom(LocalDensity.current) > 0
