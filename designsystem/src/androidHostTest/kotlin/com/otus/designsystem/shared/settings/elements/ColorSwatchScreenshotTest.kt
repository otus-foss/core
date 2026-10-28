package com.otus.designsystem.shared.settings.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.colors.OtusColors
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ColorSwatchScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unselected() =
        compose.screenshot("color_swatch_unselected") {
            ColorSwatch(OtusColors.accentRegular, "Blue", false, {})
        }

    @Test
    fun selected() =
        compose.screenshot("color_swatch_selected") { ColorSwatch(OtusColors.accentRegular, "Blue", true, {}) }

    @Test
    fun add() = compose.screenshot("color_swatch_add") { AddSwatch("Custom color", {}) }

    @Test
    fun row() =
        compose.screenshot("color_swatch_row") {
            Row {
                ColorSwatch(OtusColors.accentLight, "Light blue", false, {})
                ColorSwatch(OtusColors.accentRegular, "Blue", true, {})
                ColorSwatch(OtusColors.text1, "White", false, {})
                AddSwatch("Custom color", {})
            }
        }

    @Test
    fun selectedSwatchIsSelected() {
        compose.setContent { ColorSwatch(OtusColors.accentRegular, "Blue", true, {}) }
        compose.onNodeWithContentDescription("Blue").assertIsSelected()
    }
}
