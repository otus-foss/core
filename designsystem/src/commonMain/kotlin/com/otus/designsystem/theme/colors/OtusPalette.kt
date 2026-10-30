package com.otus.designsystem.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Every color [OtusColors] hands out. [ThemeChoice.palette] builds one from a theme and an accent;
 * [Otus] is the soft dark default.
 */
@Immutable
public data class OtusPalette(
    public val accentLight: Color,
    public val accentRegular: Color,
    public val accentDark: Color,
    public val bgLight: Color,
    public val bgRegular: Color,
    public val bgDark: Color,
    public val text1: Color,
    public val text2: Color,
    public val text3: Color,
    public val strokeBgDark: Color,
    public val isLight: Boolean = false,
) {
    /** Dims the page behind a sheet or the sidebar; callers apply their own alpha. */
    public val scrim: Color
        get() = if (isLight) text1 else bgDark

    /**
     * Fills a selected or current control that is not the screen's primary action, such as a
     * filter chip, a menu's current choice, or the sidebar's current folder, with `text1` on it.
     * The accent stays for primary actions, badges, multi-select rows, switches, and links.
     */
    public val selectedFill: Color
        get() = bgLight

    public companion object {
        public val Otus: OtusPalette =
            OtusPalette(
                accentLight = Color(0xFF7099C4),
                accentRegular = Color(0xFF5A7EA4),
                accentDark = Color(0xFF435F7C),
                bgLight = Color(0xFF252628),
                bgRegular = Color(0xFF161617),
                bgDark = Color(0xFF111112),
                text1 = Color(0xFFEBECED),
                text2 = Color(0xFFC2C2C2),
                text3 = Color(0xFF8A8A8A),
                strokeBgDark = Color(0xFF1C1D1F),
            )
    }
}
