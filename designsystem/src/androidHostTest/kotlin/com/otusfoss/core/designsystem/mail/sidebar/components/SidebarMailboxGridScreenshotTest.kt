package com.otusfoss.core.designsystem.mail.sidebar.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.mail.sidebar.elements.SidebarMailboxTile
import com.otusfoss.core.designsystem.mail.sidebar.elements.SidebarTileSize
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_archive
import com.otusfoss.core.designsystem.resources.symbol_bookmark
import com.otusfoss.core.designsystem.resources.symbol_delete
import com.otusfoss.core.designsystem.resources.symbol_draft
import com.otusfoss.core.designsystem.resources.symbol_inbox
import com.otusfoss.core.designsystem.resources.symbol_report
import com.otusfoss.core.designsystem.resources.symbol_send
import com.otusfoss.core.designsystem.screenshot.screenshot
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
    fun tilesOfEverySize() =
        compose.screenshot("sidebar_mailbox_grid") {
            SidebarMailboxGrid(TILES, Modifier.width(320.dp), sizeOf = { it.size }) { tile ->
                SidebarMailboxTile(
                    tile.label,
                    tile.icon,
                    selected = tile.label == "Inbox",
                    {},
                    count = tile.count,
                    size = tile.size,
                )
            }
        }

    @Test
    fun editorShowsHandlesOnEveryTile() =
        compose.screenshot("sidebar_tile_editor") {
            SidebarTileEditor(
                TILES,
                tileOf = { EditableTile(it.label, it.label, it.size) },
                onMove = { _, _ -> },
                onResize = { _, _ -> },
                onRemove = {},
                modifier = Modifier.width(344.dp),
            ) { tile -> SidebarMailboxTile(tile.label, tile.icon, selected = false, {}, size = tile.size) }
        }

    private data class Tile(
        val label: String,
        val icon: DrawableResource,
        val count: String? = null,
        val size: SidebarTileSize = SidebarTileSize.Small,
    )

    private companion object {
        // Junk is too wide for the rest of its row, so it starts the next and leaves a gap.
        val TILES =
            listOf(
                Tile("Inbox", Res.drawable.symbol_inbox, "42", SidebarTileSize.Full),
                Tile("Read later", Res.drawable.symbol_bookmark, "2"),
                Tile("Drafts", Res.drawable.symbol_draft, "1", SidebarTileSize.Wide),
                Tile("Sent", Res.drawable.symbol_send),
                Tile("Archive", Res.drawable.symbol_archive, "5"),
                Tile("Junk", Res.drawable.symbol_report, "4", SidebarTileSize.Wide),
                Tile("Trash", Res.drawable.symbol_delete),
            )
    }
}
