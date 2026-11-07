package com.otus.json.types

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Instant

class UtcDateSerializerTest {
    // The UTCDate example of RFC 8620, section 1.4.
    private val example = Instant.parse("2014-10-30T06:12:00Z")

    @Test
    fun writesTheNormalizedUtcForm() {
        assertEquals("2014-10-30T06:12:00Z", encode(example))
        assertEquals("0999-01-01T00:00:00Z", encode(Instant.parse("0999-01-01T00:00:00Z")))
        assertEquals("2014-10-30T06:12:00.000000001Z", encode(example + 1.nanoseconds))
        assertEquals("2000-01-01T00:00:00Z", encode(Instant.parse("2000-01-01T00:00:00Z")))
    }

    @Test
    fun fractionsLoseTheirTrailingZeros() {
        assertEquals("2014-10-30T06:12:00.5Z", encode(example + 500.milliseconds))
        assertEquals("2014-10-30T06:12:00.12Z", encode(example + 120.milliseconds))
        assertEquals("2010-10-10T10:10:10.000001Z", encode(Instant.parse("2010-10-10T10:10:10.000001Z")))
        assertEquals("2010-10-10T10:10:10Z", encode(decode("2010-10-10T10:10:10.000Z")))
    }

    @Test
    fun readsAnyOffsetAsTheSameInstant() {
        assertEquals(example, decode("2014-10-30T06:12:00Z"))
        // The Date example of RFC 8620, section 1.4, names the same instant.
        assertEquals(example, decode("2014-10-30T14:12:00+08:00"))
        assertEquals("2010-10-10T08:10:10Z", encode(decode("2010-10-10T10:10:10+02:00")))
    }

    @Test
    fun aLeapSecondReadsAsTheLastNanosecondBeforeIt() {
        // RFC 3339, section 5.6, allows second 60; the last one was at the end of 2016.
        val last = Instant.parse("2016-12-31T23:59:59.999999999Z")
        listOf("2016-12-31T23:59:60Z", "2016-12-31T23:59:60.5Z", "2017-01-01T00:59:60+01:00").forEach { text ->
            assertEquals(last, decode(text), text)
        }
        assertEquals("2016-12-31T23:59:59.999999999Z", encode(decode("2016-12-31T23:59:60Z")))
    }

    @Test
    fun aSecondsFieldLongerThanTwoDigitsIsNoLeapSecond() {
        // time-second = 2DIGIT (RFC 3339, section 5.6): "600" is malformed, not second 60 plus a digit.
        listOf("2016-12-31T23:59:600Z", "2016-12-31T23:59:601Z", "2016-12-31T23:59:61Z").forEach { text ->
            assertFailsWith<SerializationException>(text) { decode(text) }
        }
    }

    @Test
    fun fractionDigitsAfterTheNinthAreDropped() {
        assertEquals(Instant.parse("2010-10-10T10:10:10.123456789Z"), decode("2010-10-10T10:10:10.1234567891Z"))
        assertEquals("2010-10-10T10:10:10Z", encode(decode("2010-10-10T10:10:10.0000000001Z")))
    }

    @Test
    fun textThatIsNoInstantIsRejected() {
        listOf("2014-10-30", "2014-10-30T06:12:00", "yesterday", "").forEach { text ->
            assertFailsWith<SerializationException>(text) { decode(text) }
        }
        assertFailsWith<SerializationException> { Json.decodeFromJsonElement(UtcDateSerializer, JsonPrimitive(5)) }
    }

    @Test
    fun yearsRfc3339CannotWriteAreRefused() {
        assertFailsWith<SerializationException> { encode(Instant.parse("+10000-01-01T00:00:00Z")) }
        assertFailsWith<SerializationException> { encode(Instant.parse("-0001-01-01T00:00:00Z")) }
        // Instant reads these, so without the check they would decode and then fail to encode.
        listOf(
            "+12020-01-31T23:59:59Z",
            "-0001-01-01T00:00:00Z",
            "-0000-01-01T00:00:00Z",
            "+00000-01-01T00:00:00Z",
            "9999-12-31T23:30:00-01:00",
            "0000-01-01T00:30:00+01:00",
        ).forEach { text ->
            assertFailsWith<SerializationException>(text) { decode(text) }
        }
    }

    @Test
    fun theFirstAndLastYearsRoundTrip() {
        listOf("0000-01-01T00:00:00Z", "9999-12-31T23:59:59.999999999Z").forEach { text ->
            assertEquals(text, encode(decode(text)))
        }
    }

    private fun encode(value: Instant): String = Json.encodeToJsonElement(UtcDateSerializer, value).toString().trim('"')

    private fun decode(text: String): Instant = Json.decodeFromJsonElement(UtcDateSerializer, JsonPrimitive(text))
}
