package com.otus.json.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * A UTCDate (RFC 8620, section 1.4) or UTCDateTime (RFC 9553, section 1.4.5) as an [Instant]. Both
 * are RFC 3339 date-times with the offset `Z`. Writing gives one normalized form, such as
 * `2014-10-30T06:12:00Z` or `2014-10-30T06:12:00.5Z`: a fraction of a second only when it is not
 * zero, without trailing zeros, so one instant has one text in every module.
 *
 * Reading also accepts other offsets and zero fractions, since they name the same instant, so one
 * non-canonical date from a server does not make a whole object unreadable. RFC 3339 allows a leap
 * second and a fraction of any length (section 5.6), which [Instant] cannot hold: a `:60` second
 * reads as the last nanosecond of second 59, and fraction digits after the ninth are dropped. Both
 * directions refuse years outside 0000 to 9999, which RFC 3339 cannot write, so every date that
 * reads can be written back.
 */
public object UtcDateSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.otus.json.types.UtcDate", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        val instant = value.inRfc3339Years() ?: throw SerializationException("$value has no RFC 3339 year")
        encoder.encodeString(instant.toUtcDate())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val text = decoder.decodeString()
        return text.toUtcDateOrNull() ?: throw SerializationException("not an RFC 3339 date-time: $text")
    }
}

/**
 * Reads this RFC 3339 date-time as [UtcDateSerializer] does, leap second included; null when it is
 * not one or its year is not 0000 to 9999.
 */
public fun String.toUtcDateOrNull(): Instant? =
    takeIf { UNSIGNED_YEAR.containsMatchIn(it) }
        ?.replace(LEAP_SECOND) { it.groupValues[1] + LAST_NANOSECOND }
        ?.replace(LONG_FRACTION) { it.groupValues[1] }
        ?.let { Instant.parseOrNull(it) }
        ?.inRfc3339Years()

/**
 * This instant, or null outside the years 0000 to 9999. Both directions check it, because a numeric
 * offset can move a date written with year 9999 into year 10000.
 */
private fun Instant.inRfc3339Years(): Instant? = takeIf { it in RFC_3339_YEARS }

/** This instant as [UtcDateSerializer] writes it; its year must be 0000 to 9999. */
public fun Instant.toUtcDate(): String {
    // Instant.toString() pads a fraction to groups of three digits, such as ".500Z".
    val text = toString()
    return if ('.' in text) text.dropLast(1).trimEnd('0') + "Z" else text
}

// date-fullyear = 4DIGIT (RFC 3339, section 5.6). Instant also reads a sign and longer years, and
// "-0000" would even land inside the range below.
private val UNSIGNED_YEAR = Regex("""^\d{4}-""")

private val RFC_3339_YEARS: ClosedRange<Instant> =
    Instant.parse("0000-01-01T00:00:00Z")..Instant.parse("9999-12-31T23:59:59.999999999Z")

// A seconds field of exactly 60 and any fraction it has, found by its position after the hour and
// minute: a numeric offset puts a leap second at another local time, such as 15:59:60-08:00 (RFC
// 3339, section 5.8). The lookahead keeps a malformed "600" from reading as a leap second.
private val LEAP_SECOND = Regex("""([Tt]\d{2}:\d{2}:)60(?:\.\d+)?(?!\d)""")

private const val LAST_NANOSECOND = "59.999999999"

// The first nine fraction digits and the ones after them (time-secfrac = "." 1*DIGIT).
private val LONG_FRACTION = Regex("""(\.\d{9})\d+""")
