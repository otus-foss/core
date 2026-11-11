package com.otusfoss.core.designsystem.shared.filters.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.mail.folders.components.FilterFolderRow
import com.otusfoss.core.designsystem.mail.folders.components.FolderCheck
import com.otusfoss.core.designsystem.mail.folders.components.TreeFolder
import com.otusfoss.core.designsystem.screenshot.screenScreenshot
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.components.SegmentedToggle
import com.otusfoss.core.designsystem.shared.controls.elements.Chip
import com.otusfoss.core.designsystem.shared.controls.elements.ChipLabel
import com.otusfoss.core.designsystem.shared.controls.elements.ChipLook
import com.otusfoss.core.designsystem.shared.controls.elements.ChipRole
import com.otusfoss.core.designsystem.shared.controls.elements.ChipStyle
import com.otusfoss.core.designsystem.shared.folders.elements.FolderGlyph
import com.otusfoss.core.designsystem.shared.overlays.components.WithBackDispatcher
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.selection.FilterTokens
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
                        Chip("Newsletters", {}, selected = false, style = ChipStyle(look = ChipLook.Raised))
                        Chip("Calendar invites", {}, selected = true, style = ChipStyle(look = ChipLook.Raised))
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
                ) { FolderGlyph(FolderColor.Blue, size = ControlTokens.chipIconSize) }
                FilterFolderRow(
                    TreeFolder("Acme", depth = 1),
                    FolderCheck.Included,
                    {},
                ) { FolderGlyph(FolderColor.Default, size = ControlTokens.chipIconSize) }
                FilterFolderRow(
                    TreeFolder("Invoices", path = "Finance"),
                    FolderCheck.Off,
                    {},
                ) { FolderGlyph(FolderColor.Yellow, size = ControlTokens.chipIconSize) }
            }
        }
}

@Composable
private fun Sections() {
    Column(Modifier.padding(horizontal = FilterTokens.bodyInset)) {
        FilterSection("Show") {
            Chip("Unread", {}, selected = true, style = ChipStyle(look = ChipLook.Raised))
            Chip("Flagged", {}, selected = false, style = ChipStyle(look = ChipLook.Raised))
            Chip("Waiting for reply", {}, selected = false, style = ChipStyle(look = ChipLook.Raised))
        }
        FilterSection("In folder", Modifier.padding(top = FilterTokens.sectionGap)) {
            Chip(ChipLabel("Acme", "Clients"), {
            }, selected = true, style = ChipStyle(look = ChipLook.Raised)) {
                FolderGlyph(
                    FolderColor.Blue,
                    size = ControlTokens.chipIconSize,
                )
            }
            Chip("All folders…", {}, selected = false, style = ChipStyle(ChipRole.Button, ChipLook.Raised)) {
                FolderGlyph(FolderColor.Default, size = ControlTokens.chipIconSize)
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
