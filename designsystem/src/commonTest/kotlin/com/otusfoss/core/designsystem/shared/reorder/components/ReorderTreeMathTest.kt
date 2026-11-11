package com.otusfoss.core.designsystem.shared.reorder.components

import com.otusfoss.core.designsystem.shared.reorder.components.TreeDropTarget.Before
import com.otusfoss.core.designsystem.shared.reorder.components.TreeDropTarget.Into
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReorderTreeMathTest {
    // Four rows of 40 px: tops at 0, 40, 80, and 120, the end at 160.
    private val heights = listOf(40, 40, 40, 40)

    @Test
    fun theMiddleHalfOfARowDropsIntoIt() {
        assertEquals(Into(1), treeDropTarget(heights, from = 0, offset = 35f))
        assertEquals(Into(1), treeDropTarget(heights, from = 0, offset = 45f))
        assertEquals(Into(1), treeDropTarget(heights, from = 2, offset = -45f))
        assertEquals(Into(0), treeDropTarget(heights, from = 3, offset = -120f))
    }

    @Test
    fun theOuterQuartersOfARowDropIntoTheGapsBesideIt() {
        assertEquals(Before(2), treeDropTarget(heights, from = 0, offset = 55f))
        assertEquals(Before(3), treeDropTarget(heights, from = 0, offset = 95f))
        assertEquals(Before(1), treeDropTarget(heights, from = 3, offset = -95f))
    }

    @Test
    fun aRowReachesBothEndsOfTheTreeWithItsOverhang() {
        assertEquals(Before(4), treeDropTarget(heights, from = 0, offset = 140f))
        assertEquals(Before(0), treeDropTarget(heights, from = 3, offset = -140f))
    }

    @Test
    fun theRowItselfAndTheGapsBesideItDropNowhere() {
        assertNull(treeDropTarget(heights, from = 0, offset = 0f))
        assertNull(treeDropTarget(heights, from = 2, offset = -5f))
        assertNull(treeDropTarget(heights, from = 0, offset = 25f))
        assertNull(treeDropTarget(heights, from = 2, offset = -15f))
        assertNull(treeDropTarget(heights, from = 3, offset = 20f))
    }

    @Test
    fun nestedRowsMoveWithTheirParentSoTheirSpotsDropNowhere() {
        // Row 0 holds rows 1 and 2; dragged down by one row, its center is over its own row 1.
        assertEquals(2, nestedSpan(listOf(0, 1, 2, 0), from = 0))
        assertNull(treeDropTarget(heights, from = 0, offset = 40f, span = 2))
        assertNull(treeDropTarget(heights, from = 0, offset = 95f, span = 2))
        assertEquals(Into(3), treeDropTarget(heights, from = 0, offset = 120f, span = 2))
        assertEquals(Before(0), treeDropTarget(heights, from = 1, offset = -55f, span = 1))
    }

    @Test
    fun theLineSitsInTheGapOrUnderTheRowItGoesInto() {
        assertEquals(80f, dropLineY(heights, Before(2)))
        assertEquals(160f, dropLineY(heights, Before(4)))
        assertEquals(80f, dropLineY(heights, Into(1)))
    }

    @Test
    fun theLineTakesTheDepthOfTheRowBelowOrOneDeeperThanItsParent() {
        val depths = listOf(0, 1, 0, 0)
        assertEquals(1, dropLineDepth(depths, Before(1)))
        assertEquals(0, dropLineDepth(depths, Before(4)))
        assertEquals(2, dropLineDepth(depths, Into(1)))
    }
}
