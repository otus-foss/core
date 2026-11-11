package com.otusfoss.core.designsystem.shared.pages.elements

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens

/**
 * A drag that heads from the page on screen to the page at [toward]: [fraction] runs from 0 to 1,
 * and at 1 letting go turns the page.
 */
@Immutable
public class TurnProgress(
    public val toward: Int,
    public val fraction: Float,
) {
    /** Whether letting go now turns the page. */
    public val ready: Boolean get() = fraction >= 1f
}

/** The page a position shows for [current]: the one a [ready][TurnProgress.ready] drag heads to, else [current]. */
internal fun TurnProgress?.shown(current: Int): Int = this?.takeIf { it.ready }?.toward ?: current

/**
 * Dims the page at [index] while a drag heads between two pages, so the bright page is the one
 * letting go lands on. [turn] is read as the page draws, so a drag does not recompose it.
 */
public fun Modifier.turnDim(
    index: Int,
    turn: () -> TurnProgress?,
): Modifier = graphicsLayer { alpha = turn()?.let { turnAlpha(index == it.toward, it) } ?: 1f }

/**
 * Draws the seam between the page at [index] and the page a drag heads to from it: a line down
 * the edge they share, `bgLight` during the drag and `text1` once letting go turns the page. Put
 * it before [turnDim], so the line keeps its color when the page dims.
 */
public fun Modifier.turnSeam(
    index: Int,
    turn: () -> TurnProgress?,
): Modifier =
    drawWithContent {
        drawContent()
        val heading = turn()?.takeIf { it.toward != index } ?: return@drawWithContent
        val width = PageTokens.seamWidth.toPx()
        // The next page lies at the end edge, the previous one at the start edge.
        val atRight = (heading.toward > index) == (layoutDirection == LayoutDirection.Ltr)
        drawRect(
            if (heading.ready) OtusColors.text1 else OtusColors.bgLight,
            Offset(if (atRight) size.width - width else 0f, 0f),
            Size(width, size.height),
        )
    }

/**
 * How opaque a page is during [turn]: the page the drag heads to when [target], else the page it
 * started on. The page letting go lands on is opaque and the other sits at [PageTokens.TURN_DIM],
 * so the two swap at the turn point, with the tick.
 */
internal fun turnAlpha(
    target: Boolean,
    turn: TurnProgress,
): Float = if (target == turn.ready) 1f else PageTokens.TURN_DIM
