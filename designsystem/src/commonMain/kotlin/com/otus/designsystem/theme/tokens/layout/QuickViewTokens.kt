package com.otus.designsystem.theme.tokens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The quick view a control panel opens to when pulled down: modules of one height, so any two
 * the user picks stack the same, and the one-line Jump to row under them.
 */
public object QuickViewTokens {
    /** Every module's height, whichever the user picks. */
    public val moduleHeight: Dp = 112.dp

    /** The Jump to row under the modules. */
    public val jumpHeight: Dp = 64.dp

    public val moduleGap: Dp = 8.dp
    public val tileHeight: Dp = 80.dp
    public val tileGap: Dp = 8.dp
    public val tileLabelGap: Dp = 6.dp
    public val badgeInset: Dp = 8.dp
    public val faceSize: Dp = 52.dp
    public val dotSize: Dp = 12.dp
    public val dotRing: Dp = 3.dp
    public val labelSize: TextUnit = 12.sp
    public val labelLineHeight: TextUnit = 15.sp

    /** How far down a pull must go before the panel opens one step further. */
    public val pullDistance: Dp = 56.dp

    /** The share of a fading edge on the Jump to row, which shows it scrolls on. */
    public const val JUMP_FADE: Float = 0.85f

    /** A count tile with nothing in it. */
    public const val EMPTY_ALPHA: Float = 0.45f
}
