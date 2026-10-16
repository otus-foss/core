package com.otus.designsystem.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.screenshot.screenScreenshot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DatePickerScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val allowed = LocalDate(1970, 1, 1)..LocalDate(2026, 3, 15)

    @Test
    fun monthYearAnyDay() =
        compose.screenScreenshot("month_year_picker_any_day") {
            MonthYearPicker(YearMonth(2026, 3), allowed, {}, { _, _ -> })
        }

    @Test
    fun specificDateChosen() =
        compose.screenScreenshot("specific_date_picker_day") {
            SpecificDatePicker(YearMonth(2025, 11), allowed, {}, {}, initialDay = 14)
        }

    @Test
    fun specificDateUnchosen() =
        compose.screenScreenshot("specific_date_picker_pick") {
            SpecificDatePicker(YearMonth(2026, 3), allowed, {}, {})
        }
}
