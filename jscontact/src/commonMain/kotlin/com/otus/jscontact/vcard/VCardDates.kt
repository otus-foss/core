package com.otus.jscontact.vcard

import com.otus.jscontact.Anniversary
import com.otus.jscontact.AnniversaryDate
import com.otus.jscontact.PartialDate
import com.otus.jscontact.Timestamp
import com.otus.jscontact.vcard.VCardKey.OMIT_YEAR
import kotlin.time.Instant

/**
 * The [Anniversary] of a BDAY, ANNIVERSARY, or DEATHDATE property, or null for another property or a date
 * this library cannot read, such as free text. A year that matches Apple's X-APPLE-OMIT-YEAR is dropped.
 */
internal fun VCardProperty.toAnniversary(): Anniversary? {
    val kind = ANNIVERSARY_PROPERTIES[name]
    val date = value.trim().toAnniversaryDate(omittedYear = single(OMIT_YEAR)?.toLongOrNull())
    return if (kind == null || date == null) null else Anniversary(kind, date)
}

/** This anniversary as a date property, or null when vCard has no property for its kind or no form for its date. */
internal fun Anniversary.toDateProperty(): VCardProperty? {
    val propertyName = ANNIVERSARY_PROPERTIES.keyOf(kind)
    val text = date.toVCardDate()
    return if (propertyName == null || text == null) null else VCardProperty(propertyName, text)
}

/**
 * A UTC date and time, such as `19961022T140000Z`, reads as a [Timestamp]. Dates read as a [PartialDate]:
 * `--MMDD`, `--MM-DD`, `YYYYMMDD`, `YYYY-MM-DD`, `YYYY-MM`, and `YYYY` (RFC 6350, section 4.3), with any time
 * of day after `T` ignored. A date whose month or day does not exist, such as `19900231`, is null, because
 * RFC 9553, section 2.8.1, allows no such PartialDate.
 */
private fun String.toAnniversaryDate(omittedYear: Long?): AnniversaryDate? =
    toTimestamp() ?: toPartialDate()?.withoutYear(omittedYear)?.takeIf { it.isCalendarDate }

/** This date without its year when that year is [omittedYear], the placeholder Apple writes for an unknown year. */
private fun PartialDate.withoutYear(omittedYear: Long?): PartialDate =
    copy(year = year.takeUnless { it == omittedYear })

private fun String.toTimestamp(): Timestamp? =
    TIMESTAMP
        .matchEntire(this)
        ?.groupValues
        ?.drop(1)
        ?.chunked(DATE_PARTS)
        ?.let { (date, time) -> Instant.parseOrNull("${date.joinToString("-")}T${time.joinToString(":")}Z") }
        ?.let { Timestamp(it) }

private fun String.toPartialDate(): PartialDate? {
    val date = substringBefore('T')
    return DATE_FORMS.firstNotNullOfOrNull { form ->
        form.pattern
            .matchEntire(date)
            ?.destructured
            ?.let(form.read)
    }
}

/** The vCard 4.0 form of this date: `YYYYMMDD`, `--MMDD`, `YYYY-MM`, `YYYY`, or `YYYYMMDDTHHMMSSZ`. */
private fun AnniversaryDate.toVCardDate(): String? =
    when (this) {
        is Timestamp -> toVCardTimestamp()
        is PartialDate -> toVCardPartialDate()
    }

private fun Timestamp.toVCardTimestamp(): String =
    utc
        .toString()
        .substringBefore('.')
        .removeSuffix("Z")
        .filterNot { it == '-' || it == ':' } + "Z"

private fun PartialDate.toVCardPartialDate(): String? {
    val yearText = year?.toString()?.padStart(YEAR_DIGITS, '0')
    val monthText = month?.toString()?.padStart(2, '0')
    val dayText = day?.toString()?.padStart(2, '0')
    return when {
        yearText != null && monthText != null && dayText != null -> "$yearText$monthText$dayText"
        monthText != null && dayText != null -> "--$monthText$dayText"
        yearText != null && monthText != null -> "$yearText-$monthText"
        else -> yearText
    }
}

/** A timestamp's groups: year, month, and day, then hour, minute, and second. */
private const val DATE_PARTS = 3

private val TIMESTAMP = Regex("""(\d{4})-?(\d{2})-?(\d{2})T(\d{2}):?(\d{2}):?(\d{2})Z""")

/** One written form of a date and how to read its groups. */
private class DateForm(
    val pattern: Regex,
    val read: (MatchResult.Destructured) -> PartialDate,
)

private val DATE_FORMS =
    listOf(
        DateForm(Regex("""--(\d{2})-?(\d{2})""")) { (month, day) ->
            PartialDate(month = month.toLong(), day = day.toLong())
        },
        DateForm(Regex("""(\d{4})-?(\d{2})-?(\d{2})""")) { (year, month, day) ->
            PartialDate(year = year.toLong(), month = month.toLong(), day = day.toLong())
        },
        DateForm(Regex("""(\d{4})(?:-(\d{2}))?""")) { (year, month) ->
            PartialDate(year = year.toLong(), month = month.toLongOrNull())
        },
    )

private const val YEAR_DIGITS = 4

/** Whether this date's month, if any, is from 1 to 12, and its day, if any, exists in that month. */
private val PartialDate.isCalendarDate: Boolean
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
