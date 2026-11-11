package com.otusfoss.core.designsystem.shared.dates.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CalendarMonthBackgroundScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun month() = compose.screenshot("calendar_month_background") { Month() }

    @Test
    fun fullWeeks() =
        compose.screenshot("calendar_month_background_full_weeks") {
            // February 2027 starts on a Monday and fills exactly four weeks.
            Month(firstWeekday = 0, days = 28)
        }

    @Test
    fun rtl() =
        compose.screenshot("calendar_month_background_rtl") {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { Month() }
        }

    /** October 2026 starts on a Thursday and spans five weeks. */
    @Composable
    private fun Month(
        firstWeekday: Int = 3,
        days: Int = 31,
    ) {
        val weeks = (firstWeekday + days + 6) / 7
        Column {
            repeat(weeks) { week ->
                Box(
                    Modifier
                        .size(336.dp, 40.dp)
                        .calendarMonthBackground(firstWeekday, days, week, OtusColors.bgRegular),
                )
            }
        }
    }
}
