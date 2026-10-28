package com.otus.designsystem.shared.actionbar.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
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
}
