package com.otus.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.elements.SidebarCount
import com.otus.designsystem.elements.SidebarEntry
import com.otus.designsystem.elements.SidebarHeading
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_draft
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.resources.symbol_send
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
class SidebarScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun openOverPage() =
        compose.screenshot("sidebar_open") {
            PageWithSidebar(remember { SidebarState(initiallyOpen = true) })
        }

    @Test
    fun pageBehindClosedSidebar() =
        compose.screenshot("sidebar_closed") {
            PageWithSidebar(remember { SidebarState() })
        }

    @Composable
    private fun PageWithSidebar(state: SidebarState) =
        WithBackDispatcher {
            SidebarSwipeArea(state) {
                PageScaffold("Inbox", navigationLabel = "Open navigation", onNavigation = {}) {}
            }
            Sidebar(state, title = "Work", header = { Header() }) { Entries() }
        }

    @Composable
    private fun Header() =
        SidebarHeader(
            "Work",
            listOf(DemoPeople.ada),
            pickLabel = "Choose account",
            onPick = {},
            onSettings = {},
            subtitle = "work@example.com",
        )

    @Composable
    private fun Entries() {
        SidebarEntry("Inbox", Res.drawable.symbol_inbox, selected = true, {}) { SidebarCount("12") }
        SidebarEntry("Drafts", Res.drawable.symbol_draft, selected = false, {}) { SidebarCount("2") }
        SidebarEntry("Sent", Res.drawable.symbol_send, selected = false, {})
        SidebarEntry("Archive", Res.drawable.symbol_archive, selected = false, {})
        SidebarHeading("Folders", expanded = true, {})
        SidebarEntry("Receipts", Res.drawable.symbol_folder, selected = false, {})
        SidebarEntry("2026", Res.drawable.symbol_folder, selected = false, {}, depth = 1) { SidebarCount("3") }
        SidebarEntry("Travel", Res.drawable.symbol_folder, selected = false, {})
    }
}
