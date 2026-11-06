package com.otus.designsystem.shared.panels.elements

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.strokeBorder
import com.otus.designsystem.theme.tokens.core.Sizes

/** A `bgDark` surface inside a `strokeBgDark` outline, both in [shape]. */
internal fun Modifier.darkSurface(shape: Shape): Modifier =
    background(OtusColors.bgDark, shape).strokeBorder(Sizes.borderWidth, OtusColors.strokeBgDark, shape)
