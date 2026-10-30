package com.otus.designsystem.shared.selection.elements

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.people.elements.Avatar
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SelectionRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unchecked() =
        compose.screenshot("selection_row_unchecked") {
            SelectionRow("Receipts", selected = false, {}, WIDTH)
        }

    @Test
    fun checked() = compose.screenshot("selection_row_checked") { SelectionRow("Receipts", selected = true, {}, WIDTH) }

    @Test
    fun singleChoice() =
        compose.screenshot("selection_row_single_choice") {
            SelectionRow("Receipts", selected = true, {}, WIDTH, singleChoice = true)
        }

    @Test
    fun subtitleAndAvatar() =
        compose.screenshot("selection_row_subtitle_avatar") {
            SelectionRow(
                "Ada Lovelace",
                selected = false,
                {},
                WIDTH,
                subtitle = "ada@example.com",
                leading = { Avatar("AL") },
            )
        }

    @Test
    fun icon() =
        compose.screenshot("selection_row_icon") {
            SelectionRow(
                "Receipts",
                selected = false,
                {},
                WIDTH,
                singleChoice = true,
                leading = { Symbol(Res.drawable.symbol_folder) },
            )
        }

    @Test
    fun longTitle() =
        compose.screenshot("selection_row_long_title") {
            val title = "Receipts from the Analytical Engine Society, 2026 and earlier"
            SelectionRow(title, selected = true, {}, WIDTH, subtitle = "1,204 messages")
        }

    @Test
    fun checkboxStateFollowsSelected() {
        compose.setContent { SelectionRow("Receipts", selected = true, {}) }
        compose.onNodeWithText("Receipts", useUnmergedTree = false).assertIsOn()
    }

    private companion object {
        val WIDTH = Modifier.width(320.dp)
    }
}
