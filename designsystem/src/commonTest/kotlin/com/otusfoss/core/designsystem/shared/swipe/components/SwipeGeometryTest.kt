package com.otusfoss.core.designsystem.shared.swipe.components

import androidx.compose.foundation.gestures.DraggableAnchors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SwipeGeometryTest {
    @Test
    fun aReleaseCommitsOnlyPastTheThresholdTowardASideWithAnAction() {
        val anchors =
            DraggableAnchors {
                SwipeAnchor.EndToStart at -320f
                SwipeAnchor.Settled at 0f
                SwipeAnchor.StartToEnd at 320f
            }
        assertEquals(SwipeAnchor.Settled, swipeTarget(95f, 96f, anchors))
        assertEquals(SwipeAnchor.StartToEnd, swipeTarget(96f, 96f, anchors))
        assertEquals(SwipeAnchor.Settled, swipeTarget(-95f, 96f, anchors))
        assertEquals(SwipeAnchor.EndToStart, swipeTarget(-96f, 96f, anchors))

        val startOnly =
            DraggableAnchors {
                SwipeAnchor.Settled at 0f
                SwipeAnchor.StartToEnd at 320f
            }
        assertEquals(SwipeAnchor.Settled, swipeTarget(-200f, 96f, startOnly))
    }

    @Test
    fun theRowStopsOnceItsActionIsRevealedInEitherDirection() {
        assertEquals(192f, boundedRevealOffset(240f, 192f))
        assertEquals(-192f, boundedRevealOffset(-240f, 192f))
        assertEquals(50f, boundedRevealOffset(50f, 192f))
    }

    @Test
    fun theIconAndCornersGrowWithTheDragUntilTheirDistance() {
        assertEquals(0f, swipeProgress(0f, 96f))
        assertEquals(0.5f, swipeProgress(-48f, 96f))
        assertEquals(1f, swipeProgress(144f, 96f))
    }

    @Test
    fun theRevealedEdgeFollowsTheLayoutDirection() {
        assertTrue(revealFromLeft(fromStart = true, isLtr = true))
        assertFalse(revealFromLeft(fromStart = false, isLtr = true))
        assertFalse(revealFromLeft(fromStart = true, isLtr = false))
        assertTrue(revealFromLeft(fromStart = false, isLtr = false))
    }
}
