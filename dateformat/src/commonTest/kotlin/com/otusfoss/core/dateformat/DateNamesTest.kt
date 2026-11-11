package com.otusfoss.core.dateformat

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateNamesTest {
    private val date = LocalDate(2026, 10, 8)

    @Test
    fun theDateFormatChoosesWhetherTheDayOrTheMonthComesFirst() {
        assertEquals("8 Oct", DateFormatter(DateFormatPreference.DayMonthYearDots).formatNamedDay(date))
        assertEquals("8 Oct 2026", DateFormatter(DateFormatPreference.DayMonthYearSlashes).formatNamedDay(date, true))
        assertEquals("Oct 8", DateFormatter(DateFormatPreference.MonthDayYearSlashes).formatNamedDay(date))
        assertEquals("Oct 8, 2026", DateFormatter(DateFormatPreference.YearMonthDayDashes).formatNamedDay(date, true))
    }

    @Test
    fun weekdaysAndTimesAreShort() {
        assertEquals("Thu", shortWeekday(date.dayOfWeek))
        assertEquals("8:05", clockTime(LocalTime(8, 5)))
        assertEquals("17:50", clockTime(LocalTime(17, 50)))
    }

    @Test
    fun recentDaysAreYesterdayThenTheWeekday() {
        val formatter = DateFormatter(DateFormatPreference.DayMonthYearDots)
        assertEquals("Yesterday", formatter.formatRecentDay(LocalDate(2026, 10, 7), date))
        assertEquals("Fri", formatter.formatRecentDay(LocalDate(2026, 10, 2), date))
        assertNull(formatter.formatRecentDay(LocalDate(2026, 10, 1), date))
        assertNull(formatter.formatRecentDay(date, date))
    }
}
