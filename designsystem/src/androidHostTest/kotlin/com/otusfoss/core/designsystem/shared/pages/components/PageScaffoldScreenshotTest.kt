package com.otusfoss.core.designsystem.shared.pages.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_chevron_right
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.overlays.components.WithBackDispatcher
import com.otusfoss.core.designsystem.shared.settings.components.SettingRow
import com.otusfoss.core.designsystem.shared.settings.components.SettingToggleRow
import com.otusfoss.core.designsystem.shared.settings.elements.ValueButton
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PageScaffoldScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun withSections() =
        compose.screenshot("page_scaffold_sections") {
            PageScaffold("Settings", navigationLabel = "Open navigation", onNavigation = {}) {
                PageSection("Reading") {
                    SettingToggleRow("Mark read on open", checked = true, {})
                    SettingRow("Swipe actions", {}) { Symbol(Res.drawable.symbol_chevron_right) }
                }
                PageSection("Appearance") {
                    SettingRow("Theme", {}) { ValueButton("Otus") }
                }
                PageNote("Changes apply to every account on this device.")
            }
        }

    @Test
    fun subpage() =
        compose.screenshot("page_scaffold_subpage") {
            WithBackDispatcher {
                Subpage("Notifications", onBack = {}) {
                    SettingToggleRow("New mail", checked = true, {}, description = "Inbox only")
                    SettingToggleRow("Sound", checked = false, {})
                }
            }
        }
}
