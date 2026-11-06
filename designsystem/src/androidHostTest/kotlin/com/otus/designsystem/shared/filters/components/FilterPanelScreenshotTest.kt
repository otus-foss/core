package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.mail.folders.components.FilterFolderRow
import com.otus.designsystem.mail.folders.components.FolderCheck
import com.otus.designsystem.mail.folders.components.TreeFolder
import com.otus.designsystem.screenshot.screenScreenshot
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.controls.components.SegmentedToggle
import com.otus.designsystem.shared.filters.elements.ChipLabel
import com.otus.designsystem.shared.filters.elements.FilterChip
import com.otus.designsystem.shared.filters.elements.FilterChipRole
import com.otus.designsystem.shared.folders.elements.FolderGlyph
import com.otus.designsystem.shared.overlays.components.WithBackDispatcher
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.tokens.controls.ControlTokens
import com.otus.designsystem.theme.tokens.selection.FilterTokens
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
                    header = { FilterPanelTitle("Inbox", {}) { FilterResetAction({}, enabled = false) } },
                    footer = { FilterPanelFooter("Show 12 conversations", {}) },
                ) { Sections() }
            }
        }

    @Test
    fun fillHeight() =
        compose.screenScreenshot("filter_panel_fill_height") {
            WithBackDispatcher {
                FilterPanel(
                    open = true,
                    collapsedHeight = 112.dp,
                    onDismiss = {},
                    header = { FilterPanelTitle("Inbox", {}) },
                    footer = { FilterPanelFooter("Show 30 conversations", {}) },
                ) {
                    FilterSection("Type", Modifier.padding(horizontal = FilterTokens.bodyInset)) {
                        FilterChip("Newsletters", selected = false, onClick = {})
                        FilterChip("Calendar invites", selected = true, onClick = {})
                    }
                }
            }
        }

    @Test
    fun bodyUnderFooter() =
        compose.screenScreenshot("filter_panel_body_under_footer") {
            WithBackDispatcher {
                FilterPanel(
                    open = true,
                    collapsedHeight = 112.dp,
                    onDismiss = {},
                    header = { FilterPanelTitle("Inbox", {}) { FilterResetAction({}, enabled = true) } },
                    footer = { FilterPanelFooter("Show 12 conversations", {}) },
                ) { repeat(BODY_REPEATS) { Sections() } }
            }
        }

    @Test
    fun footerCounting() =
        compose.screenshot("filter_panel_footer_counting") {
            FilterPanelFooter("Show conversations", {}, counting = true)
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
                ) { FolderGlyph(FolderColor.Blue, size = FilterTokens.chipIconSize) }
                FilterFolderRow(
                    TreeFolder("Acme", depth = 1),
                    FolderCheck.Included,
                    {},
                ) { FolderGlyph(FolderColor.Default, size = FilterTokens.chipIconSize) }
                FilterFolderRow(
                    TreeFolder("Invoices", path = "Finance"),
                    FolderCheck.Off,
                    {},
                ) { FolderGlyph(FolderColor.Yellow, size = FilterTokens.chipIconSize) }
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
            FilterChip(ChipLabel("Acme", "Clients"), selected = true, onClick = {
            }) { FolderGlyph(FolderColor.Blue, size = FilterTokens.chipIconSize) }
            FilterChip("All folders…", selected = false, onClick = {}, role = FilterChipRole.Button) {
                FolderGlyph(FolderColor.Default, size = FilterTokens.chipIconSize)
            }
        }
        SegmentedToggle(
            listOf("Newest first", "Oldest first"),
            0,
            {},
            Modifier.padding(top = FilterTokens.sectionGap),
            height = ControlTokens.segmentHeight,
        )
    }
}

private const val BODY_REPEATS = 3
