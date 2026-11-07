package com.otus.designsystem.shared.sidebar.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SidebarEntryScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unselected() =
        compose.screenshot("sidebar_entry_unselected") {
            SidebarEntry("Receipts", Res.drawable.symbol_folder, selected = false, {}, WIDTH)
        }

    @Test
    fun selectedWithCount() =
        compose.screenshot("sidebar_entry_selected_count") {
            SidebarEntry("Inbox", Res.drawable.symbol_inbox, selected = true, {}, WIDTH) { SidebarCount("12") }
        }

    @Test
    fun nestedTree() =
        compose.screenshot("sidebar_entry_nested") {
            Column(WIDTH) {
                SidebarEntry(
                    "Projects",
                    Res.drawable.symbol_folder,
                    selected = false,
                    {},
                ) { SidebarExpandButton("Projects", expanded = true, {}) }
                SidebarEntry(
                    "Engine",
                    Res.drawable.symbol_folder,
                    selected = false,
                    {},
                    depth = 1,
                ) { SidebarExpandButton("Engine", expanded = false, {}) }
                SidebarEntry("Notes", Res.drawable.symbol_folder, selected = true, {}, depth = 1) { SidebarCount("3") }
            }
        }

    @Test
    fun longLabel() =
        compose.screenshot("sidebar_entry_long_label") {
            SidebarEntry(
                "Receipts from the Analytical Engine Society",
                Res.drawable.symbol_folder,
                selected = false,
                {},
                WIDTH,
            ) {
                SidebarCount("1,204")
            }
        }

    @Test
    fun expandButtonToggles() {
        var expanded = false
        compose.setContent { SidebarExpandButton("Projects", expanded, { expanded = it }) }
        compose.onNodeWithContentDescription("Expand Projects").assertIsOff().performClick()
        assertEquals(true, expanded)
    }

    @Test
    fun expandedButtonIsOn() {
        compose.setContent { SidebarExpandButton("Projects", expanded = true, {}) }
        compose.onNodeWithContentDescription("Collapse Projects").assertIsOn()
    }

    private companion object {
        val WIDTH = Modifier.width(280.dp)
    }
}
