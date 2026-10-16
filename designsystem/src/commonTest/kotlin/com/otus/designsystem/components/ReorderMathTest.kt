package com.otus.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ReorderMathTest {
    // Rows of 40, 80, and 40 px, 10 px apart: tops at 0, 50, and 140.
    private val heights = listOf(40, 80, 40)
    private val spacing = 10f

    @Test
    fun aRowPassesANeighborOnceItsLeadingEdgeCrossesTheNeighborsMiddle() {
        // Middles at 20, 90, and 160.
        assertEquals(listOf(0f, 50f, 140f), rowTops(heights, spacing))
        assertEquals(0, reorderTarget(heights, from = 0, offset = 49f, spacing = spacing))
        assertEquals(1, reorderTarget(heights, from = 0, offset = 51f, spacing = spacing))
        assertEquals(2, reorderTarget(heights, from = 0, offset = 121f, spacing = spacing))
        assertEquals(1, reorderTarget(heights, from = 2, offset = -51f, spacing = spacing))
        assertEquals(0, reorderTarget(heights, from = 2, offset = -121f, spacing = spacing))
    }

    @Test
    fun aTallRowReachesEitherEndAtTheEdgeOfTheColumn() {
        val down = clampDrag(heights, from = 1, offset = 500f, spacing = spacing)
        assertEquals(2, reorderTarget(heights, from = 1, offset = down, spacing = spacing))
        val up = clampDrag(heights, from = 1, offset = -500f, spacing = spacing)
        assertEquals(0, reorderTarget(heights, from = 1, offset = up, spacing = spacing))
    }

    @Test
    fun theRowsBetweenStepAsideByTheDraggedRowAndItsGap() {
        assertEquals(
            listOf(0f, -50f, -50f),
            heights.indices.map { reorderShift(it, from = 0, target = 2, 40, spacing) },
        )
        assertEquals(listOf(50f, 50f, 0f), heights.indices.map { reorderShift(it, from = 2, target = 0, 40, spacing) })
        assertEquals(listOf(0f, 0f, 0f), heights.indices.map { reorderShift(it, from = 1, target = 1, 80, spacing) })
    }

    @Test
    fun aDraggedRowStaysWithinTheColumn() {
        assertEquals(-50f, clampDrag(heights, from = 1, offset = -500f, spacing = spacing))
        assertEquals(50f, clampDrag(heights, from = 1, offset = 500f, spacing = spacing))
        assertEquals(12f, clampDrag(heights, from = 1, offset = 12f, spacing = spacing))
    }

    @Test
    fun theMiddleHalfOfARowTakesADropOntoIt() {
        // Row 1 spans 50 to 130, so its middle half is 70 to 110. Row 0's bottom edge starts at 40.
        assertEquals(null, nestTarget(heights, from = 0, offset = 29f, spacing = spacing))
        assertEquals(1, nestTarget(heights, from = 0, offset = 30f, spacing = spacing))
        assertEquals(1, nestTarget(heights, from = 0, offset = 70f, spacing = spacing))
        assertEquals(null, nestTarget(heights, from = 0, offset = 71f, spacing = spacing))
        // Moving up, row 2's top edge starts at 140 and leads.
        assertEquals(1, nestTarget(heights, from = 2, offset = -40f, spacing = spacing))
        assertEquals(null, nestTarget(heights, from = 2, offset = -20f, spacing = spacing))
        assertEquals(0, nestTarget(heights, from = 2, offset = -125f, spacing = spacing))
    }

    @Test
    fun withNestingARowPassesAnotherOnlyBeyondItsMiddleHalf() {
        assertEquals(ReorderDrop(0), reorderDrop(heights, from = 0, offset = 20f, spacing = spacing, nesting = true))
        assertEquals(
            ReorderDrop(0, onto = 1),
            reorderDrop(heights, from = 0, offset = 50f, spacing = spacing, nesting = true),
        )
        assertEquals(ReorderDrop(1), reorderDrop(heights, from = 0, offset = 71f, spacing = spacing, nesting = true))
        assertEquals(
            ReorderDrop(2, onto = null),
            reorderDrop(heights, from = 0, offset = 140f, spacing = spacing, nesting = true),
        )
        assertEquals(
            ReorderDrop(1, onto = 0),
            reorderDrop(heights, from = 1, offset = -40f, spacing = spacing, nesting = true),
        )
        assertEquals(ReorderDrop(0), reorderDrop(heights, from = 1, offset = -50f, spacing = spacing, nesting = true))
    }

    @Test
    fun withoutNestingTheDropOnlyReorders() {
        assertEquals(ReorderDrop(1), reorderDrop(heights, from = 0, offset = 51f, spacing = spacing, nesting = false))
    }
}
