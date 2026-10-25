package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.elements.AvatarBadge
import com.otus.designsystem.elements.BadgedAvatar
import com.otus.designsystem.elements.FolderGlyphKind
import com.otus.designsystem.elements.avatarFill
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_reply
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.InboxTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompactContentRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val invoice =
        CompactRowText(
            first = line("Grace Hopper"),
            second = line("Invoice for March"),
            trailing = "9:41",
            count = "3",
            marks = listOf(RowMark.Attachment, RowMark.Flagged),
            folders = listOf(RowFolderIcon(FolderGlyphKind.Folder, FolderColor.Green)),
            preview = "Your invoice is attached as a PDF. Payment is due by the end of the month, as usual.",
        )

    @Test
    fun unread() = compose.pageScreenshot("compact_row_unread") { Row(invoice) }

    @Test
    fun read() = compose.pageScreenshot("compact_row_read") { Row(invoice, dimmed = true) }

    @Test
    fun youReplied() =
        compose.pageScreenshot("compact_row_you_replied") {
            Row(
                invoice.copy(
                    first = line("Trip to Lisbon: final itinerary"),
                    second =
                        RowLine(
                            listOf(
                                RowTextPart("You"),
                                RowTextPart(" to", quiet = true),
                                RowTextPart(" Margaret Knuth"),
                            ),
                            icon = Res.drawable.symbol_reply,
                        ),
                    marks = listOf(RowMark.Answered),
                ),
            )
        }

    @Test
    fun multiFolder() =
        compose.pageScreenshot("compact_row_multi_folder") {
            Row(
                invoice.copy(
                    marks = listOf(RowMark.ReadLater),
                    folders =
                        listOf(
                            RowFolderIcon(FolderGlyphKind.Inbox, null),
                            RowFolderIcon(FolderGlyphKind.Folder, FolderColor.Purple),
                        ),
                    moreFolders = 2,
                ),
            )
        }

    @Test
    fun withBadge() = compose.pageScreenshot("compact_row_badge") { Row(invoice, badge = true) }

    @Test
    fun selecting() =
        compose.pageScreenshot("compact_row_selecting") {
            Column {
                Row(invoice, selection = RowSelection(selected = false))
                Row(invoice.copy(first = line("Ada Lovelace")), selection = RowSelection(selected = true))
            }
        }

    @Test
    fun senderFirst() =
        compose.pageScreenshot("compact_row_sender_first") {
            Row(
                invoice.copy(
                    first = line("Ada Lovelace, Linus T., Grace Hopper, Edsger D."),
                    second = line("Notes on the analytical engine and a very long subject"),
                    countPlacement = CountPlacement.BeforeTrailing,
                ),
            )
        }

    @Test
    fun previewOneLine() = compose.pageScreenshot("compact_row_preview_one") { Row(invoice.copy(previewLines = 1)) }

    @Test
    fun previewTwoLines() = compose.pageScreenshot("compact_row_preview_two") { Row(invoice.copy(previewLines = 2)) }

    @Composable
    private fun Row(
        text: CompactRowText,
        dimmed: Boolean = false,
        selection: RowSelection? = null,
        badge: Boolean = false,
    ) {
        val sender = DemoPeople.grace
        CompactContentRow(
            text.copy(
                description = "Grace Hopper, Invoice for March, 3 mails",
                stateLabel = if (dimmed) "Read" else "Unread",
            ),
            dimmed = dimmed,
            onClick = {},
            selection = selection,
        ) {
            BadgedAvatar(if (badge) ({ AvatarBadge("AL", colorIndex = 0) }) else null) {
                Avatar(sender.initials, fill = avatarFill(sender.colorIndex), size = InboxTokens.compactAvatarSize)
            }
        }
    }
}

private fun line(text: String) = RowLine(listOf(RowTextPart(text)))
