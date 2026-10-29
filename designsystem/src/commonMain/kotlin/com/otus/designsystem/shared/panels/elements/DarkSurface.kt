package com.otus.designsystem.shared.panels.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/** A `bgDark` surface inside a `strokeBgDark` outline, both in [shape]. */
internal fun Modifier.darkSurface(shape: Shape): Modifier =
    background(OtusColors.bgDark, shape).border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
