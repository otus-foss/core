package com.otusfoss.core.designsystem.shared.panels.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The panel's stops: where a drag settles, a fling's next stop, opening straight to a stop, and moved stops. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PanelExpansionTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var expansion: PanelExpansion
    private var stops by mutableStateOf(listOf(QUICK, FILTERS))
    private var content by mutableStateOf<Any?>(null)

    private fun show() {
        compose.setContent {
            // One pixel per dp, so heights read as the stops they are.
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                expansion = rememberPanelExpansion(stops, content)
            }
        }
        compose.waitForIdle()
    }

    private fun dragTo(height: Float) {
        compose.runOnIdle { expansion.drag(height - expansion.height) }
    }

    private fun release(velocity: Float = 0f) {
        compose.runOnIdle { expansion.release(velocity) }
        compose.waitForIdle()
    }

    private fun assertAt(
        stop: Int,
        height: Dp,
    ) {
        compose.runOnIdle {
            assertEquals(stop, expansion.stop)
            assertEquals(height.value, expansion.height)
            assertFalse(expansion.moving)
            assertFalse(expansion.dragging)
        }
    }

    @Test
    fun aReleaseSettlesOnTheNearerStop() {
        show()
        dragTo(60f)
        release()
        assertAt(1, QUICK)
        assertEquals(1f, expansion.position)
        dragTo(130f)
        release()
        assertAt(1, QUICK)
        dragTo(30f)
        release()
        assertAt(0, 0.dp)
        assertFalse(expansion.expanded)
    }

    @Test
    fun aFlingGoesOnToTheNextStopInItsDirection() {
        show()
        dragTo(120f)
        release(FAST)
        assertAt(2, FILTERS)
        dragTo(280f)
        release(-FAST)
        assertAt(1, QUICK)
    }

    @Test
    fun openingStraightToAStopSkipsTheOnesBetweenUntilItCloses() {
        show()
        compose.runOnIdle { expansion.expandTo(2) }
        compose.waitForIdle()
        assertAt(2, FILTERS)
        assertTrue(expansion.skipping)
        // Opened straight, a release only goes back to that stop or to the start, never the quick view.
        dragTo(120f)
        release()
        assertAt(0, 0.dp)
        assertFalse(expansion.skipping)
        dragTo(120f)
        release()
        assertAt(1, QUICK)
    }

    @Test
    fun aStopThatMovesUnderARestingPanelIsFollowedAtOnce() {
        show()
        compose.runOnIdle { expansion.expandTo(1) }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        stops = listOf(150.dp, FILTERS)
        Snapshot.sendApplyNotifications()
        compose.mainClock.advanceTimeByFrame()
        assertEquals(150f, expansion.height)
        // New content moves on the motion curve instead.
        content = "search"
        stops = listOf(200.dp, FILTERS)
        Snapshot.sendApplyNotifications()
        compose.mainClock.advanceTimeByFrame()
        assertTrue(expansion.moving)
        assertTrue(expansion.height < 200f)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        assertAt(1, 200.dp)
    }

    @Test
    fun stopsWithinAPixelCountAsOneHeight() {
        stops = listOf(FILTERS, FILTERS + 0.5.dp)
        show()
        compose.runOnIdle { expansion.expandTo(2) }
        compose.waitForIdle()
        assertEquals(2f, expansion.position)
        assertEquals(2, expansion.stop)
    }

    private companion object {
        val QUICK = 100.dp
        val FILTERS = 300.dp

        /** Well past the fling speed, in pixels a second. */
        const val FAST = 10_000f
    }
}
