package com.otus.designsystem.shared.overlays.components

import androidx.compose.runtime.MonotonicFrameClock
import com.otus.designsystem.theme.motion.MotionCurve
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SheetPositionTest {
    @Test
    fun repeatedCloseRequestsFinishOnlyTheFirstAction() =
        runTest {
            val position = SheetPosition()
            var finished = 0
            val clock = frameClock()
            launch(clock) { position.show(MotionCurve.Enter) }.join()
            val close = launch(clock) { position.hide { finished++ } }
            runCurrent()
            assertTrue(position.closing)
            assertEquals(0, finished)
            launch(clock) { position.hide { finished += 10 } }.join()
            close.join()
            assertEquals(1, finished)
            assertEquals(1f, position.hidden)
        }

    @Test
    fun closingInterruptsEntryAndCannotBeReopenedByAnotherShow() =
        runTest {
            val position = SheetPosition()
            val clock = frameClock()
            val entry = launch(clock) { position.show(MotionCurve.Enter) }
            advanceTimeBy(64)
            var finished = false
            launch(clock) { position.hide { finished = true } }.join()
            assertTrue(entry.isCancelled)
            assertTrue(finished)
            launch(clock) { position.show(MotionCurve.Enter) }.join()
            assertEquals(1f, position.hidden)
        }

    @Test
    fun draggingInterruptsEntryAndClampsTheSheetToItsHeight() =
        runTest {
            val position = SheetPosition().apply { height = 400 }
            val entry = launch(frameClock()) { position.show(MotionCurve.Enter) }
            advanceTimeBy(64)
            position.dragBy(-400f)
            entry.join()
            assertTrue(entry.isCancelled)
            assertEquals(0f, position.hidden)
            assertEquals(400f, position.dragBy(500f))
            assertEquals(-400f, position.dragBy(-500f))
        }

    @Test
    fun aShortDragReturnsButADownwardFlingCloses() =
        runTest {
            val position = SheetPosition().apply { height = 400 }
            val clock = frameClock()
            launch(clock) { position.show(MotionCurve.Enter) }.join()
            var closes = 0
            position.dragBy(40f)
            launch(clock) { position.release(0f, 600f) { closes++ } }.join()
            assertEquals(0f, position.hidden)
            assertEquals(0, closes)
            launch(clock) { position.release(601f, 600f) { closes++ } }.join()
            assertEquals(1, closes)
        }

    private fun TestScope.frameClock(): MonotonicFrameClock =
        object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                delay(16)
                return onFrame(testScheduler.currentTime * 1_000_000)
            }
        }
}
