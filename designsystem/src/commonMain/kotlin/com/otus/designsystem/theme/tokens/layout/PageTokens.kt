package com.otus.designsystem.theme.tokens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.core.Spacing

/** Layout for pages built from a context panel header and a scrolling body. */
public object PageTokens {
    public val pageInset: Dp = 8.dp
    public val contentInset: Dp = Spacing.gutter

    /** Free-standing text lines up with the text inside full-width rows. */
    public val textInset: Dp = 12.dp
    public val rowGap: Dp = 8.dp
    public val captionSize: TextUnit = 14.sp
    public val dialogMaxWidth: Dp = 420.dp

    /** Each nesting level of a tree row moves its icon this far toward the trailing edge. */
    public val nestIndent: Dp = Spacing.indent

    /** Position dots: a plain dot, the wide current one, and the gap between them. */
    public val dotSize: Dp = 6.dp
    public val currentDotWidth: Dp = 18.dp
    public val dotGap: Dp = 3.dp

    /** The widest the dot of the page a drag heads to swells before the drag is far enough. */
    public val swollenDotWidth: Dp = 14.dp

    /** The line between the two pages while a drag heads from one to the other. */
    public val seamWidth: Dp = 2.dp

    /** How opaque the dimmer of the two pages is while a drag heads from one to the other. */
    public const val TURN_DIM: Float = 0.35f

    /** The dots at the edge of a cut window: next to the edge, and at it. */
    public val nearEdgeDot: Dp = 4.dp
    public val edgeDot: Dp = 3.dp

    /** The most dots shown at once; more slide a window that keeps the current one in view. */
    public const val DOT_WINDOW: Int = 7
}
