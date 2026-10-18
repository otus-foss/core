package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.elements.ChipLabel
import com.otus.designsystem.elements.FilterChip
import com.otus.designsystem.elements.FilterChipRole
import com.otus.designsystem.elements.FolderGlyph
import com.otus.designsystem.screenshot.screenScreenshot
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.FolderColor
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FilterPanelScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun open() =
        compose.screenScreenshot("filter_panel_open") {
            WithBackDispatcher {
                FilterPanel(
                    open = true,
                    collapsedHeight = 112.dp,
                    onDismiss = {},
                    header = { FilterPanelHeader("Filters", {}, closeDescription = "Close filters") },
                    footer = { FilterPanelFooter("Show 12 conversations", {}, {}, resetEnabled = false) },
                    modifier = Modifier.padding(8.dp),
                ) { Sections() }
            }
        }

    @Test
    fun footerCounting() =
        compose.screenshot("filter_panel_footer_counting") {
            FilterPanelFooter("Show conversations", {}, {}, counting = true)
        }

    @Test
    fun folderRows() =
        compose.screenshot("filter_folder_rows") {
            Column {
                FilterFolderRow(
                    TreeFolder("Clients"),
                    FolderCheck.On,
                    {},
                    expanded = true,
                ) { FolderGlyph(FolderColor.Blue) }
                FilterFolderRow(
                    TreeFolder("Acme", depth = 1),
                    FolderCheck.Included,
                    {},
                ) { FolderGlyph(FolderColor.Default) }
                FilterFolderRow(
                    TreeFolder("Invoices", path = "Finance"),
                    FolderCheck.Off,
                    {},
                ) { FolderGlyph(FolderColor.Yellow) }
            }
        }
}

@Composable
private fun Sections() {
    Column(Modifier.padding(horizontal = FilterTokens.bodyInset)) {
        FilterSection("Show") {
            FilterChip("Unread", selected = true, onClick = {})
            FilterChip("Flagged", selected = false, onClick = {})
            FilterChip("Waiting for reply", selected = false, onClick = {})
        }
        FilterSection("In folder", Modifier.padding(top = FilterTokens.sectionGap)) {
            FilterChip(ChipLabel("Acme", "Clients"), selected = true, onClick = {}) { FolderGlyph(FolderColor.Blue) }
            FilterChip("All folders…", selected = false, onClick = {}, role = FilterChipRole.Button) {
                FolderGlyph(FolderColor.Default)
            }
        }
        SegmentedToggle(
            listOf("Newest first", "Oldest first"),
            0,
            {},
            Modifier.padding(top = FilterTokens.sectionGap),
            height = FilterTokens.segmentHeight,
        )
    }
}
