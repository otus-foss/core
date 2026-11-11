package com.otusfoss.core.designsystem.shared.reorder.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReorderableTreeScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    // Projects holds Old, which holds Drafts.
    private val folders = listOf("Clients" to 0, "Projects" to 0, "Old" to 1, "Drafts" to 2, "Receipts" to 0)

    /** Projects and its nested rows move as one block, rounded only at its ends. */
    @Test
    fun draggingAParentTakesItsNestedRows() =
        compose.screenshot(
            "reorderable_tree_dragging_parent",
            {
                onNodeWithContentDescription("Reorder Projects").performTouchInput {
                    down(center)
                    repeat(STEPS) { moveBy(Offset(0f, DRAG / STEPS)) }
                }
            },
        ) {
            ReorderableTree(folders, key = { it.first }, depth = { it.second }, onDrop = { _, _ -> }) { (name, depth) ->
                Row(
                    Modifier.width(320.dp).height(48.dp).padding(start = (16 + 24 * depth).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(name, Modifier.weight(1f).fillMaxWidth())
                    DragHandle(name)
                }
            }
        }

    private companion object {
        const val STEPS = 10
        const val DRAG = 150f
    }
}
