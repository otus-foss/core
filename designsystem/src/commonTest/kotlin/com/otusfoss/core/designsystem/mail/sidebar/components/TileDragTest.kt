package com.otusfoss.core.designsystem.mail.sidebar.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.otusfoss.core.designsystem.mail.sidebar.elements.SidebarTileSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TileDragTest {
    private val keys = listOf("a", "b", "c")

    // Three Small tiles in one row of 100 px columns with 10 px gaps.
    private val place = { order: List<Any> ->
        tileBounds(tileSlots(order.map { SidebarTileSize.Small }), width = 320, gap = 10) { 50 }
    }

    @Test
    fun aTileDraggedPastAnotherTakesItsPlaceAndDropsThere() {
        val drag = TileDrag().apply { sync(keys) }
        drag.start("a", IntOffset.Zero)
        assertTrue(drag.moveBy(Offset(230f, 0f), place))
        assertEquals(listOf("b", "c", "a"), drag.shown(keys))
        assertEquals(0 to 2, drag.drop())
        // The dropped order shows until the caller's keys catch up.
        assertEquals(listOf("b", "c", "a"), drag.shown(keys))
        drag.sync(listOf("b", "c", "a"))
        assertEquals(keys, drag.shown(keys))
    }

    @Test
    fun aTileDroppedWhereItStartedReportsNoMove() {
        val drag = TileDrag().apply { sync(keys) }
        drag.start("b", IntOffset(110, 0))
        drag.moveBy(Offset(20f, 10f), place)
        assertNull(drag.drop())
    }
}
