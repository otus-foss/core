package com.otus.designsystem.theme.motion

import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MotionTest {
    @Test
    fun aLargerScaleShortensEveryDuration() {
        MotionSpeed.entries.forEach { speed ->
            assertEquals(Motion.millis(speed) / 2, Motion.millis(speed, scale = 2f), speed.name)
        }
        assertEquals(Motion.millis(MotionSpeed.Normal) * 2, Motion.millis(MotionSpeed.Normal, scale = 0.5f))
    }

    @Test
    fun durationsRoundToTheNearestMillisecond() {
        val base = Motion.millis(MotionSpeed.Fast)
        assertEquals((base / SCALE_WITH_FRACTION).roundToInt(), Motion.millis(MotionSpeed.Fast, SCALE_WITH_FRACTION))
    }

    @Test
    fun tweenUsesTheScaledDurationAndCurve() {
        val spec = Motion.tween<Float>(MotionSpeed.Fast, MotionCurve.Exit, scale = 2f)
        assertEquals(Motion.millis(MotionSpeed.Fast, scale = 2f), spec.durationMillis)
        assertEquals(MotionCurve.Exit.easing, spec.easing)
    }

    @Test
    fun reversePlaysStandardBackward() {
        (0..20).map { it / 20f }.forEach { t ->
            val backward = 1f - MotionCurve.Standard.easing.transform(1f - t)
            assertEquals(backward, MotionCurve.Reverse.easing.transform(t), absoluteTolerance = 0.002f)
        }
    }

    @Test
    fun aScaleThatIsNotPositiveIsRejected() {
        assertFailsWith<IllegalArgumentException> { Motion.millis(MotionSpeed.Normal, scale = 0f) }
    }

    private companion object {
        const val SCALE_WITH_FRACTION = 1.5f
    }
}
