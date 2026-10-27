package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
class AnimatedTextScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun settled() = compose.screenshot("animated_text_settled") { AnimatedText("October 2026", order = 10) }

    @Test
    fun replaced() =
        compose.screenshot("animated_text_replaced") {
            var month by remember { mutableStateOf(OCTOBER) }
            LaunchedEffect(Unit) { month = NOVEMBER }
            AnimatedText(month.first, order = month.second)
        }

    private companion object {
        val OCTOBER = "October 2026" to 10
        val NOVEMBER = "November 2026" to 11
    }
}
