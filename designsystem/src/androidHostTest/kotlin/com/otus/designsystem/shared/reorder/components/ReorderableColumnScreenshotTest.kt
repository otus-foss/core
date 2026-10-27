package com.otus.designsystem.shared.reorder.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.OtusColors
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReorderableColumnScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val folders = listOf("Inbox", "Receipts", "Travel", "Newsletters")

    @Test
    fun atRest() = compose.screenshot("reorderable_column_rest") { Folders() }

    @Test
    fun dragging() =
        compose.screenshot(
            "reorderable_column_dragging",
            {
                onNodeWithContentDescription("Reorder Inbox").performTouchInput {
                    down(center)
                    repeat(STEPS) { moveBy(Offset(0f, DRAG / STEPS)) }
                }
            },
        ) { Folders() }

    @Composable
    private fun Folders() =
        ReorderableColumn(folders, key = { it }, onMove = { _, _ -> }, background = OtusColors.bgDark) { name ->
            Row(
                Modifier.padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(name, Modifier.weight(1f))
                DragHandle(name)
            }
        }

    private companion object {
        const val STEPS = 10
        const val DRAG = 150f
    }
}
