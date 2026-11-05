package com.otus.designsystem.shared.contacts.components

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import com.otus.designsystem.theme.tokens.rows.LetterIndexTokens
import kotlinx.coroutines.flow.drop
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Keeps the [state]'s headings from opening the index while [scroll], the list they sit in, is
 * scrolling or has only just stopped, so a touch that stops a fling, or lands mid-scroll, scrolls
 * the list instead.
 */
@Composable
public fun LetterIndexScrollGuard(
    state: LetterIndexState,
    scroll: ScrollableState,
) {
    val stopped = remember { StopMark() }
    LaunchedEffect(scroll) {
        snapshotFlow { scroll.isScrollInProgress }
            .drop(1)
            .collect { moving -> if (!moving) stopped.mark = TimeSource.Monotonic.markNow() }
    }
    DisposableEffect(state, scroll) {
        state.scrolling = {
            scroll.isScrollInProgress ||
                stopped.mark?.let { it.elapsedNow() < LetterIndexTokens.SCROLL_SETTLE_MILLIS.milliseconds } == true
        }
        onDispose { state.scrolling = { false } }
    }
}

/** When the list last stopped scrolling; read only from touch handling, so it is not snapshot state. */
private class StopMark {
    var mark: TimeMark? = null
}

/**
 * While a finger drags across the open index, scrolls a column too tall for the screen when the
 * finger holds near the top or bottom of the safe area, faster the closer it is to the edge, and
 * marks the letter that comes under it. Rows are [rowHeight] pixels tall.
 */
internal suspend fun LetterIndexState.scrollAtEdges(rowHeight: Float) {
    if (!dragging) return
    var last = withFrameNanos { it }
    while (dragging) {
        val frame = withFrameNanos { it }
        scrollAtEdgesFor((frame - last) / NANOS_PER_SECOND, rowHeight)
        last = frame
    }
}

/** One frame of [scrollAtEdges], [seconds] after the last one. */
private fun LetterIndexState.scrollAtEdgesFor(
    seconds: Float,
    rowHeight: Float,
) {
    val held = finger
    val safe = safeArea
    if (held == null || safe == null) return
    val pull = edgePull(held.y, safe.top, safe.bottom, rowHeight * LetterIndexTokens.EDGE_ROWS)
    if (pull != 0f) {
        shiftBy(pull * rowHeight * LetterIndexTokens.EDGE_ROWS_PER_SECOND * seconds)
        hover(held)
    }
}

/**
 * How hard a finger at [y] pulls the column, from -1 to 1: positive within [edge] pixels of [top],
 * which brings earlier letters down into view, negative near [bottom], and 0 in between.
 */
internal fun edgePull(
    y: Float,
    top: Float,
    bottom: Float,
    edge: Float,
): Float =
    when {
        y < top + edge -> ((top + edge - y) / edge).coerceAtMost(1f)
        y > bottom - edge -> -((y - bottom + edge) / edge).coerceAtMost(1f)
        else -> 0f
    }

/** Draws only the part of the index inside its safe area, so a scrolled column stays clear of the bars. */
internal fun Modifier.clipToSafeArea(state: LetterIndexState): Modifier =
    drawWithContent {
        val safe = state.safeArea
        if (safe == null) {
            drawContent()
        } else {
            clipRect(top = safe.top - state.windowTop, bottom = safe.bottom - state.windowTop) {
                this@drawWithContent.drawContent()
            }
        }
    }

private const val NANOS_PER_SECOND = 1_000_000_000f
