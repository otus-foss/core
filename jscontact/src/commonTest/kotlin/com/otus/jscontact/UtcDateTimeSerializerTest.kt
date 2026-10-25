package com.otus.jscontact

import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class UtcDateTimeSerializerTest {
    @Test
    fun theRfcConformantExampleRoundTrips() {
        val instant = decode("2010-10-10T10:10:10.003Z")
        assertEquals(Instant.parse("2010-10-10T10:10:10.003Z"), instant)
        assertEquals("2010-10-10T10:10:10.003Z", encode(instant))
    }

    @Test
    fun theRfcInvalidZeroFractionReadsAndIsWrittenCorrectly() {
        assertEquals("2010-10-10T10:10:10Z", encode(decode("2010-10-10T10:10:10.000Z")))
    }

    @Test
    fun fractionsLoseTrailingZeros() {
        assertEquals("2010-10-10T10:10:10.12Z", encode(Instant.parse("2010-10-10T10:10:10.120Z")))
        assertEquals("2010-10-10T10:10:10.1Z", encode(Instant.parse("2010-10-10T10:10:10.1Z")))
        assertEquals("2010-10-10T10:10:10.000001Z", encode(Instant.parse("2010-10-10T10:10:10.000001Z")))
        assertEquals("2010-10-10T10:10:10.123456789Z", encode(Instant.parse("2010-10-10T10:10:10.123456789Z")))
    }

    @Test
    fun wholeSecondsKeepTheirZeros() {
        assertEquals("2010-10-10T10:10:00Z", encode(Instant.parse("2010-10-10T10:10:00Z")))
        assertEquals("2000-01-01T00:00:00Z", encode(Instant.parse("2000-01-01T00:00:00Z")))
    }

    @Test
    fun numericOffsetsAreReadAndWrittenAsUtc() {
        assertEquals("2010-10-10T08:10:10Z", encode(decode("2010-10-10T10:10:10+02:00")))
    }

    @Test
    fun unreadableTextFails() {
        val unreadable =
            listOf(
                "yesterday",
                "2010-10-10",
                "2010-10-10T10:10:10",
                "",
                "2016-12-31T23:59:61Z",
                "2016-12-31T23:59:600Z",
            )
        for (text in unreadable) {
            assertFailsWith<SerializationException>(text) { decode(text) }
        }
        assertFailsWith<SerializationException> { JsContactJson.decodeFromString(UtcDateTimeSerializer, "5") }
    }

    @Test
    fun aLeapSecondReadsAsTheLastNanosecondBeforeIt() {
        val lastNanosecond = Instant.parse("2016-12-31T23:59:59.999999999Z")
        assertEquals(lastNanosecond, decode("2016-12-31T23:59:60Z"))
        assertEquals(lastNanosecond, decode("2016-12-31T23:59:60.5Z"))
        assertEquals(lastNanosecond, decode("2017-01-01T00:59:60+01:00"))
        assertEquals("2016-12-31T23:59:59.999999999Z", encode(decode("2016-12-31T23:59:60Z")))
    }

    @Test
    fun fractionDigitsPastTheNanosecondAreDropped() {
        assertEquals("2010-10-10T10:10:10Z", encode(decode("2010-10-10T10:10:10.0000000001Z")))
        assertEquals(Instant.parse("2010-10-10T10:10:10.123456789Z"), decode("2010-10-10T10:10:10.1234567891Z"))
    }

    @Test
    fun yearsRfc3339CannotRepresentAreNotWritten() {
        assertFailsWith<SerializationException> { encode(Instant.parse("+12020-01-31T23:59:59Z")) }
        assertFailsWith<SerializationException> { encode(Instant.parse("-0001-01-31T23:59:59Z")) }
    }

    @Test
    fun yearsRfc3339CannotRepresentAreNotRead() {
        val outside =
            listOf(
                "+12020-01-31T23:59:59Z",
                "-0001-01-31T23:59:59Z",
                "-0000-01-01T00:00:00Z",
                "+00000-01-01T00:00:00Z",
                "9999-12-31T23:30:00-01:00",
                "0000-01-01T00:30:00+01:00",
            )
        for (text in outside) {
            assertFailsWith<SerializationException>(text) { decode(text) }
        }
    }

    @Test
    fun theFirstAndLastYearsRfc3339CanRepresentRoundTrip() {
        for (text in listOf("0000-01-01T00:00:00Z", "9999-12-31T23:59:59.999999999Z")) {
            assertEquals(text, encode(decode(text)))
        }
    }

    private fun decode(text: String): Instant = JsContactJson.decodeFromString(UtcDateTimeSerializer, "\"$text\"")

    private fun encode(instant: Instant): String =
        JsContactJson.encodeToString(UtcDateTimeSerializer, instant).removeSurrounding("\"")
}
