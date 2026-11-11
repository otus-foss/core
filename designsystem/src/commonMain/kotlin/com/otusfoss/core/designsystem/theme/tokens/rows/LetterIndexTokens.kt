package com.otusfoss.core.designsystem.theme.tokens.rows

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens

/** Sizes of the jump-to-letter index; see LetterIndex. */
internal object LetterIndexTokens {
    /** How much the page behind the open index blurs. */
    val blur: Dp = 16.dp

    /** The vertical blur on a letter halfway through its slide, as a motion blur. */
    val motionBlur: Dp = 6.dp

    /** The share of [motionBlur] a letter also blurs sideways, so the blur reads as vertical motion. */
    const val SIDEWAYS_BLUR: Float = 0.1f

    /** A letter's row; with more letters than fit, the column scrolls instead of shrinking them. */
    val rowHeight: Dp = 48.dp

    /** The current letter's highlight corner, and the letter and count's inset from the row's ends. */
    val highlightRadius: Dp = CornerTokens.row
    val rowPadding: Dp = 12.dp

    /** The gap above and below the highlight behind the current letter, inside its row. */
    val highlightInset: Dp = 4.dp

    /** The letter's column, wider than any letter, so every count starts at the same edge. */
    val letterColumn: Dp = 28.dp

    /** The longest first name the current letter shows with a second name beside it. */
    const val PREVIEW_CHARACTERS: Int = 10

    /**
     * How far from a heading's edge on the letters' side a press opens the index; further along it
     * takes a long press.
     */
    val handleWidth: Dp = 64.dp

    /**
     * How many rows above and below the pressed heading a thumb reaches without shifting the hand.
     * The edge zones sit at the ends of that reach, and every letter can scroll into it.
     */
    const val THUMB_REACH_ROWS: Float = 4f

    /** How much the switch-sides button grows while a dragging finger is on it. */
    const val SWITCH_HOVER_SCALE: Float = 1.2f

    /** How many rows deep the edge zones are where a held finger scrolls an overflowing column. */
    const val EDGE_ROWS: Float = 1.5f

    /** How many rows a second the column scrolls with the finger deepest in an edge zone. */
    const val EDGE_ROWS_PER_SECOND: Float = 14f

    /** How long after the list stops scrolling a press on a heading still counts as part of the scroll. */
    const val SCROLL_SETTLE_MILLIS: Long = 300
}
