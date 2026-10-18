package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The corner radii every part rounds to. Feature tokens such as [SheetTokens] and [MenuTokens]
 * name their corner by one of these steps, so the scale changes here. A part inside another,
 * such as a menu row, takes the outer radius minus its inset instead of a step. Pills, avatars,
 * and circles round to half their height and take no step.
 */
internal object CornerTokens {
    /** The ends of a date range in the calendar. */
    val tiny: Dp = 6.dp

    /** Compact rule chips and the bars of a theme sketch. */
    val small: Dp = 10.dp

    /** Chips, value buttons, and tokens, such as recipients, quotes, and dates. */
    val chip: Dp = 14.dp

    /** Toolbar buttons, search fields, and theme cards. */
    val button: Dp = 20.dp

    /** The control panel at the top of a page, and the filter panel that grows out of it. */
    val panel: Dp = 22.dp

    /** Rows and cards inside pages, menus, and sheets, and sidebar entries. */
    val row: Dp = 24.dp

    /** The formatting row above the keyboard, and nudge cards. */
    val large: Dp = 28.dp

    /** The action bar, menus, dropdowns, and the top corners of sheets and dialogs. */
    val surface: Dp = 30.dp
}
