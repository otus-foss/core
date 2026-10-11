package com.otus.designsystem.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import com.otus.designsystem.theme.ChoiceSheetTokens
import com.otus.designsystem.theme.MotionTokens
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

/** How far the sheet is pushed below its resting place: 1 is fully hidden, 0 fully shown. */
internal class SheetPosition {
    var hidden by mutableFloatStateOf(1f)
        private set
    var height by mutableIntStateOf(0)
    var closing by mutableStateOf(false)
        private set
    private var animation: Job? = null

    suspend fun show(easing: Easing) {
        if (!closing) animateTo(0f, ChoiceSheetTokens.ENTER_MILLIS, easing)
    }

    suspend fun hide(then: () -> Unit) {
        if (closing) return
        closing = true
        animateTo(1f, ChoiceSheetTokens.EXIT_MILLIS, MotionTokens.exit)
        then()
    }

    /** Moves the sheet [delta] pixels down, or up when negative, and returns how far it moved. */
    fun dragBy(delta: Float): Float {
        if (height <= 0 || closing) return 0f
        animation?.cancel()
        val before = hidden
        hidden = (hidden + delta / height).coerceIn(0f, 1f)
        return (hidden - before) * height
    }

    /** After a drag: a downward fling or a sheet pulled far enough down closes; anything else returns. */
    suspend fun release(
        velocity: Float,
        flingVelocity: Float,
        close: () -> Unit,
    ) {
        if (closing) return
        if (velocity > flingVelocity ||
            hidden > ChoiceSheetTokens.DISMISS_FRACTION
        ) {
            close()
        } else {
            show(MotionTokens.standard)
        }
    }

    private suspend fun animateTo(
        target: Float,
        millis: Int,
        easing: Easing,
    ) {
        val job = currentCoroutineContext().job
        animation?.takeIf { it != job }?.cancel()
        animation = job
        try {
            animate(hidden, target, animationSpec = tween(millis, easing = easing)) { value, _ -> hidden = value }
        } finally {
            if (animation == job) animation = null
        }
    }
}

/**
 * Lets a downward swipe anywhere on the sheet close it. Scrolling content hands the swipe to the
 * sheet once it is at its top, and a swipe back up returns the sheet before the content scrolls
 * again. A swipe that starts anywhere else, such as on the header, a text field, or empty space,
 * drags the sheet directly.
 */
@Composable
internal fun Modifier.swipeToDismiss(
    position: SheetPosition,
    close: () -> Unit,
): Modifier {
    val flingVelocity = with(LocalDensity.current) { ChoiceSheetTokens.dismissVelocity.toPx() }
    val onClose by rememberUpdatedState(close)
    val connection = remember(position, flingVelocity) { SheetScrollConnection(position, flingVelocity) { onClose() } }
    return nestedScroll(connection).draggable(
        rememberDraggableState { delta -> position.dragBy(delta) },
        Orientation.Vertical,
        enabled = !position.closing,
        onDragStopped = { velocity -> position.release(velocity, flingVelocity, onClose) },
    )
}

private class SheetScrollConnection(
    private val position: SheetPosition,
    private val flingVelocity: Float,
    private val close: () -> Unit,
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset =
        if (source == NestedScrollSource.UserInput && available.y < 0f && position.hidden > 0f) {
            Offset(0f, position.dragBy(available.y))
        } else {
            Offset.Zero
        }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset =
        if (source == NestedScrollSource.UserInput && available.y > 0f) {
            Offset(0f, position.dragBy(available.y))
        } else {
            Offset.Zero
        }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (position.hidden <= 0f) return Velocity.Zero
        position.release(available.y, flingVelocity, close)
        return available
    }
}
