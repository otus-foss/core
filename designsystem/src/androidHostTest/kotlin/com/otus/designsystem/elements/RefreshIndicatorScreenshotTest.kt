package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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
class RefreshIndicatorScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    /** Before 0.4 nothing draws; the arc grows to the threshold at 1, then overshoot turns it. */
    @Test
    fun pulling() =
        compose.screenshot("refresh_indicator_pulling") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.3f, 0.6f, 0.9f, 1f, 1.8f).forEach { RefreshIndicator({ it }, refreshing = false) }
            }
        }

    /** Tests stop infinite animations, so the spinner shows its first frame: a dot at the top. */
    @Test
    fun refreshing() =
        compose.screenshot("refresh_indicator_refreshing") {
            RefreshIndicator({ 1f }, refreshing = true)
        }
}
