package com.otus.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.otus.designsystem.components.TreeDropTarget.Before
import com.otus.designsystem.components.TreeDropTarget.Into
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReorderableTreeTest {
    @get:Rule
    val compose = createComposeRule()

    private val drops = mutableListOf<Pair<Int, TreeDropTarget>>()

    @Test
    fun droppingOnTheMiddleOfARowDropsIntoIt() {
        showTree()
        drag("Clients", rows = 1f)
        assertEquals(listOf<Pair<Int, TreeDropTarget>>(0 to Into(1)), drops)
    }

    @Test
    fun droppingNearTheEdgeOfARowDropsInTheGap() {
        showTree()
        drag("Receipts", rows = -1.6f)
        assertEquals(listOf<Pair<Int, TreeDropTarget>>(2 to Before(1)), drops)
    }

    @Test
    fun aRefusedTargetDropsNothing() {
        showTree(canDrop = { _, target -> target !is Into })
        drag("Clients", rows = 1f)
        assertTrue(drops.isEmpty())
    }

    /** Holds [name]'s row past the long-press timeout, then moves it by [rows] row heights and lifts. */
    private fun drag(
        name: String,
        rows: Float,
    ) {
        val row = compose.onNodeWithText(name)
        row.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(LONG_PRESS_MILLIS)
        row.performTouchInput {
            moveBy(Offset(0f, (ROW_HEIGHT * rows).dp.toPx()))
            up()
        }
        compose.waitForIdle()
    }

    private fun showTree(canDrop: (Int, TreeDropTarget) -> Boolean = { _, _ -> true }) {
        val rows = listOf("Clients" to 0, "Projects" to 1, "Receipts" to 0)
        compose.setContent {
            ReorderableTree(
                rows,
                key = { it.first },
                depth = { it.second },
                onDrop = { from, target -> drops.add(from to target) },
                canDrop = canDrop,
            ) { (name, _) ->
                Text(
                    name,
                    Modifier
                        .dragOnLongPress()
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
