package com.otus.jscontact

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * A UTCDateTime (RFC 9553, section 1.4.5): an RFC 3339 date-time with the offset `Z`, and with
 * fractional seconds only when they are not zero, written without trailing zeros.
 *
 * Encoding writes that single canonical form. Decoding also accepts numeric offsets and zero fractions,
 * such as `2010-10-10T10:10:10.000Z`, so one non-canonical date from a server does not make the whole
 * card unreadable. Both directions accept only the years 0000 to 9999, because RFC 3339 writes a year
 * as four digits without a sign (section 5.6), so every date that decodes also encodes again.
 *
 * RFC 3339 allows two forms that [Instant] cannot parse, and decoding reads both. A leap second, `:60`,
 * reads as the last nanosecond before it, because [Instant] counts no leap seconds. A fraction longer
 * than nine digits loses the digits past the nanosecond, the finest unit [Instant] holds.
 */
internal object UtcDateTimeSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.otus.jscontact.UtcDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val text = decoder.decodeString()
        return text.toInstantOrNull()
            ?: throw SerializationException("Not an RFC 3339 date-time from the years 0000 to 9999: $text")
    }

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        val instant =
            value.inRfc3339Years() ?: throw SerializationException("$value is outside the years RFC 3339 can represent")
        encoder.encodeString(instant.toUtcDateTime())
    }
}

/** Parses this RFC 3339 date-time, or returns null when it is not one or its year is not 0000 to 9999. */
private fun String.toInstantOrNull(): Instant? =
    takeIf { UNSIGNED_YEAR.containsMatchIn(it) }
        ?.replace(LEAP_SECOND) { it.groupValues[1] + LAST_NANOSECOND }
        ?.replace(LONG_FRACTION) { it.groupValues[1] }
        ?.let { Instant.parseOrNull(it) }
        ?.inRfc3339Years()

/**
 * This instant, or null when it falls outside the years 0000 to 9999. Encoding and decoding share this check,
 * because a numeric offset can move a date that is written with year 9999 into year 10000.
 */
private fun Instant.inRfc3339Years(): Instant? = takeIf { it in RFC_3339_YEARS }

private fun Instant.toUtcDateTime(): String {
    // Instant.toString() pads fractions to groups of three digits, such as ".120Z".
    val text = toString()
    return if ('.' in text) text.dropLast(1).trimEnd('0') + "Z" else text
}

// date-fullyear = 4DIGIT (RFC 3339, section 5.6). Instant also reads a sign and longer years, which RFC 3339
// does not allow, and "-0000" would even land inside the range below.
private val UNSIGNED_YEAR = Regex("""^\d{4}-""")

private val RFC_3339_YEARS: ClosedRange<Instant> =
    Instant.parse("0000-01-01T00:00:00Z")..Instant.parse("9999-12-31T23:59:59.999999999Z")

// A seconds field of 60 and any fraction it has. It is found by its position after the hour and minute,
// because a numeric offset puts a leap second at another local time, such as 15:59:60-08:00 (RFC 3339,
// section 5.8).
private val LEAP_SECOND = Regex("""([Tt]\d{2}:\d{2}:)60(?:\.\d+)?(?!\d)""")

private const val LAST_NANOSECOND = "59.999999999"

// The first nine fraction digits and the ones after them (RFC 3339, section 5.6: time-secfrac = "." 1*DIGIT).
private val LONG_FRACTION = Regex("""(\.\d{9})\d+""")
