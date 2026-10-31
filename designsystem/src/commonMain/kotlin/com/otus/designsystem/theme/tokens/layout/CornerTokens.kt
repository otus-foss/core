package com.otus.designsystem.theme.tokens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The corner radii every part rounds to. Feature tokens such as
 * [SheetTokens][com.otus.designsystem.theme.tokens.overlays.SheetTokens] and
 * [MenuTokens][com.otus.designsystem.theme.tokens.overlays.MenuTokens] name their corner by one of
 * these steps, so the scale changes here. A part inside another,
 * such as a menu row, takes the outer radius minus its inset instead of a step. Pills, avatars,
 * and circles, such as the action bar and the formatting row, round to half their height and take
 * no step.
 */
internal object CornerTokens {
    /** How far each corner eases into its edges, as in Figma's corner smoothing; 0.6 matches iOS. */
    const val SMOOTHING: Float = 0.6f

    /** The ends of a date range in the calendar. */
    val tiny: Dp = 4.dp

    /** Compact rule chips and the bars of a theme sketch. */
    val small: Dp = 8.dp

    /** Chips, value buttons, and tokens, such as recipients, quotes, and dates. */
    val chip: Dp = 10.dp

    /** Search fields and theme cards. */
    val button: Dp = 15.dp

    /** The control panel at the top of a page, and the filter panel that grows out of it. */
    val panel: Dp = 16.dp

    /** Rows and cards inside pages, menus, and sheets, and sidebar entries. */
    val row: Dp = 18.dp

    /** Nudge cards. */
    val large: Dp = 20.dp

    /** Menus, dropdowns, and the top corners of sheets and dialogs. */
    val surface: Dp = 22.dp
}
