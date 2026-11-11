package com.otusfoss.core.designsystem.shared.settings.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.screenshot.screenshot
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
class ColorSliderScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun positions() =
        compose.screenshot("color_slider_positions") {
            Column(Modifier.width(300.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(0f, 0.5f, 1f).forEach { ColorSlider(it, {}, HUES, "Hue") }
            }
        }

    @Test
    fun customRange() =
        compose.screenshot("color_slider_range") {
            ColorSlider(270f, {}, HUES, "Hue", Modifier.width(300.dp), range = 0f..360f)
        }

    @Test
    fun setProgressClampsToRange() {
        var value = 0f
        compose.setContent { ColorSlider(value, { value = it }, HUES, "Hue", range = 0f..360f) }
        compose.onNodeWithContentDescription("Hue").performSemanticsAction(SemanticsActions.SetProgress) { it(400f) }
        assertEquals(360f, value)
    }

    private companion object {
        val HUES =
            Brush.horizontalGradient(
                listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
            )
    }
}
