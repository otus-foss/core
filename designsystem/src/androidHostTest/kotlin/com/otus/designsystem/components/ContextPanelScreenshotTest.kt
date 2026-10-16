package com.otus.designsystem.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.elements.Chip
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_filter_list
import com.otus.designsystem.resources.symbol_menu
import com.otus.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ContextPanelScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun chipsBelow() = compose.screenshot("context_panel_below") { Panel(ChipPlacement.Below) }

    @Test
    fun chipsOverlapping() = compose.screenshot("context_panel_overlapping") { Panel(ChipPlacement.Overlapping) }

    @Test
    fun noChips() = compose.screenshot("context_panel_none") { Panel(ChipPlacement.None) }

    @Test
    fun titleOnly() = compose.screenshot("context_panel_title") { ContextPanel { Text("Settings") } }

    @Composable
    private fun Panel(placement: ChipPlacement) {
        ContextPanel(
            placement = placement,
            navigation = {
                IconButton("Open folders", {}, outlined = false) { Symbol(Res.drawable.symbol_menu) }
            },
            trailingAction = { IconButton("Filter", {}) { Symbol(Res.drawable.symbol_filter_list) } },
            chips = {
                item("unread") { Chip("Unread", {}, selected = true, detail = "4") }
                item("flagged") { Chip("Flagged", {}) }
                item("attachments") { Chip("Attachments", {}) }
            },
        ) { Text("Inbox") }
    }
}
