package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExpandChevronScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun collapsedAndExpanded() =
        compose.screenshot("expand_chevron") {
            Row {
                ExpandChevron(expanded = false)
                ExpandChevron(expanded = true)
            }
        }

    @Test
    fun rtl() =
        compose.screenshot("expand_chevron_rtl") {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row {
                    ExpandChevron(expanded = false)
                    ExpandChevron(expanded = true)
                }
            }
        }
}
