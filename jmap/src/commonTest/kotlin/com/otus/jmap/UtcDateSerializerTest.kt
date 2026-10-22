package com.otus.jmap

import kotlinx.serialization.SerializationException
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
        assertEquals(JsonPrimitive("2014-10-30T06:12:00Z"), encode(example))
        assertEquals(JsonPrimitive("0999-01-01T00:00:00Z"), encode(Instant.parse("0999-01-01T00:00:00Z")))
        assertEquals(JsonPrimitive("2014-10-30T06:12:00.000000001Z"), encode(example + 1.nanoseconds))
    }

    @Test
    fun fractionsLoseTheirTrailingZeros() {
        // Instant.toString() would write ".500Z" and ".120Z". jscontact's UtcDateTimeSerializer trims them too.
        assertEquals(JsonPrimitive("2014-10-30T06:12:00.5Z"), encode(example + 500.milliseconds))
        assertEquals(JsonPrimitive("2014-10-30T06:12:00.12Z"), encode(example + 120.milliseconds))
    }

    @Test
    fun readsAnyOffsetAsTheSameInstant() {
        assertEquals(example, decode("2014-10-30T06:12:00Z"))
        assertEquals(example, decode("2014-10-30T06:12:00.000Z"))
        // The Date example of RFC 8620, section 1.4, names the same instant.
        assertEquals(example, decode("2014-10-30T14:12:00+08:00"))
    }

    @Test
    fun aLeapSecondReadsAsTheLastNanosecondBeforeIt() {
        // RFC 3339, section 5.6, allows second 60; the last one was at the end of 2016.
        val last = Instant.parse("2016-12-31T23:59:59.999999999Z")
        listOf("2016-12-31T23:59:60Z", "2016-12-31T23:59:60.5Z", "2017-01-01T00:59:60+01:00").forEach { text ->
            assertEquals(last, decode(text), text)
        }
        assertEquals(JsonPrimitive("2016-12-31T23:59:59.999999999Z"), encode(decode("2016-12-31T23:59:60Z")))
    }

    @Test
    fun fractionDigitsAfterTheNinthAreDropped() {
        assertEquals(Instant.parse("2010-10-10T10:10:10Z"), decode("2010-10-10T10:10:10.0000000001Z"))
        assertEquals(Instant.parse("2010-10-10T10:10:10.123456789Z"), decode("2010-10-10T10:10:10.1234567891Z"))
        assertEquals(JsonPrimitive("2010-10-10T10:10:10Z"), encode(decode("2010-10-10T10:10:10.0000000001Z")))
    }

    @Test
    fun textThatIsNoInstantIsRejected() {
        listOf("2014-10-30", "2014-10-30T06:12:00", "yesterday", "").forEach { text ->
            assertFailsWith<SerializationException>(text) { decode(text) }
        }
        assertFailsWith<SerializationException> {
            JmapJson.decodeFromJsonElement(UtcDateSerializer, JsonPrimitive(1414649520))
        }
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
            "9999-12-31T23:30:00-01:00",
        ).forEach { text ->
            assertFailsWith<SerializationException>(text) { decode(text) }
        }
    }

    private fun encode(value: Instant) = JmapJson.encodeToJsonElement(UtcDateSerializer, value)

    private fun decode(text: String) = JmapJson.decodeFromJsonElement(UtcDateSerializer, JsonPrimitive(text))
}
