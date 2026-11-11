package com.otusfoss.core.designsystem.shared.dates.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
class CalendarDayScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun plain() = compose.screenshot("calendar_day_plain") { Day("14", NONE) }

    @Test
    fun singleSelected() =
        compose.screenshot("calendar_day_selected") {
            Day("14", CalendarDaySelection(inRange = true, endpoint = true))
        }

    @Test
    fun range() =
        compose.screenshot("calendar_day_range") {
            Row {
                Day("12", NONE)
                Day("13", CalendarDaySelection(inRange = true, endpoint = true, connectsNext = true))
                Day("14", CalendarDaySelection(inRange = true, endpoint = false))
                Day("15", CalendarDaySelection(inRange = true, endpoint = false))
                Day("16", CalendarDaySelection(inRange = true, endpoint = true, connectsPrevious = true))
                Day("17", NONE)
            }
        }

    @Test
    fun monthLabel() =
        compose.screenshot("calendar_day_month_label") {
            Row {
                Day("1", NONE, monthLabel = "Nov")
                Day("1", CalendarDaySelection(inRange = true, endpoint = true), monthLabel = "Nov")
            }
        }

    @Composable
    private fun Day(
        label: String,
        selection: CalendarDaySelection,
        monthLabel: String? = null,
    ) = CalendarDay(label, "October $label", selection, {}, Modifier.width(48.dp), monthLabel = monthLabel)

    private companion object {
        val NONE = CalendarDaySelection(inRange = false, endpoint = false)
    }
}
