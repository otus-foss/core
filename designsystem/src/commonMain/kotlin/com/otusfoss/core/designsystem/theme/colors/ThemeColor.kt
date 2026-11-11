package com.otusfoss.core.designsystem.theme.colors

import androidx.compose.ui.graphics.Color

/** A color of a theme the user can change; each maps to one [OtusPalette] color. */
public enum class ThemeColor {
    Page,
    Raised,
    Controls,
    Borders,
    Text,
    SecondaryText,
    MutedText,
    AccentFill,
    AccentOutline,
    AccentText,
    ;

    /** True for the shades Otus derives from the accent, which a new accent replaces. */
    public val isAccent: Boolean
        get() = this == AccentFill || this == AccentOutline || this == AccentText
}

/** The palette's color for [color]. */
public operator fun OtusPalette.get(color: ThemeColor): Color =
    when (color) {
        ThemeColor.Page -> bgRegular
        ThemeColor.Raised -> bgDark
        ThemeColor.Controls -> bgLight
        ThemeColor.Borders -> strokeBgDark
        ThemeColor.Text -> text1
        ThemeColor.SecondaryText -> text2
        ThemeColor.MutedText -> text3
        ThemeColor.AccentFill -> accentDark
        ThemeColor.AccentOutline -> accentRegular
        ThemeColor.AccentText -> accentLight
    }
