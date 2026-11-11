package com.otusfoss.core.designsystem.shared.dates.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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
class MonthNavigationScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val march = MonthNavigationLabel("March", "2026", chronologicalOrder = 2026 * 12 + 2)

    @Test
    fun bothArrows() =
        compose.screenshot("month_navigation_enabled") {
            Column(Modifier.width(344.dp)) { MonthNavigation(march, {}, {}, {}) }
        }

    @Test
    fun atRangeEnds() =
        compose.screenshot("month_navigation_range_ends") {
            Column(Modifier.width(344.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MonthNavigation(march, {}, {}, {}, nextEnabled = false)
                MonthNavigation(MonthNavigationLabel("January", "1970", 1970 * 12), {}, {}, {}, previousEnabled = false)
            }
        }
}
