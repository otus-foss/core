package com.otus.designsystem.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import kotlin.coroutines.cancellation.CancellationException

/**
 * Drags vertically after a long press. It watches the initial pass, so it sees each event before
 * the row's own click and the list's scroll. Once the press is long enough it consumes the rest of
 * the gesture, so releasing the row neither clicks it nor scrolls the list. A press that moves
 * beyond the touch slop or lifts early stays an ordinary tap or scroll.
 */
internal suspend fun PointerInputScope.detectLongPressDrag(
    onStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onEnd: () -> Unit,
    onCancel: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val endedEarly = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { awaitReleaseOrSlop(down) }
        if (endedEarly != null) return@awaitEachGesture
        onStart()
        try {
            followDrag(down.id, onDrag)
            onEnd()
        } catch (cancelled: CancellationException) {
            onCancel()
            throw cancelled
        }
    }
}

/** Returns once the pointer lifts or moves beyond the touch slop. */
private suspend fun AwaitPointerEventScope.awaitReleaseOrSlop(down: PointerInputChange) {
    while (true) {
        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: return
        if (!change.pressed || (change.position - down.position).getDistance() > viewConfiguration.touchSlop) return
    }
}

/** Reports the pointer's vertical moves, including one that comes with the lift, and consumes them until it lifts. */
private suspend fun AwaitPointerEventScope.followDrag(
    id: PointerId,
    onDrag: (Float) -> Unit,
) {
    while (true) {
        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == id } ?: return
        onDrag(change.positionChangeIgnoreConsumed().y)
        change.consume()
        if (!change.pressed) return
    }
}
