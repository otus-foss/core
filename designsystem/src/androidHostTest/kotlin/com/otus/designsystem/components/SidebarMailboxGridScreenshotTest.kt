package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.elements.SidebarInboxEntry
import com.otus.designsystem.elements.SidebarMailboxTile
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_bookmark
import com.otus.designsystem.resources.symbol_delete
import com.otus.designsystem.resources.symbol_draft
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.resources.symbol_report
import com.otus.designsystem.resources.symbol_send
import com.otus.designsystem.screenshot.screenshot
import org.jetbrains.compose.resources.DrawableResource
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SidebarMailboxGridScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun inboxOverTiles() =
        compose.screenshot("sidebar_mailbox_grid") {
            Column(Modifier.width(320.dp)) {
                SidebarInboxEntry("Inbox", Res.drawable.symbol_inbox, selected = true, {}, count = "42")
                SidebarMailboxGrid(TILES) { (label, icon, count) ->
                    SidebarMailboxTile(label, icon, selected = false, {}, count = count)
                }
            }
        }

    private companion object {
        val TILES =
            listOf<Triple<String, DrawableResource, String?>>(
                Triple("Read later", Res.drawable.symbol_bookmark, "2"),
                Triple("Drafts", Res.drawable.symbol_draft, "1"),
                Triple("Sent", Res.drawable.symbol_send, null),
                Triple("Archive", Res.drawable.symbol_archive, "5"),
                Triple("Junk", Res.drawable.symbol_report, "4"),
                Triple("Trash", Res.drawable.symbol_delete, null),
            )
    }
}
