package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_all_inbox
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
class SidebarRailItemScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun account() = compose.screenshot("sidebar_rail_account") { SidebarRailAccount("AL", "Ada Lovelace", false, {}) }

    @Test
    fun accountSelectedBadge() =
        compose.screenshot("sidebar_rail_account_selected_badge") {
            SidebarRailAccount("AL", "Ada Lovelace", true, {}, badge = "12")
        }

    @Test
    fun accountWideBadge() =
        compose.screenshot("sidebar_rail_account_wide_badge") {
            SidebarRailAccount("CB", "Charles Babbage", false, {}, badge = "999+")
        }

    @Test
    fun icon() =
        compose.screenshot("sidebar_rail_icon") {
            SidebarRailIcon(Res.drawable.symbol_all_inbox, "All", "All accounts", false, {})
        }

    @Test
    fun iconSelected() =
        compose.screenshot("sidebar_rail_icon_selected") {
            SidebarRailIcon(Res.drawable.symbol_all_inbox, "All", "All accounts", true, {}, badge = "4")
        }

    @Test
    fun rail() =
        compose.screenshot("sidebar_rail") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SidebarRailIcon(Res.drawable.symbol_all_inbox, "All", "All accounts", false, {})
                SidebarRailDivider()
                SidebarRailAccount("AL", "Ada Lovelace", true, {}, badge = "3")
                SidebarRailAccount("CB", "Charles Babbage", false, {})
                SidebarRailAdd("Add account", {})
            }
        }

    @Test
    fun selectedAccountIsSelected() {
        compose.setContent { SidebarRailAccount("AL", "Ada Lovelace", true, {}) }
        compose.onNodeWithContentDescription("Ada Lovelace").assertIsSelected()
    }
}
