package com.otusfoss.core.designsystem.shared.dates.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.theme.tokens.selection.CalendarTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** One gesture owner for the viewport keeps the anchor when its month scrolls out of composition. */
@Composable
internal fun Modifier.calendarRangeGesture(
    state: LazyListState,
    targets: CalendarDragTargets,
    start: String?,
    end: String?,
    onRange: (String?, String?) -> Unit,
): Modifier {
    val currentRange by rememberUpdatedState(start to end)
    val updateRange by rememberUpdatedState(onRange)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return pointerInput(state, targets, rtl) {
        val edge = CalendarTokens.dragScrollEdge.toPx()
        val speed = CalendarTokens.dragScrollSpeed.toPx()
        coroutineScope {
            val scope = this
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val anchor = targets.dateAt(down.position, rtl) ?: return@awaitEachGesture
                val previous = currentRange
                val held = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                var position = held.position

                fun update() {
                    val date = targets.dateAt(position, rtl) ?: return
                    updateRange(minOf(anchor, date).toString(), maxOf(anchor, date).toString())
                }
                held.consume()
                update()
                val direction = { edgeScroll(position, size.height.toFloat(), edge) }
                val scrolling = scope.scrollWhileHeld(state, speed, direction, ::update)
                var completed = false
                try {
                    completed =
                        followPointer(held.id) {
                            position = it
                            update()
                        }
                } finally {
                    scrolling.cancel()
                    if (!completed) updateRange(previous.first, previous.second)
                }
            }
        }
    }
}

/**
 * Consumes [id]'s moves, clamped to this element's height, until it lifts. Returns whether it
 * lifted normally rather than being cancelled or taken by another handler.
 */
private suspend fun AwaitPointerEventScope.followPointer(
    id: PointerId,
    onMove: (Offset) -> Unit,
): Boolean {
    while (true) {
        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == id }
        if (change == null || !change.pressed) {
            val completed = change?.isConsumed == false
            change?.consume()
            return completed
        }
        change.consume()
        onMove(Offset(change.position.x, change.position.y.coerceIn(0f, size.height - 1f)))
    }
}

private fun edgeScroll(
    position: Offset,
    height: Float,
    edge: Float,
): Float =
    when {
        position.y < edge -> -((edge - position.y) / edge).coerceIn(0f, 1f)
        position.y > height - edge -> ((position.y - height + edge) / edge).coerceIn(0f, 1f)
        else -> 0f
    }

private fun CoroutineScope.scrollWhileHeld(
    state: LazyListState,
    speed: Float,
    direction: () -> Float,
    update: () -> Unit,
): Job =
    launch {
        var lastFrame = withFrameNanos { it }
        while (isActive) {
            val frame = withFrameNanos { it }
            val elapsed = (frame - lastFrame) / CalendarTokens.NANOS_PER_SECOND
            lastFrame = frame
            val scrollDirection = direction()
            if (scrollDirection != 0f) state.scrollBy(scrollDirection * speed * elapsed)
            update()
        }
    }
