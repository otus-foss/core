package com.otus.designsystem.mail.sidebar.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.otus.designsystem.mail.sidebar.elements.SidebarTileSize.Full
import com.otus.designsystem.mail.sidebar.elements.SidebarTileSize.Small
import com.otus.designsystem.mail.sidebar.elements.SidebarTileSize.Wide
import kotlin.test.Test
import kotlin.test.assertEquals

class TileSlotsTest {
    @Test
    fun tilesFillRowsInOrderAndOneThatDoesNotFitStartsTheNext() {
        assertEquals(
            listOf(
                TileSlot(0, 0, Full),
                TileSlot(1, 0, Small),
                TileSlot(1, 1, Small),
                TileSlot(2, 0, Wide),
                TileSlot(2, 2, Small),
                TileSlot(3, 0, Wide),
            ),
            tileSlots(listOf(Full, Small, Small, Wide, Small, Wide)),
        )
    }

    @Test
    fun boundsSpanTheirColumnsAndTheGapsBetweenThem() {
        // 100 px columns with 10 px gaps.
        val bounds = tileBounds(tileSlots(listOf(Small, Wide, Full)), width = 320, gap = 10) { 50 }
        assertEquals(
            listOf(
                IntRect(IntOffset(0, 0), IntSize(100, 50)),
                IntRect(IntOffset(110, 0), IntSize(210, 50)),
                IntRect(IntOffset(0, 60), IntSize(320, 50)),
            ),
            bounds,
        )
    }
}
