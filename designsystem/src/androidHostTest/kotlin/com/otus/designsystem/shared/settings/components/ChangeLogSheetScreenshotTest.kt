package com.otus.designsystem.shared.settings.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.screenshot.screenScreenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChangeLogSheetScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sections() =
        compose.screenScreenshot("change_log_sheet") {
            ChangeLogSheet(
                "Update",
                listOf(
                    ChangeLogSection("Sidebar", listOf("Show only the scoped account's folders")),
                    ChangeLogSection(
                        "Compose",
                        listOf("Attach files and images, and morph the bars", "Add the Write page with sending"),
                    ),
                ),
                actionLabel = "Install update",
                onAction = {},
                secondaryLabel = "Changelog",
                onSecondary = {},
                onDismiss = {},
            )
        }

    @Test
    fun history() =
        compose.screenScreenshot("change_log_history_sheet") {
            ChangeLogHistorySheet(
                "Changelog",
                listOf(
                    ChangeLogRelease(
                        "17.10.26",
                        listOf(
                            ChangeLogSection("Compose", listOf("Add the Write page with sending and reply threads")),
                        ),
                    ),
                    ChangeLogRelease(
                        "13.10.26",
                        listOf(
                            ChangeLogSection("Settings", listOf("Add the Theme page and rework Appearance rows")),
                            ChangeLogSection("Inbox", listOf("Select messages by dragging", "Fill dragged pills")),
                        ),
                        installed = true,
                    ),
                    ChangeLogRelease("12.10.26", emptyList(), installed = true),
                ),
                onDismiss = {},
            )
        }

    @Test
    fun empty() =
        compose.screenScreenshot("change_log_sheet_empty") {
            ChangeLogSheet("Update", emptyList(), "Install update", {}, "Changelog", {}, onDismiss = {})
        }
}
