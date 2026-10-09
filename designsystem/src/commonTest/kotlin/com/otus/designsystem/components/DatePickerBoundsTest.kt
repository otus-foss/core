package com.otus.designsystem.components

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatePickerBoundsTest {
    @Test
    fun boundsWithinOneMonthExcludeDatesOnBothSides() {
        val bounds = DatePickerBounds(LocalDate(2020, 2, 15), LocalDate(2020, 2, 29))
        assertEquals(2..2, bounds.months(2020))
        assertEquals(15..29, bounds.days(2020, 2))
    }

    @Test
    fun interiorMonthsUseTheirActualLengthIncludingLeapYears() {
        val bounds = DatePickerBounds(LocalDate(2020, 1, 10), LocalDate(2021, 12, 20))
        assertEquals(29, bounds.days(2020, 2).last)
        assertEquals(28, bounds.days(2021, 2).last)
        assertEquals(30, bounds.days(2020, 4).last)
    }

    @Test
    fun aSingleDateLeavesOneMonthAndDayAvailable() {
        val date = LocalDate(2020, 2, 29)
        val bounds = DatePickerBounds(date, date)
        assertEquals(2..2, bounds.months(2020))
        assertEquals(29..29, bounds.days(2020, 2))
    }

    @Test
    fun reversedBoundsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            DatePickerBounds(LocalDate(2021, 1, 1), LocalDate(2020, 12, 31))
        }
    }
}
