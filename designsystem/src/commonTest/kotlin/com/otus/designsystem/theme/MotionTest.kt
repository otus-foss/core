package com.otus.designsystem.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MotionTest {
    @Test
    fun eachSpeedDividesTheBaseDuration() {
        assertEquals(
            mapOf(
                MotionSpeed.Quick to 120,
                MotionSpeed.Fast to 160,
                MotionSpeed.Normal to 240,
                MotionSpeed.Relaxed to 300,
            ),
            MotionSpeed.entries.associateWith { Motion.millis(it) },
        )
    }

    @Test
    fun aLargerScaleShortensEveryDuration() {
        assertEquals(60, Motion.millis(MotionSpeed.Quick, scale = 2f))
        assertEquals(480, Motion.millis(MotionSpeed.Normal, scale = 0.5f))
        assertEquals(240, Motion.millis(MotionSpeed.Relaxed, scale = 1.25f))
    }

    @Test
    fun durationsRoundToTheNearestMillisecond() {
        assertEquals(107, Motion.millis(MotionSpeed.Fast, scale = 1.5f))
    }

    @Test
    fun tweenUsesTheScaledDurationAndCurve() {
        val spec = Motion.tween<Float>(MotionSpeed.Fast, MotionCurve.Exit, scale = 2f)
        assertEquals(80, spec.durationMillis)
        assertEquals(MotionCurve.Exit.easing, spec.easing)
    }

    @Test
    fun aScaleThatIsNotPositiveIsRejected() {
        assertFailsWith<IllegalArgumentException> { Motion.millis(MotionSpeed.Normal, scale = 0f) }
    }
}
