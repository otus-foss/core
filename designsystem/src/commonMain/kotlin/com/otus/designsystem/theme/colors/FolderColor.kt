package com.otus.designsystem.theme.colors

import androidx.compose.ui.graphics.Color

/**
 * A color the user gives one of their folders. It tints only the folder's icon. Apps store its
 * [name] and read it back with [named]. [Default] keeps the icon's normal color.
 *
 * Each color has one shade for dark themes and one for light themes, picked by
 * [OtusPalette.isLight], so custom themes follow the kind of theme they were made from. Every shade
 * reaches 3:1 contrast on `bgDark`, `bgRegular`, `selectedFill`, and `accentDark` for every built-in theme and
 * accent preset. Dark shades are light so they stay readable on the brightest accent fill.
 */
public enum class FolderColor(
    public val label: String,
    private val onDark: Color?,
    private val onLight: Color?,
) {
    Default("Default", null, null),
    Red("Red", Color(0xFFFFB5B8), Color(0xFFE12B2B)),
    Orange("Orange", Color(0xFFFFB973), Color(0xFFC45205)),
    Yellow("Yellow", Color(0xFFF2C200), Color(0xFF996B00)),
    Green("Green", Color(0xFF68DF77), Color(0xFF1D872E)),
    Teal("Teal", Color(0xFF3CDDCD), Color(0xFF07837F)),

    // Bluer and brighter than the Otus blue accent (hue 210), so a blue folder never reads as selected.
    Blue("Blue", Color(0xFFAFC7FD), Color(0xFF3D6FE3)),
    Purple("Purple", Color(0xFFD8BAFF), Color(0xFF9258D5)),
    Pink("Pink", Color(0xFFFFB0E5), Color(0xFFD42594)),
    ;

    /** The icon color in [palette], or null for [Default], which keeps the icon's own color. */
    public fun tintIn(palette: OtusPalette): Color? = if (palette.isLight) onLight else onDark

    /** The icon color in the current theme; reading it in composition follows theme changes. */
    public val tint: Color?
        get() = tintIn(OtusColors.palette)

    public companion object {
        /** The color stored as [name], or [Default] for null and names it does not know. */
        public fun named(name: String?): FolderColor = entries.firstOrNull { it.name == name } ?: Default
    }
}
