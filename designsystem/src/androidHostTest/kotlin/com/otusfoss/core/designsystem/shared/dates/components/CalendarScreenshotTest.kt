package com.otusfoss.core.designsystem.shared.dates.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.dateformat.DateFormatPreference
import com.otusfoss.core.dateformat.DateFormatter
import com.otusfoss.core.designsystem.screenshot.screenScreenshot
import com.otusfoss.core.designsystem.shared.pages.components.PageText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

/**
 * DateRangePicker reads today from the system clock and opens on today's month without a start,
 * so every shot passes a start date in a past month and none shows the unselected state.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CalendarScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val allowed = LocalDate(2020, 1, 1)..LocalDate(2024, 12, 31)

    @Test
    fun rangeStartOnly() = compose.screenScreenshot("date_range_picker_start") { Range("2024-03-12", end = null) }

    @Test
    fun rangeInOneMonth() = compose.screenScreenshot("date_range_picker_range") { Range("2024-03-04", "2024-03-15") }

    @Test
    fun rangeAcrossMonths() =
        compose.screenScreenshot("date_range_picker_across_months") { Range("2024-02-26", "2024-03-06") }

    @Test
    fun monthWheel() =
        compose.screenScreenshot("date_wheel_picker_month") {
            DateWheelPicker(YearMonth(2024, 3), initialDay = null, allowed, dayRequired = false, {}, { _, _ -> })
        }

    @Test
    fun dayWheelUnpicked() =
        compose.screenScreenshot("date_wheel_picker_day_required") {
            DateWheelPicker(YearMonth(2024, 3), initialDay = null, allowed, dayRequired = true, {}, { _, _ -> })
        }

    @Test
    fun dayWheelPicked() =
        compose.screenScreenshot("date_wheel_picker_day") {
            DateWheelPicker(YearMonth(2024, 3), initialDay = 12, allowed, dayRequired = true, {}, { _, _ -> })
        }

    @Composable
    private fun Range(
        start: String,
        end: String?,
    ) {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            PageText {
                DateRangePicker(start, end, DateFormatter(DateFormatPreference.YearMonthDayDashes)) { _, _ -> }
            }
        }
    }
}
