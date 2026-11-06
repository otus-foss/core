package com.otus.dateformat

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/** Formats display values only. Domain dates, sorting, and protocol values stay ISO. */
public class DateFormatter(
    public val preference: DateFormatPreference = DateFormatPreference.DeviceLocale,
    public val localeIdentifier: String = currentDateLocaleIdentifier(),
) {
    private val localized =
        if (preference == DateFormatPreference.DeviceLocale) {
            createLocalizedDateFormatter(localeIdentifier, FULL_DATE)
        } else {
            null
        }

    private val localizedDayMonth by lazy { createLocalizedDateFormatter(localeIdentifier, DAY_MONTH) }

    public fun format(date: LocalDate): String {
        val day = date.paddedDay
        val month = date.paddedMonth
        val year = date.year.toString().padStart(FOUR_DIGITS, '0')
        return when (preference) {
            DateFormatPreference.DeviceLocale -> requireNotNull(localized).format(date)
            DateFormatPreference.DayMonthYearDots -> "$day.$month.$year"
            DateFormatPreference.DayMonthYearSlashes -> "$day/$month/$year"
            DateFormatPreference.MonthDayYearDots -> "$month.$day.$year"
            DateFormatPreference.MonthDayYearSlashes -> "$month/$day/$year"
            DateFormatPreference.YearMonthDayDashes -> date.toString()
            DateFormatPreference.YearMonthDaySlashes -> "$year/$month/$day"
        }
    }

    /** [format] without the year, such as 09.03 or 03/09, for dates the reader knows the year of. */
    public fun formatDayMonth(date: LocalDate): String {
        val day = date.paddedDay
        val month = date.paddedMonth
        return when (preference) {
            DateFormatPreference.DeviceLocale -> localizedDayMonth.format(date)
            DateFormatPreference.DayMonthYearDots -> "$day.$month"
            DateFormatPreference.DayMonthYearSlashes -> "$day/$month"
            DateFormatPreference.MonthDayYearDots -> "$month.$day"
            DateFormatPreference.MonthDayYearSlashes -> "$month/$day"
            DateFormatPreference.YearMonthDayDashes -> "$month-$day"
            DateFormatPreference.YearMonthDaySlashes -> "$month/$day"
        }
    }

    public fun formatRange(
        start: LocalDate,
        end: LocalDate,
        separator: String = " → ",
    ): String = format(start) + separator + format(end)
}

internal fun interface LocalizedDateFormatter {
    fun format(date: LocalDate): String
}

public expect fun currentDateLocaleIdentifier(): String

/** A formatter for the device locale's own order of the fields in [template], such as "yyyyMMdd". */
internal expect fun createLocalizedDateFormatter(
    localeIdentifier: String,
    template: String,
): LocalizedDateFormatter

private val LocalDate.paddedDay: String
    get() = day.toString().padStart(TWO_DIGITS, '0')

private val LocalDate.paddedMonth: String
    get() = month.number.toString().padStart(TWO_DIGITS, '0')

private const val TWO_DIGITS = 2
private const val FOUR_DIGITS = 4
private const val FULL_DATE = "yyyyMMdd"
private const val DAY_MONTH = "MMdd"
