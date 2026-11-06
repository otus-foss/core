package com.otus.designsystem.shared.pages.elements

import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PageTokens

/**
 * Where a page sits among [count] pages, newest first: the [current] page is a wide `text1` pill,
 * the others small `bgLight` dots. With more than [PageTokens.DOT_WINDOW] pages it shows a window
 * that keeps the current dot in view, and the outer dots shrink where more pages lie beyond them.
 * It has no semantics; the text beside it says the position.
 */
@Composable
internal fun PageDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(PageTokens.dotGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visibleDots(count, current).forEach { dot -> key(dot.index) { Dot(dot) } }
    }
}

@Composable
private fun Dot(dot: VisibleDot) {
    // Width and height animate as one value, so a dot that grows or shrinks stays round.
    val size by animateValueAsState(
        DpSize(if (dot.current) PageTokens.currentDotWidth else dot.size, dot.size),
        DpSizeConverter,
        motionTween(MotionSpeed.Fast),
    )
    Box(
        Modifier
            .size(size)
            .background(if (dot.current) OtusColors.text1 else OtusColors.bgLight, CircleShape),
    )
}

private val DpSizeConverter =
    TwoWayConverter<DpSize, AnimationVector2D>(
        { AnimationVector2D(it.width.value, it.height.value) },
        { DpSize(it.v1.dp, it.v2.dp) },
    )

/** One dot [PageDots] draws: the page [index], whether it is [current], and its height. */
internal data class VisibleDot(
    val index: Int,
    val current: Boolean,
    val size: Dp,
)

/** The dots in view for [current] of [count]: a window of at most [PageTokens.DOT_WINDOW]. */
internal fun visibleDots(
    count: Int,
    current: Int,
): List<VisibleDot> {
    val window = minOf(count, PageTokens.DOT_WINDOW)
    val first = (current - window / 2).coerceIn(0, count - window)
    val last = first + window - 1
    return (first..last).map { index ->
        val fromEdge =
            minOf(
                if (first > 0) index - first else Int.MAX_VALUE,
                if (last < count - 1) last - index else Int.MAX_VALUE,
            )
        val size =
            when (fromEdge) {
                0 -> PageTokens.edgeDot
                1 -> PageTokens.nearEdgeDot
                else -> PageTokens.dotSize
            }
        VisibleDot(index, index == current, if (index == current) PageTokens.dotSize else size)
    }
}
