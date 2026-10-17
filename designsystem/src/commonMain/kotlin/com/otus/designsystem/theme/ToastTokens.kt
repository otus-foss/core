package com.otus.designsystem.theme

import androidx.compose.ui.unit.dp

/** Sizes and timings of toasts. The sizes were measured from the supplied mockups, not from Figma. */
internal object ToastTokens {
    val height = 52.dp

    /** Space between the toast's edge and its text, and inside the dismiss area behind it. */
    val textInset = 20.dp

    /** Space around the action button inside the toast; the button's touch area still fills the toast's height. */
    val actionInset = 6.dp
    val actionPadding = 12.dp
    val actionGap = 8.dp
    val ringSize = 18.dp
    val ringStroke = 2.dp

    /** How far each toast behind the front one peeks out above it while the group is stacked. */
    val peek = 6.dp

    /** Space between toasts once the group fans out. */
    val gap = 8.dp

    /** How much smaller each toast behind the front one draws while stacked. */
    const val PEEK_SCALE_STEP = 0.05f

    /** Toasts behind the front one that still peek out while stacked; older ones hide until it clears. */
    const val PEEK_LIMIT = 2

    /** Share of its width a toast must be dragged for release to dismiss it. */
    const val DISMISS_FRACTION = 1f / 3f

    /** How long a toast stays on screen; one with an action stays longer to leave time to tap it. */
    const val ACTION_MILLIS = 6_000L
    const val PLAIN_MILLIS = 4_000L
}
