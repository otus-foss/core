package com.otusfoss.core.designsystem.theme.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * The page, surface, and text colors of a theme, before an accent is added. [page] becomes
 * `bgRegular`, [raised] `bgDark`, [light] `bgLight`, and [stroke] `strokeBgDark`.
 */
public enum class ThemeBase(
    public val page: Color,
    public val raised: Color,
    public val light: Color,
    public val stroke: Color,
    public val text1: Color,
    public val text2: Color,
    public val text3: Color,
) {
    /** Soft dark, the default. */
    Otus(
        page = Color(0xFF161617),
        raised = Color(0xFF111112),
        light = Color(0xFF252628),
        stroke = Color(0xFF1C1D1F),
        text1 = Color(0xFFEBECED),
        text2 = Color(0xFFC2C2C2),
        text3 = Color(0xFF8A8A8A),
    ),

    /** Darker than [Otus], with surfaces closer together so the page reads as one calm plane. */
    Ink(
        page = Color(0xFF111112),
        raised = Color(0xFF0E0E0F),
        light = Color(0xFF1E1F20),
        stroke = Color(0xFF18191A),
        text1 = Color(0xFFE3E4E5),
        text2 = Color(0xFFB9BABB),
        text3 = Color(0xFF86878A),
    ),

    /** Bright pages with white surfaces. */
    Light(
        page = Color(0xFFF2F2F3),
        raised = Color(0xFFFFFFFF),
        light = Color(0xFFD4D5D8),
        stroke = Color(0xFFE3E4E6),
        text1 = Color(0xFF1B1C1E),
        text2 = Color(0xFF45474B),
        text3 = Color(0xFF696B70),
    ),

    /** True black for OLED screens, after the X mobile app. */
    Black(
        page = Color(0xFF000000),
        raised = Color(0xFF000000),
        light = Color(0xFF2F3336),
        stroke = Color(0xFF2F3336),
        text1 = Color(0xFFE7E9EA),
        text2 = Color(0xFFA1A6AB),
        text3 = Color(0xFF7B8086),
    ),
    ;

    /** This theme's own [color], before the user changes it; null for the accent's derived shades. */
    public fun color(color: ThemeColor): Color? =
        when (color) {
            ThemeColor.Page -> page
            ThemeColor.Raised -> raised
            ThemeColor.Controls -> light
            ThemeColor.Borders -> stroke
            ThemeColor.Text -> text1
            ThemeColor.SecondaryText -> text2
            ThemeColor.MutedText -> text3
            ThemeColor.AccentFill, ThemeColor.AccentOutline, ThemeColor.AccentText -> null
        }

    /** A light theme has dark text on bright pages. */
    public val isLight: Boolean
        get() = page.isLightPage
}

/** Whether a page of this color is bright enough to need dark text. */
internal val Color.isLightPage: Boolean
    get() = luminance() > HALF_LUMINANCE

private const val HALF_LUMINANCE = 0.5f
