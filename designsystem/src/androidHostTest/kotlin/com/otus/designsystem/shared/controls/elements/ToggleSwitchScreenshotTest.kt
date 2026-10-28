package com.otus.designsystem.shared.controls.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class ToggleSwitchScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun off() = compose.screenshot("toggle_switch_off") { ToggleSwitch(checked = false) }

    @Test
    fun on() = compose.screenshot("toggle_switch_on") { ToggleSwitch(checked = true) }
}
