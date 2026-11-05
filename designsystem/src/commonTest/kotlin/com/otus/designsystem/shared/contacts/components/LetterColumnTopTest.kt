package com.otus.designsystem.shared.contacts.components

import kotlin.test.Test
import kotlin.test.assertEquals

class LetterColumnTopTest {
    @Test
    fun theAnchorRowCentresOnItsHeading() {
        val top = top(anchorCenterY = 500f, anchorIndex = 4)

        assertEquals(320f, top)
        assertEquals(500f, top + 4.5f * ROW)
    }

    @Test
    fun theAnchorKeepsItsPlaceNearTheEdges() {
        assertEquals(850f, top(anchorCenterY = 950f, anchorIndex = 2))
        assertEquals(-190f, top(anchorCenterY = 30f, anchorIndex = 5))
    }

    @Test
    fun theAnchorKeepsItsPlaceInAColumnTallerThanTheBox() {
        assertEquals(-440f, top(anchorCenterY = 100f, anchorIndex = 13, count = 30))
        assertEquals(80f, top(anchorCenterY = 100f, anchorIndex = 0, count = 30))
        assertEquals(-280f, top(anchorCenterY = 900f, anchorIndex = 29, count = 30))
    }

    @Test
    fun withoutAnAnchorTheColumnIsCentred() {
        assertEquals(300f, top(anchorCenterY = null, anchorIndex = 4))
        assertEquals(300f, top(anchorCenterY = 500f, anchorIndex = -1))
        assertEquals(290f, top(anchorCenterY = null, anchorIndex = 0, band = 80f..900f))
    }

    @Test
    fun withoutAnAnchorATallerColumnStartsAtTheTop() {
        assertEquals(80f, top(anchorCenterY = null, anchorIndex = 0, count = 30, band = 80f..900f))
    }

    @Test
    fun aColumnThatFitsInPlaceDoesNotScroll() {
        assertEquals(320f..320f, scrollReach(top = 320f, height = 400f, band = 0f..BOX))
    }

    @Test
    fun aColumnPastAnEdgeScrollsOnlyUntilItsEndShows() {
        assertEquals(-190f..60f, scrollReach(top = -190f, height = 400f, band = 60f..920f))
        assertEquals(520f..850f, scrollReach(top = 850f, height = 400f, band = 60f..920f))
    }

    @Test
    fun aTallColumnScrollsUntilEitherEndShows() {
        assertEquals(-280f..80f, scrollReach(top = -280f, height = 1200f, band = 80f..920f))
    }

    private fun top(
        anchorCenterY: Float?,
        anchorIndex: Int,
        count: Int = COUNT,
        band: ClosedFloatingPointRange<Float> = 0f..BOX,
    ): Float = letterColumnTop(anchorCenterY, anchorIndex, rowHeight = ROW, count = count, band = band)

    private companion object {
        const val ROW = 40f
        const val BOX = 1000f
        const val COUNT = 10
    }
}
