package com.otus.designsystem.theme

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
        page = Color(0xFF202021),
        raised = Color(0xFF151515),
        light = Color(0xFF3F4041),
        stroke = Color(0xFF27292C),
        text1 = Color(0xFFEBECED),
        text2 = Color(0xFFC2C2C2),
        text3 = Color(0xFF8A8A8A),
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

    /** Warm grey dark, after Notion's dark mode. */
    Graphite(
        page = Color(0xFF252525),
        raised = Color(0xFF191919),
        light = Color(0xFF373737),
        stroke = Color(0xFF2F2F2F),
        text1 = Color(0xFFD4D4D4),
        text2 = Color(0xFFA3A3A3),
        text3 = Color(0xFF8F8F8F),
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

    /** A light theme has dark text on bright pages. */
    public val isLight: Boolean
        get() = page.luminance() > HALF_LUMINANCE
}

private const val HALF_LUMINANCE = 0.5f
