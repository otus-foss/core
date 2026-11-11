package com.otusfoss.core.designsystem.shared.input.elements

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

// iOS has no animation target, so a closing keyboard counts as up until it is gone.
@Composable
public actual fun isKeyboardUp(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0
