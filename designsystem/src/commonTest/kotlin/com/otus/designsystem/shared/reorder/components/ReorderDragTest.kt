package com.otus.designsystem.shared.reorder.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReorderDragTest {
    private val drops = mutableListOf<Pair<Int, Float>>()
    private val keys = listOf("first", "second", "third")

    @Test
    fun aDropReportsTheRowAndItsOffsetOnce() {
        val drag = measuredDrag()
        drag.start("first")
        drag.moveBy(500f)
        assertEquals(emptyList(), drops)
        drag.drop()
        drag.drop()
        // Rows of 40, 80, and 40 px, 10 px apart: the first row stops at the bottom, 140 px down.
        assertEquals(listOf(0 to 140f), drops)
        assertNull(drag.key)
    }

    @Test
    fun anOverhangLetsTheRowReachPastEitherEnd() {
        val drag = measuredDrag(overhang = 0.5f)
        drag.start("first")
        drag.moveBy(-500f)
        drag.drop()
        drag.start("first")
        drag.moveBy(500f)
        drag.drop()
        assertEquals(listOf(0 to -20f, 0 to 160f), drops)
    }

    @Test
    fun aCancelledDragReportsNothing() {
        val drag = measuredDrag()
        drag.start("second")
        drag.moveBy(50f)
        drag.cancel()
        drag.drop()
        assertEquals(emptyList(), drops)
    }

    @Test
    fun aListUpdateCancelsTheDragAndRemovesObsoleteMeasurements() {
        val drag = measuredDrag()
        drag.start("first")
        drag.moveBy(100f)
        drag.layout(keys.drop(1), spacing = 10f, ::drop)
        drag.drop()
        assertNull(drag.key)
        assertEquals(setOf<Any>("second", "third"), drag.heights.keys)
        assertEquals(emptyList(), drops)
    }

    @Test
    fun aCallbackUpdateKeepsTheDragAndUsesTheLatestCallbackOnDrop() {
        val drag = measuredDrag()
        drag.start("first")
        drag.moveBy(30f)
        val latest = mutableListOf<Pair<Int, Float>>()
        drag.layout(keys, spacing = 10f) { from, offset -> latest.add(from to offset) }
        drag.drop()
        assertEquals(emptyList(), drops)
        assertEquals(listOf(0 to 30f), latest)
    }

    @Test
    fun anUnmeasuredColumnDoesNotStartADrag() {
        val drag = ReorderDrag()
        drag.layout(keys, spacing = 10f, ::drop)
        drag.start("first")
        drag.moveBy(100f)
        drag.drop()
        assertNull(drag.key)
        assertEquals(emptyList(), drops)
    }

    private fun measuredDrag(overhang: Float = 0f): ReorderDrag =
        ReorderDrag(overhang).apply {
            layout(keys, spacing = 10f, ::drop)
            heights.putAll(keys.zip(listOf(40, 80, 40)))
        }

    private fun drop(
        from: Int,
        offset: Float,
    ) {
        drops.add(from to offset)
    }
}
