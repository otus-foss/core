package com.otusfoss.core.designsystem.shared.actionbar.elements

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_archive
import com.otusfoss.core.designsystem.resources.symbol_self_improvement
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.theme.colors.ThemeBase
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice
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
class BarIconButtonScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun enabled() =
        compose.screenshot("bar_icon_button_enabled") {
            BarIconButton("Archive", Res.drawable.symbol_archive, {})
        }

    @Test
    fun disabled() =
        compose.screenshot("bar_icon_button_disabled") {
            BarIconButton("Archive", Res.drawable.symbol_archive, {}, enabled = false)
        }

    @Test
    fun disabledIgnoresClicks() {
        var clicks = 0
        compose.setContent { BarIconButton("Archive", Res.drawable.symbol_archive, { clicks++ }, enabled = false) }
        compose.onNodeWithContentDescription("Archive").performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun on() =
        compose.screenshot("bar_icon_button_on") {
            BarIconButton("Leave zen mode", Res.drawable.symbol_self_improvement, {}, on = true)
        }

    @Test
    fun onLight() =
        compose.screenshot("bar_icon_button_on_light", palette = ThemeChoice(ThemeBase.Light).palette) {
            BarIconButton("Leave zen mode", Res.drawable.symbol_self_improvement, {}, on = true)
        }

    @Test
    fun onBlack() =
        compose.screenshot("bar_icon_button_on_black", palette = ThemeChoice(ThemeBase.Black).palette) {
            BarIconButton("Leave zen mode", Res.drawable.symbol_self_improvement, {}, on = true)
        }

    @Test
    fun onlyAToggleSaysWhetherItIsSelected() {
        compose.setContent {
            BarIconButton("Leave zen mode", Res.drawable.symbol_self_improvement, {}, on = true)
            BarIconButton("Zen mode", Res.drawable.symbol_self_improvement, {}, on = false)
            BarIconButton("Archive", Res.drawable.symbol_archive, {})
        }
        compose.onNodeWithContentDescription("Leave zen mode").assertIsSelected()
        compose.onNodeWithContentDescription("Zen mode").assertIsNotSelected()
        compose
            .onNodeWithContentDescription("Archive")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
    }
}
