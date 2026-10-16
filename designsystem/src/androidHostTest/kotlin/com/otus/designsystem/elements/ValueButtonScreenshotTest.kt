package com.otus.designsystem.elements

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.OtusColors
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ValueButtonScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun plain() = compose.screenshot("value_button_plain") { ValueButton("Every 15 minutes") }

    @Test
    fun swatch() = compose.screenshot("value_button_swatch") { ValueButton("Blue", swatch = OtusColors.accentRegular) }

    @Test
    fun darkFill() = compose.screenshot("value_button_fill_dark") { ValueButton("#1F3A5F", fill = Color(0xFF1F3A5F)) }

    @Test
    fun lightFill() = compose.screenshot("value_button_fill_light") { ValueButton("#F2C94C", fill = Color(0xFFF2C94C)) }

    @Test
    fun longValue() =
        compose.screenshot("value_button_long_value") {
            ValueButton("ada.lovelace@analytical-engine.example.com", swatch = OtusColors.accentLight)
        }
}
