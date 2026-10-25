package com.otus.designsystem.theme

import androidx.compose.ui.unit.dp

/** Sizes and timings of toasts. The sizes were measured from the supplied mockups, not from Figma. */
internal object ToastTokens {
    val height = 52.dp

    /** Space between the toast's edge and its text, and inside the dismiss area behind it. */
    val textInset = 20.dp

    /** Space above and below the text, so a toast of several lines keeps air around them. */
    val textPadding = 14.dp

    /** Longer messages are cut after this many lines. */
    const val MAX_LINES = 3

    /** Space around the action button inside the toast; the button's touch area still fills the toast's height. */
    val actionInset = 6.dp
    val actionPadding = 12.dp
    val actionGap = 8.dp
    val ringSize = 18.dp
    val ringStroke = 2.dp

    /** How far each toast behind the front one peeks out above it while the group is stacked. */
    val peek = 10.dp

    /** Space between toasts once the group fans out. */
    val gap = 8.dp

    /** How much smaller each toast behind the front one draws while stacked. */
    const val PEEK_SCALE_STEP = 0.05f

    /** Toasts behind the front one that still peek out while stacked; older ones hide until it clears. */
    const val PEEK_LIMIT = 2

    /** How small a toast draws as it arrives from, or leaves to, below the group. */
    const val AWAY_SCALE = 0.9f

    /** How far a dragged toast's dismiss pill stretches out past the toast's edge, at most. */
    val dismissStretch = 16.dp

    /** How much of the drag the dismiss pill stretches out by, until [dismissStretch]. */
    const val STRETCH_RATIO = 0.25f

    /** Share of its width a toast must be dragged for release to dismiss it. */
    const val DISMISS_FRACTION = 1f / 3f

    /** How long a toast stays on screen; one with an action stays longer to leave time to tap it. */
    const val ACTION_MILLIS = 6_000L
    const val PLAIN_MILLIS = 4_000L
}
