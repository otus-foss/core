package com.otus.jscontact

import com.otus.jscontact.RoundTrip.assertEntriesRoundTrip
import com.otus.jscontact.RoundTrip.decodeEntries
import com.otus.jscontact.RoundTrip.encodeEntries
import com.otus.jscontact.RoundTrip.fragment
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class CardAnniversariesTest {
    @Test
    fun anniversariesWithAPartialDateAndATimestampRoundTrip() {
        val (anniversaries, encoded) = assertEntriesRoundTrip(Anniversary.serializer(), FIGURE_41, "anniversaries")
        val birth = anniversaries.getValue("k8")
        assertEquals(AnniversaryKind.Birth, birth.kind)
        assertEquals(PartialDate(year = 1953, month = 4, day = 15), birth.date)
        assertNull(birth.place)
        val death = anniversaries.getValue("k9")
        assertEquals(AnniversaryKind.Death, death.kind)
        assertEquals(Timestamp(Instant.parse("2019-10-15T23:10:00Z")), death.date)
        assertEquals("4445 Tree Street\nNew England, ND 58647\nUSA", death.place?.full)
        assertEquals(JsonPrimitive("Timestamp"), dateOf(encoded, "k9")["@type"])
    }

    @Test
    fun aDateWithoutTypeIsAPartialDate() {
        val date = decodeDate("""{"month": 2, "day": 29}""")
        assertEquals(PartialDate(month = 2, day = 29), date)
    }

    @Test
    fun aPartialDateMayNameItsType() {
        val date = decodeDate("""{"@type": "PartialDate", "year": 2003, "calendarScale": "hebrew"}""")
        assertEquals(PartialDate(year = 2003, calendarScale = "hebrew"), date)
    }

    @Test
    fun aTimestampWithoutItsTypeReadsAsAPartialDate() {
        val date = assertIs<PartialDate>(decodeDate("""{"utc": "2019-10-15T23:10:00Z"}"""))
        assertNull(date.year)
        assertEquals(setOf("utc"), date.extra.keys)
    }

    @Test
    fun aTimestampKeepsFractionalSeconds() {
        val date = decodeDate("""{"@type": "Timestamp", "utc": "2010-10-10T10:10:10.003Z"}""")
        assertEquals(Timestamp(Instant.parse("2010-10-10T10:10:10.003Z")), date)
    }

    @Test
    fun anUnknownDateTypeIsRejected() {
        assertFailsWith<SerializationException> { decodeDate("""{"@type": "Date", "year": 2000}""") }
        val lowercase = """{"@type": "timestamp", "utc": "2000-01-01T00:00:00Z"}"""
        assertFailsWith<SerializationException> { decodeDate(lowercase) }
    }

    @Test
    fun aTimestampNeedsItsUtcValue() {
        assertFailsWith<SerializationException> { decodeDate("""{"@type": "Timestamp", "year": 2000}""") }
    }

    @Test
    fun encodingATimestampWritesItsType() {
        val wedding = Anniversary(AnniversaryKind.Wedding, Timestamp(Instant.parse("2001-06-30T14:00:00Z")))
        val date = dateOf(encodeEntries(Anniversary.serializer(), mapOf("w" to wedding)), "w")
        assertEquals(
            JsContactJson.parseToJsonElement("""{"@type": "Timestamp", "utc": "2001-06-30T14:00:00Z"}"""),
            date,
        )
    }

    @Test
    fun encodingAPartialDateWritesOnlyItsFields() {
        val birthday = Anniversary(AnniversaryKind.Birth, PartialDate(month = 4, day = 15))
        val encoded = encodeEntries(Anniversary.serializer(), mapOf("b" to birthday))
        assertEquals(
            JsContactJson.parseToJsonElement("""{"month": 4, "day": 15}"""),
            RoundTrip.withoutTypes(dateOf(encoded, "b")),
        )
        assertEquals(birthday, decodeEntries(Anniversary.serializer(), encoded).getValue("b"))
    }

    @Test
    fun anniversariesKeepVendorKinds() {
        val text = """"anniversaries": {"a": {"kind": "example.com:graduation", "date": {"year": 1975}}}"""
        val graduation = assertEntriesRoundTrip(Anniversary.serializer(), text, "anniversaries").first.getValue("a")
        assertEquals(AnniversaryKind("example.com:graduation"), graduation.kind)
    }

    private fun decodeDate(date: String): AnniversaryDate {
        val text = """"anniversaries": {"a": {"kind": "birth", "date": $date}}"""
        return decodeEntries(Anniversary.serializer(), fragment(text, "anniversaries")).getValue("a").date
    }

    private fun dateOf(
        encoded: JsonObject,
        id: String,
    ): JsonObject {
        val entry = encoded.getValue(id).jsonObject
        return entry.getValue("date").jsonObject
    }
}

// RFC 9553, Figure 41.
private const val FIGURE_41 = """
   "anniversaries": {
     "k8": {
       "kind": "birth",
       "date": {
         "year": 1953,
         "month": 4,
         "day": 15
       }
     },
     "k9": {
       "kind": "death",
       "date": {
         "@type": "Timestamp",
         "utc": "2019-10-15T23:10:00Z"
       },
       "place": {
         "full": "4445 Tree Street\nNew England, ND 58647\nUSA"
       }
     }
   }
"""
