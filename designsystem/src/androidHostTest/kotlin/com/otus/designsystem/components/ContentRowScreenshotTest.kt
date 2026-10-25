package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.elements.avatarFill
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_attach_file
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ContentRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val invoice =
        ContentRowText(
            title = "Invoice for March",
            subtitle = "Grace Hopper",
            trailingText = "9:41",
            metadata = "Your invoice is attached as a PDF.",
        )
    private val trip =
        ContentRowText(
            title = "Trip to Lisbon: final itinerary with flights and hotel",
            subtitle = "Ada Lovelace, Linus T.",
            trailingText = "Mon",
            metadata = "2 attachments",
            labels =
                listOf(
                    RowLabel("Travel"),
                    RowLabel("PDF", icon = Res.drawable.symbol_attach_file, highlighted = true),
                ),
        )

    @Test
    fun unread() = compose.pageScreenshot("content_row_unread") { Row(invoice, emphasized = true) }

    @Test
    fun readWithLabels() = compose.pageScreenshot("content_row_labels") { Row(trip, emphasized = false) }

    @Test
    fun selecting() =
        compose.pageScreenshot("content_row_selecting") {
            Column {
                Row(invoice, selection = RowSelection(selected = false))
                Row(trip, selection = RowSelection(selected = true))
            }
        }

    @Test
    fun joinedSelection() =
        compose.pageScreenshot("content_row_selection_joined") {
            Column {
                Row(invoice, selection = RowSelection(selected = true, joinedBelow = true))
                Row(trip, selection = RowSelection(selected = true, joinedAbove = true))
                Row(invoice.copy(title = "Receipt"), selection = RowSelection(selected = false))
            }
        }

    @Composable
    private fun Row(
        text: ContentRowText,
        emphasized: Boolean = false,
        selection: RowSelection? = null,
    ) {
        val sender = if (text.subtitle.startsWith("Ada")) DemoPeople.ada else DemoPeople.grace
        ContentRow(text, emphasized, if (emphasized) "Unread" else "Read", {}, selection) {
            Avatar(sender.initials, fill = avatarFill(sender.colorIndex))
        }
    }
}
