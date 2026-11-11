package com.otusfoss.core.designsystem.shared.panels.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_tune
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.elements.Chip
import com.otusfoss.core.designsystem.shared.controls.elements.ChipLabel
import com.otusfoss.core.designsystem.shared.controls.elements.IconButton
import com.otusfoss.core.designsystem.shared.controls.elements.IconButtonLook
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.panels.elements.PanelNavigation
import com.otusfoss.core.designsystem.shared.panels.elements.PanelNavigationIcon
import com.otusfoss.core.designsystem.shared.panels.elements.PanelTitle
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import com.otusfoss.core.designsystem.theme.colors.ThemeBase
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens
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
    fun inboxAtTheTop() = compose.screenshot("context_panel_inbox") { Inbox(0f) }

    @Test
    fun inboxHalfCollapsed() = compose.screenshot("context_panel_inbox_collapse_50") { Inbox(HALF) }

    @Test
    fun inboxCollapsed() = compose.screenshot("context_panel_inbox_collapsed") { Inbox(1f) }

    @Test
    fun inboxLight() =
        compose.screenshot("context_panel_inbox_light", palette = ThemeChoice(ThemeBase.Light).palette) {
            Inbox(0f)
        }

    @Test
    fun chipsBelow() = compose.screenshot("context_panel_below") { Rail(ChipPlacement.Below) }

    @Test
    fun noChips() = compose.screenshot("context_panel_none") { Rail(ChipPlacement.None) }

    @Test
    fun subpage() =
        compose.screenshot("context_panel_title") {
            ContextPanel(placement = ChipPlacement.None, navigation = { PanelNavigation("Back", {}) }) {
                ExpandingSearch("Settings", PageSearch("", {}, "Search settings"), open = false, {}) { 0f }
            }
        }

    @Test
    fun closeIcon() =
        compose.screenshot("context_panel_close") {
            ContextPanel(
                placement = ChipPlacement.None,
                navigation = { PanelNavigation("Stop selecting", {}, icon = PanelNavigationIcon.Close) },
            ) { PanelTitle("3 selected", Modifier.padding(start = PanelTokens.titleStart)) }
        }

    @Composable
    private fun Inbox(collapse: Float) {
        ContextPanel(
            placement = ChipPlacement.Overlapping,
            navigation = { PanelNavigation("Open navigation", {}) },
            trailingAction = { Filter() },
            chips = { Pills() },
            motion = PanelMotion(collapse = { collapse }),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PanelSearchPill("Search Inbox", {}, Modifier.weight(1f), collapse = { collapse }, filter = { Filter() })
                AccountStack(
                    listOf(ChipAvatar("O", colorIndex = 1), ChipAvatar("F", colorIndex = 3)),
                    "Two accounts",
                    onClick = {},
                    collapse = { collapse },
                )
            }
        }
    }

    @Composable
    private fun Rail(placement: ChipPlacement) {
        ContextPanel(
            placement = placement,
            navigation = { PanelNavigation("Open navigation", {}) },
            trailingAction = { Filter() },
            chips = { Pills() },
        ) { PanelTitle("Inbox", Modifier.padding(start = PanelTokens.titleStart)) }
    }

    @Composable
    private fun ContextPanelScope.Pills() {
        item("unread") { Chip(ChipLabel("Unread", detail = "4"), {}, selected = true) }
        item("files") { Chip("Files", {}) }
        item("later") { Chip("Later", {}) }
    }

    @Composable
    private fun Filter() {
        IconButton("Filter", {}, look = IconButtonLook.Bare) { Symbol(Res.drawable.symbol_tune) }
    }

    private companion object {
        const val HALF = 0.5f
    }
}
