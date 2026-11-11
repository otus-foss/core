package com.otusfoss.core.designsystem.theme.colors

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * The colors of the current theme. [OtusTheme][com.otusfoss.core.designsystem.theme.OtusTheme] sets the [palette].
 * Reading a color in composition or drawing recomposes or redraws it when the theme changes.
 */
public object OtusColors {
    public var palette: OtusPalette by mutableStateOf(OtusPalette.Otus)
        internal set

    public val accentLight: Color get() = palette.accentLight
    public val accentRegular: Color get() = palette.accentRegular
    public val accentDark: Color get() = palette.accentDark
    public val bgLight: Color get() = palette.bgLight
    public val bgRegular: Color get() = palette.bgRegular
    public val bgDark: Color get() = palette.bgDark
    public val text1: Color get() = palette.text1
    public val text2: Color get() = palette.text2
    public val text3: Color get() = palette.text3
    public val strokeBgDark: Color get() = palette.strokeBgDark

    /** See [OtusPalette.selectedFill]. */
    public val selectedFill: Color get() = palette.selectedFill

    /** Dims the page behind sheets and the sidebar; apply the caller's alpha. */
    public val scrim: Color get() = palette.scrim
}
