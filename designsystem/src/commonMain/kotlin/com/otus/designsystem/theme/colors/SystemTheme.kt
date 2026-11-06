package com.otus.designsystem.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The device's own theme colors, such as Android's Material You colors from the wallpaper, in the
 * device's current light or dark mode. Otus derives the accent's shades from [accent] as for any
 * other accent.
 */
@Immutable
public data class SystemTheme(
    val page: Color,
    val raised: Color,
    val controls: Color,
    val borders: Color,
    val text: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val accent: Color,
) {
    /** The device's [color]; null for the accent's derived shades. */
    public fun color(color: ThemeColor): Color? =
        when (color) {
            ThemeColor.Page -> page
            ThemeColor.Raised -> raised
            ThemeColor.Controls -> controls
            ThemeColor.Borders -> borders
            ThemeColor.Text -> text
            ThemeColor.SecondaryText -> secondaryText
            ThemeColor.MutedText -> mutedText
            ThemeColor.AccentFill, ThemeColor.AccentOutline, ThemeColor.AccentText -> null
        }
}
