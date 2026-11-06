package com.otus.designsystem.shared.selection.components

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
class CardPickRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unchecked() =
        compose.screenshot("selection_row_unchecked") {
            PickRow("Receipts", null, PickMark(false, multiple = true, style = PickStyle.Card), {}, WIDTH)
        }

    @Test
    fun checked() =
        compose.screenshot("selection_row_checked") {
            PickRow("Receipts", null, PickMark(true, multiple = true, style = PickStyle.Card), {}, WIDTH)
        }

    @Test
    fun singleChoice() =
        compose.screenshot("selection_row_single_choice") {
            PickRow("Receipts", null, PickMark(true, style = PickStyle.Card), {}, WIDTH)
        }

    @Test
    fun subtitleAndAvatar() =
        compose.screenshot("selection_row_subtitle_avatar") {
            PickRow(
                "Ada Lovelace",
                "ada@example.com",
                PickMark(false, multiple = true, style = PickStyle.Card),
                {},
                WIDTH,
                leading = { Avatar("AL") },
            )
        }

    @Test
    fun icon() =
        compose.screenshot("selection_row_icon") {
            PickRow(
                "Receipts",
                null,
                PickMark(false, style = PickStyle.Card),
                {},
                WIDTH,
                leading = { Symbol(Res.drawable.symbol_folder) },
            )
        }

    @Test
    fun longTitle() =
        compose.screenshot("selection_row_long_title") {
            val title = "Receipts from the Analytical Engine Society, 2026 and earlier"
            PickRow(title, "1,204 messages", PickMark(true, multiple = true, style = PickStyle.Card), {}, WIDTH)
        }

    @Test
    fun checkboxStateFollowsSelected() {
        compose.setContent {
            PickRow("Receipts", null, PickMark(true, multiple = true, style = PickStyle.Card), {})
        }
        compose.onNodeWithText("Receipts", useUnmergedTree = false).assertIsOn()
    }

    private companion object {
        val WIDTH = Modifier.width(320.dp)
    }
}
