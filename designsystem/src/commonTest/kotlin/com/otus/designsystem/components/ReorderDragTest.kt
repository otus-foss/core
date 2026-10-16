package com.otus.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReorderDragTest {
    private val moves = mutableListOf<Pair<Int, Int>>()
    private val keys = listOf("first", "second", "third")

    @Test
    fun aDropReportsOneMoveAfterTheCallerHasNotYetChangedTheOrder() {
        val drag = measuredDrag()
        drag.start("first")
        drag.moveBy(500f)
        assertEquals(emptyList(), moves)
        drag.drop()
        drag.drop()
        assertEquals(listOf(0 to 2), moves)
        assertNull(drag.key)
    }

    @Test
    fun cancelAndAnUnchangedDropLeaveTheOrderAlone() {
        val drag = measuredDrag()
        drag.start("second")
        drag.moveBy(50f)
        drag.cancel()
        drag.drop()
        drag.start("second")
        drag.drop()
        assertEquals(emptyList(), moves)
    }

    @Test
    fun aListUpdateCancelsTheDragAndRemovesObsoleteMeasurements() {
        val drag = measuredDrag()
        drag.start("first")
        drag.moveBy(100f)
        drag.layout(keys.drop(1), 10f, ::move)
        drag.drop()
        assertNull(drag.key)
        assertEquals(setOf<Any>("second", "third"), drag.heights.keys)
        assertEquals(emptyList(), moves)
    }

    @Test
    fun aCallbackUpdateKeepsTheDragAndUsesTheLatestCallbackOnDrop() {
        val drag = measuredDrag()
        drag.start("first")
        drag.moveBy(500f)
        val latestMoves = mutableListOf<Pair<Int, Int>>()
        drag.layout(keys, 10f, onMove = { from, to -> latestMoves.add(from to to) })
        drag.drop()
        assertEquals(emptyList(), moves)
        assertEquals(listOf(0 to 2), latestMoves)
    }

    @Test
    fun anUnmeasuredColumnDoesNotStartADrag() {
        val drag = ReorderDrag()
        drag.layout(keys, 10f, ::move)
        drag.start("first")
        drag.moveBy(100f)
        drag.drop()
        assertNull(drag.key)
        assertEquals(emptyList(), moves)
    }

    private fun measuredDrag(): ReorderDrag =
        ReorderDrag().apply {
            layout(keys, 10f, ::move)
            heights.putAll(keys.zip(listOf(40, 80, 40)))
        }

    private fun move(
        from: Int,
        to: Int,
    ) {
        moves.add(from to to)
    }
}
