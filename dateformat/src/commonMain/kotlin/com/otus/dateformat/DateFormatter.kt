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
            createLocalizedDateFormatter(localeIdentifier)
        } else {
            null
        }

    public fun format(date: LocalDate): String {
        val day = date.day.toString().padStart(TWO_DIGITS, '0')
        val month = date.month.number.toString().padStart(TWO_DIGITS, '0')
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

    public fun formatIsoDate(date: String): String = format(LocalDate.parse(date))

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

internal expect fun createLocalizedDateFormatter(localeIdentifier: String): LocalizedDateFormatter

private const val TWO_DIGITS = 2
private const val FOUR_DIGITS = 4
