package com.otusfoss.core.designsystem.theme.tokens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The quick view a control panel grows into when dragged down: modules of one height, so any two
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

    /** The width each face takes in a scrolling row, its circle and a short name. */
    public val faceSlot: Dp = 68.dp

    /** The sides of the "+N" pill at a scrolling row's end. */
    public val morePadding: Dp = 8.dp
    public val dotSize: Dp = 12.dp
    public val dotRing: Dp = 3.dp
    public val labelSize: TextUnit = 12.sp
    public val labelLineHeight: TextUnit = 15.sp

    /** A file card in a row that scrolls, its picture, and the gap to its two-line name. */
    public val fileCardWidth: Dp = 160.dp
    public val fileIconSize: Dp = 48.dp
    public val fileGap: Dp = 10.dp
    public val fileEnd: Dp = 12.dp
    public val fileNameSize: TextUnit = 13.sp
    public val fileNameLineHeight: TextUnit = 17.sp

    /** Up to this many file cards share the row's width; more scroll. */
    public const val FILE_FILL_LIMIT: Int = 2

    /** A drag released faster than this, per second, settles on the next stop the way it went. */
    public val flingSpeed: Dp = 800.dp

    /** How far the panel grows before its pills have faded out. */
    public val pillsFadeDistance: Dp = 60.dp

    /** The share of a fading edge on the Jump to row, which shows it scrolls on. */
    public const val JUMP_FADE: Float = 0.85f

    /** A count tile with nothing in it. */
    public const val EMPTY_ALPHA: Float = 0.45f
}
