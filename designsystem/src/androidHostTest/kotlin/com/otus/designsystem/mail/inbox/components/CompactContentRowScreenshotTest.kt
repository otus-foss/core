package com.otus.designsystem.mail.inbox.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.resources.symbol_reply
import com.otus.designsystem.shared.pages.components.pageScreenshot
import com.otus.designsystem.shared.people.components.DemoPeople
import com.otus.designsystem.shared.people.elements.Avatar
import com.otus.designsystem.shared.people.elements.AvatarBadge
import com.otus.designsystem.shared.people.elements.BadgedAvatar
import com.otus.designsystem.shared.people.elements.avatarFill
import com.otus.designsystem.shared.rows.components.RowSelection
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.tokens.rows.EntryTokens
import com.otus.designsystem.theme.tokens.rows.InboxTokens
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
            folders = listOf(RowFolderIcon(FolderColor.Green)),
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
                            RowFolderIcon(color = null, icon = Res.drawable.symbol_inbox),
                            RowFolderIcon(FolderColor.Purple),
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

    @Test
    fun cards() = compose.pageScreenshot("compact_row_cards") { Cards() }

    @Test
    fun cardsLight() =
        compose.pageScreenshot("compact_row_cards_light", ThemeChoice(ThemeBase.Light).palette) { Cards() }

    @Test
    fun cardsBlack() =
        compose.pageScreenshot("compact_row_cards_black", ThemeChoice(ThemeBase.Black).palette) { Cards() }

    /** Unread, read, and selected rows on the page color, as a list shows them. */
    @Composable
    private fun Cards() {
        Column(
            Modifier.background(OtusColors.bgRegular).padding(InboxTokens.pageInset),
            verticalArrangement = Arrangement.spacedBy(EntryTokens.gap),
        ) {
            Row(invoice)
            Row(invoice.copy(first = line("Ada Lovelace"), count = null, marks = emptyList()), dimmed = true)
            Row(invoice.copy(first = line("Linus Torvalds")), selection = RowSelection(selected = true))
        }
    }

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
