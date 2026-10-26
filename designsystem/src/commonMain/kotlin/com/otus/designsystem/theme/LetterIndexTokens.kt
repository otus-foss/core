package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Sizes of the jump-to-letter index; see LetterIndex. */
internal object LetterIndexTokens {
    /** How much the page behind the open index blurs. */
    val blur: Dp = 16.dp

    /** The vertical blur on a letter halfway through its slide, as a motion blur. */
    val motionBlur: Dp = 6.dp

    /** The share of [motionBlur] a letter also blurs sideways, so the blur reads as vertical motion. */
    const val SIDEWAYS_BLUR: Float = 0.1f

    /** The tallest a letter's row grows; with many letters the rows shrink to fit the screen. */
    val rowHeight: Dp = 48.dp

    /** The letter's column, wider than any letter, so every count starts at the same edge. */
    val letterColumn: Dp = 28.dp
}
