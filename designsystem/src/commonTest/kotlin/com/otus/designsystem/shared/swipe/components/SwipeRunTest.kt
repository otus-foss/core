package com.otus.designsystem.shared.swipe.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.MonotonicFrameClock
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.shared.controls.elements.UiAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SwipeRunTest {
    private val anchors =
        DraggableAnchors {
            SwipeAnchor.Settled at 0f
            SwipeAnchor.StartToEnd at ROW_WIDTH
        }

    @Test
    fun aDismissedRowWaitsAtItsAnchorUntilItReturnsToRest() =
        runTest {
            val state = AnchoredDraggableState(SwipeAnchor.Settled, anchors)
            var runs = 0
            val archive = SwipeAction(UiAction("Archive", Res.drawable.symbol_archive) { runs++ }, dismisses = true)
            launch(FrameClock()) { runSwipe(state, SwipeAnchor.StartToEnd, archive, Animatable(0f), motionScale = 1f) }
            advanceUntilIdle()
            assertEquals(1, runs)
            assertEquals(ROW_WIDTH, state.requireOffset())
            launch(FrameClock()) { state.returnToRest(motionScale = 1f) }
            advanceUntilIdle()
            assertEquals(1, runs)
            assertEquals(0f, state.requireOffset())
            assertEquals(SwipeAnchor.Settled, state.settledValue)
        }

    /** Frames every 16 ms of virtual time, so animations advance with the test scheduler. */
    private class FrameClock : MonotonicFrameClock {
        private var frameNanos = 0L

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            delay(FRAME_MILLIS)
            frameNanos += FRAME_MILLIS * NANOS_PER_MILLI
            return onFrame(frameNanos)
        }
    }

    private companion object {
        const val ROW_WIDTH = 320f
        const val FRAME_MILLIS = 16L
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
