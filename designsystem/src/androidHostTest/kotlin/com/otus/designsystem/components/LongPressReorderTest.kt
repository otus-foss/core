package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LongPressReorderTest {
    @get:Rule
    val compose = createComposeRule()

    private val clicks = mutableListOf<String>()
    private val moves = mutableListOf<Pair<Int, Int>>()

    @Test
    fun aTapStillClicksTheRow() {
        showRows()
        compose.onNodeWithText("Clients").performClick()
        assertEquals(listOf("Clients"), clicks)
        assertEquals(emptyList(), moves)
    }

    @Test
    fun aLongPressWithoutMovingNeitherClicksNorMoves() {
        showRows()
        compose.onNodeWithText("Clients").performTouchInput { longClick() }
        compose.waitForIdle()
        assertEquals(emptyList(), clicks)
        assertEquals(emptyList(), moves)
    }

    @Test
    fun aLongPressDragMovesTheRowPastOthers() {
        showRows()
        dragFirstRowBy(ROW_HEIGHT * 2)
        assertEquals(listOf(0 to 2), moves)
        assertEquals(emptyList(), clicks)
    }

    /** Holds the first row past the long-press timeout, then moves it by [distance] dp and lifts. */
    private fun dragFirstRowBy(distance: Int) {
        val row = compose.onNodeWithText("Clients")
        row.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(LONG_PRESS_MILLIS)
        row.performTouchInput {
            moveBy(Offset(0f, distance.dp.toPx()))
            up()
        }
        compose.waitForIdle()
    }

    private fun showRows() {
        compose.setContent {
            ReorderableColumn(
                listOf("Clients", "Projects", "Receipts"),
                key = { it },
                onMove = { from, to -> moves.add(from to to) },
            ) { name ->
                Text(
                    name,
                    Modifier
                        .dragOnLongPress()
                        .clickable { clicks.add(name) }
                        .fillMaxWidth()
                        .height(ROW_HEIGHT.dp),
                )
            }
        }
    }

    private companion object {
        const val ROW_HEIGHT = 48
        const val LONG_PRESS_MILLIS = 600L
    }
}
