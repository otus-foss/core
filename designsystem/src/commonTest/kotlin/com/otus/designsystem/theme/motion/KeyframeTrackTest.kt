package com.otus.designsystem.theme.motion

import com.otus.designsystem.theme.brand.MailLaunchTimeline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeyframeTrackTest {
    private val track = KeyframeTrack(MotionCurve.Standard, 10f at 0.2f, -2f at 0.6f, 0f at 0.8f)

    @Test
    fun holdsTheFirstValueBeforeTheFirstKeyframe() {
        assertEquals(10f, track.valueAt(0f))
        assertEquals(10f, track.valueAt(0.2f))
    }

    @Test
    fun reachesEachKeyframeOnTime() {
        assertEquals(-2f, track.valueAt(0.6f))
        assertEquals(0f, track.valueAt(0.8f))
    }

    @Test
    fun easesBetweenKeyframes() {
        val halfway = track.valueAt(0.4f)
        assertTrue(halfway < 4f, "Standard eases out, so halfway is past the midpoint, was $halfway")
        assertTrue(halfway > -2f)
    }

    @Test
    fun holdsTheLastValueAfterTheLastKeyframe() {
        assertEquals(0f, track.valueAt(1f))
    }

    @Test
    fun theLoopPicksUpWhereTheIntroSettles() {
        val intro = MailLaunchTimeline.Intro
        val loop = MailLaunchTimeline.Loop
        val settled = MailLaunchTimeline.LOOP_SETTLED
        assertEquals(intro.envelope.valueAt(1f), loop.envelope.valueAt(settled))
        assertEquals(intro.owl.valueAt(1f), loop.owl.valueAt(settled))
        assertEquals(intro.tilt.valueAt(1f), loop.tilt.valueAt(settled))
        assertEquals(intro.blink.valueAt(1f), loop.blink.valueAt(settled))
        assertEquals(loop.owl.valueAt(0f), loop.owl.valueAt(1f), "The loop ends where it starts")
    }

    @Test
    fun designedLengthsFollowTheScale() {
        assertEquals(3000, Motion.scaled(Motion.LAUNCH_MILLIS.toFloat(), scale = 1f))
        assertEquals(2100, Motion.scaled(Motion.LAUNCH_LOOP_MILLIS.toFloat(), scale = 2f))
    }
}
