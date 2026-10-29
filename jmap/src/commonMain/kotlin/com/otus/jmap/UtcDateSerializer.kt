package com.otus.jmap

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * A UTCDate (RFC 8620, section 1.4) as an [Instant]. Writing gives one normalized form, such as
 * `2014-10-30T06:12:00Z` or `2014-10-30T06:12:00.5Z`: uppercase letters, a `Z` offset, and a
 * fraction of a second only when it is not zero, without trailing zeros. jscontact writes a
 * UTCDateTime (RFC 9553, section 1.4.5) the same way, so one instant has one text in every module.
 *
 * Reading also accepts other RFC 3339 offsets, since they name the same instant. RFC 3339 allows a
 * leap second and a fraction of any length (section 5.6), which [Instant] cannot hold, so a `:60`
 * second reads as the last nanosecond of second 59 and fraction digits after the ninth are dropped.
 * Both directions refuse years outside 0000 to 9999, which RFC 3339 cannot write, so every date
 * that reads can be written back. Data types and filters of other modules use it for their UTCDates.
 */
public object UtcDateSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.otus.jmap.UtcDate", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeString(value.toUtcDate() ?: throw SerializationException("$value has no RFC 3339 year"))
    }

    override fun deserialize(decoder: Decoder): Instant {
        val text = decoder.decodeString()
        // RFC 3339 writes a year as four digits, so a signed year such as "-0000" is not a date.
        val unsignedYear = UNSIGNED_YEAR.containsMatchIn(text)
        val instant = if (unsignedYear) Instant.parseOrNull(text.withinInstantPrecision()) else null
        return instant?.takeIf { it.toUtcDate() != null } ?: throw SerializationException("not a UTCDate: $text")
    }
}

/** The normalized UTCDate, or null when the year is outside 0000 to 9999. */
private fun Instant.toUtcDate(): String? {
    // Instant.toString() pads a fraction to groups of three digits, such as ".500Z", and signs
    // years outside 0000 to 9999, such as "+10000".
    val text = toString()
    val trimmed = if ('.' in text) text.dropLast(1).trimEnd('0') + "Z" else text
    return trimmed.takeIf { UTC_DATE.matches(it) }
}

/** RFC 3339 text that [Instant.parseOrNull] can read: no leap second and at most nine fraction digits. */
private fun String.withinInstantPrecision(): String {
    val withoutLeapSecond = LEAP_SECOND.replace(this) { it.groupValues[1] + "59.999999999" }
    return LONG_FRACTION.replace(withoutLeapSecond) { it.groupValues[1] }
}

private val UNSIGNED_YEAR = Regex("""^\d{4}-""")

private val UTC_DATE = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d*[1-9])?Z""")

// Matched by position, not only at 23:59 UTC: with a numeric offset, a leap second can fall at
// another local time, such as 00:59:60+01:00.
private val LEAP_SECOND = Regex("""([Tt]\d{2}:\d{2}:)60(\.\d+)?""")

private val LONG_FRACTION = Regex("""(\.\d{9})\d+""")
