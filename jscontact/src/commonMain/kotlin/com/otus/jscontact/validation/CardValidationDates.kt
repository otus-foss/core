package com.otus.jscontact.validation

import com.otus.jscontact.Anniversary
import com.otus.jscontact.AnniversaryDate
import com.otus.jscontact.PartialDate
import com.otus.jscontact.Timestamp

/** Validates an Anniversary, its date, and its place, which follows the Address rules (RFC 9553, section 2.8.1). */
internal fun ValidationScope.checkAnniversary(anniversary: Anniversary) {
    propertyNames(anniversary.extra, Anniversary.serializer())
    enumValue(anniversary.kind.value, RegisteredValues.anniversaryKinds, "kind")
    member("date", anniversary.date, ValidationScope::checkDate)
    member("place", anniversary.place, ValidationScope::checkAddress)
}

private fun ValidationScope.checkDate(date: AnniversaryDate) {
    when (date) {
        is PartialDate -> checkPartialDate(date)
        is Timestamp -> propertyNames(date.extra, Timestamp.serializer())
    }
}

/**
 * Validates a PartialDate: a year, a month in a year, a day in a month, or a whole date, with the month
 * from 1 to 12, a day that exists in that month, and a lowercase calendar scale (RFC 9553, section 2.8.1).
 */
private fun ValidationScope.checkPartialDate(date: PartialDate) {
    propertyNames(date.extra, PartialDate.serializer())
    ensure(date.year != null || date.month != null || date.day != null, "A PartialDate needs a date ($DATE_SECTION)")
    ensure(date.year == null || date.year in 0..MAX_UNSIGNED_INT, "year must be an UnsignedInt ($DATE_SECTION)", "year")
    ensure(date.month == null || date.month in 1..MONTHS, "month must be from 1 to 12 ($DATE_SECTION)", "month")
    ensure(
        date.month == null || date.year != null || date.day != null,
        "A month needs a year or a day ($DATE_SECTION)",
        "month",
    )
    ensure(date.day == null || date.month != null, "A day needs a month ($DATE_SECTION)", "day")
    ensure(
        date.day == null || date.day in 1..daysIn(date.month, date.year),
        "The day does not exist in its month ($DATE_SECTION)",
        "day",
    )
    ensure(
        date.calendarScale == null || date.calendarScale == date.calendarScale.lowercase(),
        "calendarScale must be lowercase ($DATE_SECTION)",
        "calendarScale",
    )
    enumValue(date.calendarScale, emptySet(), "calendarScale")
}

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

private const val DATE_SECTION = "RFC 9553, section 2.8.1"

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
