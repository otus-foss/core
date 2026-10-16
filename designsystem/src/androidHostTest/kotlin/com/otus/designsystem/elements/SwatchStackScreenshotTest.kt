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
class SwatchStackScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun several() =
        compose.screenshot("swatch_stack") {
            SwatchStack(listOf(OtusColors.accentLight, OtusColors.accentRegular, Color(0xFFF2994A), OtusColors.bgDark))
        }

    @Test
    fun single() = compose.screenshot("swatch_stack_single") { SwatchStack(listOf(OtusColors.accentRegular)) }
}
