package com.otus.designsystem.components

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

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
        val firstOfMonth = LocalDate(year, month, 1)
        val lastOfMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val first = if (year == minDate.year && month == minDate.month.ordinal + 1) minDate.day else 1
        val last = if (year == maxDate.year && month == maxDate.month.ordinal + 1) maxDate.day else lastOfMonth.day
        return first..last
    }
}
