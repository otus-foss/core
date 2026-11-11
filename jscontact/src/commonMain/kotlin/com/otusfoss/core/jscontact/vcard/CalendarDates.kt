package com.otusfoss.core.jscontact.vcard

import com.otusfoss.core.jscontact.PartialDate

/** Whether this date's month, if any, is from 1 to 12, and its day, if any, exists in that month. */
internal val PartialDate.isCalendarDate: Boolean
    get() = (month == null || month in 1..MONTHS) && (day == null || day in 1..daysIn(month, year))

/** The days of [month] in [year], with February 29 when the year is unknown or the month is. */
private fun daysIn(
    month: Long?,
    year: Long?,
): Long =
    when (month) {
        FEBRUARY -> if (year == null || isLeapYear(year)) LEAP_FEBRUARY_DAYS else FEBRUARY_DAYS
        APRIL, JUNE, SEPTEMBER, NOVEMBER -> SHORT_MONTH_DAYS
        else -> LONG_MONTH_DAYS
    }

// The Gregorian rule, which applies whatever calendarScale says (RFC 9553, section 2.8.1).
private fun isLeapYear(year: Long): Boolean =
    year % LEAP_CYCLE == 0L && (year % CENTURY != 0L || year % LEAP_CENTURY == 0L)

private const val MONTHS = 12L
private const val FEBRUARY = 2L
private const val APRIL = 4L
private const val JUNE = 6L
private const val SEPTEMBER = 9L
private const val NOVEMBER = 11L
private const val FEBRUARY_DAYS = 28L
private const val LEAP_FEBRUARY_DAYS = 29L
private const val SHORT_MONTH_DAYS = 30L
private const val LONG_MONTH_DAYS = 31L
private const val LEAP_CYCLE = 4L
private const val CENTURY = 100L
private const val LEAP_CENTURY = 400L
