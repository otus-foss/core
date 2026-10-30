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
 * of day after `T` ignored.
 */
private fun String.toAnniversaryDate(omittedYear: Long?): AnniversaryDate? =
    toTimestamp() ?: toPartialDate()?.let { date -> date.copy(year = date.year.takeUnless { it == omittedYear }) }

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
