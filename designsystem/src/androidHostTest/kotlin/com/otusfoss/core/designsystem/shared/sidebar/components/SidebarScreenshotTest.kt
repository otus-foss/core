package com.otusfoss.core.designsystem.shared.sidebar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_archive
import com.otusfoss.core.designsystem.resources.symbol_draft
import com.otusfoss.core.designsystem.resources.symbol_folder
import com.otusfoss.core.designsystem.resources.symbol_inbox
import com.otusfoss.core.designsystem.resources.symbol_send
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.overlays.components.WithBackDispatcher
import com.otusfoss.core.designsystem.shared.pages.components.PageScaffold
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import com.otusfoss.core.designsystem.shared.sidebar.elements.SidebarCount
import com.otusfoss.core.designsystem.shared.sidebar.elements.SidebarEntry
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

    @Test
    fun header() = compose.screenshot("sidebar_header") { Box(Modifier.height(80.dp)) { Header() } }

    @Composable
    private fun PageWithSidebar(state: SidebarState) =
        WithBackDispatcher {
            SidebarSwipeArea(state) {
                PageScaffold("Inbox", navigationLabel = "Open navigation", onNavigation = {}) {}
            }
            Sidebar(
                state,
                title = "Work",
                header = { Header() },
                footer = { SidebarFooter("Nightly", "Build 412") },
            ) { Entries() }
        }

    @Composable
    private fun Header() =
        SidebarHeader(
            "Work",
            listOf(ChipAvatar("WK", colorIndex = 0)),
            pickLabel = "Show mail from",
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
        SidebarEntry("Receipts", Res.drawable.symbol_folder, selected = false, {})
        SidebarEntry("2026", Res.drawable.symbol_folder, selected = false, {}, depth = 1) { SidebarCount("3") }
        SidebarEntry("Travel", Res.drawable.symbol_folder, selected = false, {})
    }
}
