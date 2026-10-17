package com.otus.designsystem.components

import androidx.compose.material.Text
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.otus.designsystem.screenshot.screenScreenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DialogScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun actionDialogWithMessage() =
        compose.screenScreenshot("action_dialog_message") {
            ActionDialog(
                "Discard draft?",
                listOf(DialogAction("Discard", {}, primary = true), DialogAction("Keep editing", {})),
                onDismiss = {},
                message = "The draft to Ada Lovelace is deleted and cannot be restored.",
            )
        }

    @Test
    fun actionDialogMenu() =
        compose.screenScreenshot("action_dialog_menu") {
            ActionDialog(
                "Receipts",
                listOf(DialogAction("Rename", {}), DialogAction("Move", {}), DialogAction("Delete folder", {})),
                onDismiss = {},
            )
        }

    @Test
    fun inputDialog() =
        compose.screenScreenshot("input_dialog") {
            InputDialog("New folder", "Name", "Create", {}, {}, initial = "Travel")
        }

    @Test
    fun inputDialogProblem() =
        compose.screenScreenshot("input_dialog_problem") {
            InputDialog("Rename folder", "Name", "Rename", {}, {}, problem = { "Enter a name" })
        }

    @Test
    fun choiceDialog() =
        compose.screenScreenshot("choice_dialog") {
            ChoiceDialog(
                "Swipe right",
                listOf("Archive", "Delete", "Mark as read", "Flag"),
                selected = "Archive",
                label = { it },
                onSelect = {},
                onDismiss = {},
                detail = { if (it == "Delete") "Moves the message to Trash" else null },
            )
        }

    @Test
    fun confirmDialog() =
        compose.screenScreenshot("confirm_dialog") {
            ConfirmDialog(
                "Remove account?",
                "Mail stored on this phone for ada@example.com is removed. The server keeps it.",
                "Remove",
                {},
                {},
            )
        }

    @Test
    fun dialogFrameWithCloseButton() =
        compose.screenScreenshot("dialog_frame") {
            DialogFrame("About this message", {}) {
                Text("Sent through a mailing list.")
            }
        }

    @Test
    fun confirmRunsAfterClosing() {
        val calls = mutableListOf<String>()
        compose.setContent {
            ConfirmDialog(
                "Delete folder?",
                "Its messages move to Trash.",
                "Delete",
                onConfirm = { calls += "confirm" },
                onDismiss = { calls += "dismiss" },
            )
        }
        compose.onNode(isDialog()).assertExists()
        compose.onNodeWithText("Delete").performClick()
        assertEquals(listOf("dismiss", "confirm"), calls)
    }
}
