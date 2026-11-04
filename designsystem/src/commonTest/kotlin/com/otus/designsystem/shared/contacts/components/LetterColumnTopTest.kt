package com.otus.designsystem.shared.contacts.components

import kotlin.test.Test
import kotlin.test.assertEquals

class LetterColumnTopTest {
    @Test
    fun theAnchorRowCentresOnItsHeadingWhenTheColumnFits() {
        val top = top(anchorCenterY = 500f, anchorIndex = 4)

        assertEquals(320f, top)
        assertEquals(500f, top + 4.5f * ROW)
    }

    @Test
    fun anAnchorNearTheBottomMovesTheColumnUpOnlyUntilItFits() {
        val top = top(anchorCenterY = 950f, anchorIndex = 2)

        assertEquals(BOX, top + COUNT * ROW)
    }

    @Test
    fun anAnchorNearTheTopMovesTheColumnDownOnlyUntilItFits() {
        assertEquals(0f, top(anchorCenterY = 30f, anchorIndex = 5))
    }

    @Test
    fun withoutAnAnchorTheColumnIsCentred() {
        assertEquals(300f, top(anchorCenterY = null, anchorIndex = 4))
        assertEquals(300f, top(anchorCenterY = 500f, anchorIndex = -1))
    }

    @Test
    fun aColumnTallerThanTheBoxIsCentredOnIt() {
        assertEquals(-100f, top(anchorCenterY = 100f, anchorIndex = 0, count = 30))
    }

    private fun top(
        anchorCenterY: Float?,
        anchorIndex: Int,
        count: Int = COUNT,
    ): Float = letterColumnTop(anchorCenterY, anchorIndex, rowHeight = ROW, count = count, containerHeight = BOX)

    private companion object {
        const val ROW = 40f
        const val BOX = 1000f
        const val COUNT = 10
    }
}
