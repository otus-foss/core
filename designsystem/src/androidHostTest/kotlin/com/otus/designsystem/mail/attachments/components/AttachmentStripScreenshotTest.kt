package com.otus.designsystem.mail.attachments.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.rows.components.rememberUnfold
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AttachmentStripScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun closed() = compose.screenshot("attachment_strip_closed") { Attachments(expanded = false) }

    @Test
    fun opened() = compose.screenshot("attachment_strip_opened") { Attachments(expanded = true) }

    @Test
    fun openedLight() =
        compose.screenshot("attachment_strip_opened_light", palette = ThemeChoice(ThemeBase.Light).palette) {
            Attachments(expanded = true)
        }
}

@Composable
private fun Attachments(expanded: Boolean) {
    AttachmentStrip(
        listOf(
            AttachmentFile("Q4-plan-v3.pdf", "PDF · 240 KB · 4 pages", AttachmentKind.Pdf),
            AttachmentFile("whiteboard.jpg", "Image · 2.1 MB", AttachmentKind.Image),
            AttachmentFile("numbers.xlsx", "Spreadsheet · 1.9 MB", AttachmentKind.Sheet),
        ),
        "3 attachments · 4.2 MB",
        rememberUnfold(expanded),
        onToggle = {},
        AttachmentActions(onOpen = {}, onSave = {}, onSaveAll = {}),
    )
}
