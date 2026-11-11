package com.otusfoss.core.designsystem.shared.rows.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
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
class RowButtonScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun default() = compose.screenshot("row_button") { RowButton("Remove", {}) }

    @Test
    fun accessibilityLabelNamesButton() {
        var clicks = 0
        compose.setContent { RowButton("Remove", { clicks++ }, accessibilityLabel = "Remove Ada") }
        compose.onNodeWithContentDescription("Remove Ada").performClick()
        assertEquals(1, clicks)
    }
}
