package com.otusfoss.core.designsystem.theme.shapes

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes

/** A `bgDark` surface inside a `strokeBgDark` outline, both in [shape], as fields and cards use. */
public fun Modifier.darkSurface(shape: Shape): Modifier =
    background(OtusColors.bgDark, shape).strokeBorder(Sizes.borderWidth, OtusColors.strokeBgDark, shape)
