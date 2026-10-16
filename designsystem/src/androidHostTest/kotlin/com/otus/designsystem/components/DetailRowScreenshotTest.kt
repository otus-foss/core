package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_inbox
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun withDetails() =
        compose.pageScreenshot("detail_row_details") {
            DetailRow("Inbox", listOf("ada@example.com", "1,204 messages"), {}) { Symbol(Res.drawable.symbol_inbox) }
        }

    @Test
    fun disabled() =
        compose.pageScreenshot("detail_row_disabled") {
            DetailRow("Shared folders", listOf("Not offered by this server"), {}, enabled = false) {
                Symbol(Res.drawable.symbol_folder)
            }
        }

    @Test
    fun nestedAndTruncated() =
        compose.pageScreenshot("detail_row_nested") {
            Column {
                DetailRow("Projects", emptyList(), {}) { Symbol(Res.drawable.symbol_folder) }
                DetailRow("Otus", emptyList(), {}, depth = 1) { Symbol(Res.drawable.symbol_folder) }
                DetailRow(
                    "Design reviews and every thread about the calendar week view",
                    listOf("Last message from Grace Hopper about the weekly planning meeting"),
                    {},
                    depth = 2,
                    maxLines = 1,
                ) { Symbol(Res.drawable.symbol_folder) }
            }
        }
}
