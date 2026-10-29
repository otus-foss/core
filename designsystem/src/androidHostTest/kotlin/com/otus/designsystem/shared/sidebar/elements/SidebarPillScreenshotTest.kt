package com.otus.designsystem.shared.sidebar.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_draft
import com.otus.designsystem.resources.symbol_flag
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.controls.elements.IconPill
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SidebarPillScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unselected() =
        compose.screenshot("sidebar_pill_unselected") {
            SidebarPill("Drafts", Res.drawable.symbol_draft, selected = false, {})
        }

    @Test
    fun selectedWithCount() =
        compose.screenshot("sidebar_pill_selected_count") {
            SidebarPill("Inbox", Res.drawable.symbol_inbox, selected = true, {}, count = "12")
        }

    @Test
    fun besideIconPill() =
        compose.screenshot("sidebar_pill_row") {
            Row {
                SidebarPill("Inbox", Res.drawable.symbol_inbox, selected = false, {}, count = "3")
                IconPill("Flagged", Res.drawable.symbol_flag, selected = true, {})
            }
        }
}
