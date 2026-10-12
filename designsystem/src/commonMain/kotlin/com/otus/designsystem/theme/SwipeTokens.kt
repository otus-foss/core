package com.otus.designsystem.theme

import androidx.compose.ui.unit.dp

internal object SwipeTokens {
    /** Drag distance past which releasing runs the revealed action. The same drag is needed every time. */
    val threshold = 96.dp

    /** Space between the row edge and the revealed icon, and between the icon and its label. */
    val inset = 16.dp
    val gap = 8.dp

    /** How far the icon slides in from the edge while it grows to full size at the threshold. */
    val iconEntry = 16.dp

    /** Rounding of the revealed area's free end, next to the moving row. */
    val cornerRadius = 12.dp

    /** Concave rounding where the revealed area meets the row's edge, reaching past the row's top and bottom. */
    val filletRadius = 12.dp

    /** Icon scale at the start of a swipe; it reaches full size at [threshold]. */
    const val ICON_START_SCALE = 0.5f

    /** Opacity of the text-colored flash over the accent when a release commits. */
    const val FLASH_ALPHA = 0.24f
}
