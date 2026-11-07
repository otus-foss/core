package com.otus.dateformat

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.minus

/**
 * Dates with English day and month names, for labels that read better than numbers. The order of
 * day and month follows [DateFormatter.preference]: a day-first format, or a device locale that
 * writes the day first, gives "8 Oct"; the others give "Oct 8".
 */
public fun DateFormatter.formatNamedDay(
    date: LocalDate,
    withYear: Boolean = false,
): String {
    val month = date.month.shortName
    return when {
        dayFirst && withYear -> "${date.day} $month ${date.year}"
        dayFirst -> "${date.day} $month"
        withYear -> "$month ${date.day}, ${date.year}"
        else -> "$month ${date.day}"
    }
}

/** The weekday's short English name, such as "Mon". */
public fun shortWeekday(day: DayOfWeek): String =
    day.name
        .take(SHORT_NAME)
        .lowercase()
        .replaceFirstChar(Char::uppercase)

/** A time of day on the 24-hour clock, with the hour unpadded, such as "8:05" or "17:50". */
public fun clockTime(time: LocalTime): String = "${time.hour}:${time.minute.toString().padStart(2, '0')}"

/**
 * A recent day as mail lists name it: "Yesterday", the weekday for the five days before it, or null
 * for [today] and every other date.
 */
public fun DateFormatter.formatRecentDay(
    date: LocalDate,
    today: LocalDate,
): String? =
    when {
        date == today.minus(1, DateTimeUnit.DAY) -> YESTERDAY
        date > today.minus(DAYS_IN_WEEK, DateTimeUnit.DAY) && date < today -> shortWeekday(date.dayOfWeek)
        else -> null
    }

/** Whether [DateFormatter.formatNamedDay] puts the day before the month. */
private val DateFormatter.dayFirst: Boolean
    get() =
        when (preference) {
            DateFormatPreference.DayMonthYearDots, DateFormatPreference.DayMonthYearSlashes -> true
            DateFormatPreference.DeviceLocale -> formatDayMonth(DAY_FIRST_PROBE).startsWith(PROBE_DAY.toString())
            else -> false
        }

/** This month's English name, such as "January". */
public val Month.fullName: String
    get() = name.lowercase().replaceFirstChar(Char::uppercase)

private val Month.shortName: String
    get() = fullName.take(SHORT_NAME)

internal const val TODAY = "Today"
internal const val YESTERDAY = "Yesterday"
private const val SHORT_NAME = 3
private const val DAYS_IN_WEEK = 7

/** A day whose number no month has, so a day-month text shows which comes first. */
private const val PROBE_DAY = 31
private const val PROBE_YEAR = 2000
private val DAY_FIRST_PROBE = LocalDate(PROBE_YEAR, Month.DECEMBER, PROBE_DAY)
