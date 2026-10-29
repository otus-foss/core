package com.otus.designsystem.shared.dates.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

/** Valid wheel choices within the caller's inclusive date interval. */
internal class DatePickerBounds(
    private val minDate: LocalDate,
    private val maxDate: LocalDate,
) {
    init {
        require(minDate <= maxDate) { "minDate must not be after maxDate" }
    }

    fun months(year: Int): IntRange {
        val first = if (year == minDate.year) minDate.month.ordinal + 1 else 1
        val last = if (year == maxDate.year) maxDate.month.ordinal + 1 else Month.entries.size
        return first..last
    }

    fun days(
        year: Int,
        month: Int,
    ): IntRange {
        val shown = YearMonth(year, month)
        val first = if (shown == minDate.yearMonth) minDate.day else 1
        val last = if (shown == maxDate.yearMonth) maxDate.day else shown.numberOfDays
        return first..last
    }
}
