package com.otus.designsystem.shared.rows.elements

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActionRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun titleOnly() =
        compose.screenshot("action_row_title") {
            ActionRow("Folders", {}, Res.drawable.symbol_chevron_right, Modifier.width(320.dp))
        }

    @Test
    fun iconAndSummary() =
        compose.screenshot("action_row_icon_summary") {
            ActionRow(
                "Receipts",
                {},
                Res.drawable.symbol_chevron_right,
                Modifier.width(320.dp),
                summary = "Filed automatically",
                icon = Res.drawable.symbol_folder,
            )
        }

    @Test
    fun longText() =
        compose.screenshot("action_row_long_text") {
            ActionRow(
                "Forward a copy of every message from Ada Lovelace to the archive",
                {},
                Res.drawable.symbol_chevron_right,
                Modifier.width(320.dp),
                summary = "Applies to new mail in Inbox, Receipts, and every subfolder",
            )
        }
}
