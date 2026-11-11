package com.otusfoss.core.jscontact

import com.otusfoss.core.jscontact.RoundTrip.assertEntriesRoundTrip
import com.otusfoss.core.jscontact.RoundTrip.decodeEntries
import com.otusfoss.core.jscontact.RoundTrip.encodeEntries
import com.otusfoss.core.jscontact.RoundTrip.fragment
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class CardNotesTest {
    @Test
    fun notesWithAnAuthorRoundTrip() {
        val (notes, encoded) = assertEntriesRoundTrip(Note.serializer(), FIGURE_43, "notes")
        assertEquals(
            Note(
                "Open office hours are 1600 to 1715 EST, Mon-Fri",
                Instant.parse("2022-11-23T15:01:32Z"),
                Author(name = "John"),
            ),
            notes.getValue("n1"),
        )
        val author =
            encoded
                .getValue("n1")
                .jsonObject
                .getValue("author")
                .jsonObject
        assertEquals(JsonPrimitive("Author"), author["@type"])
    }

    @Test
    fun aNoteWritesItsCreationTimeInCanonicalForm() {
        val note =
            Note(
                note = "Call back",
                created = Instant.parse("2010-10-10T10:10:10.300Z"),
                author = Author(uri = "mailto:jane@example.com"),
            )
        assertEquals(
            JsContactJson.parseToJsonElement(
                """{"@type": "Note", "note": "Call back", "created": "2010-10-10T10:10:10.3Z",
                "author": {"@type": "Author", "uri": "mailto:jane@example.com"}}""",
            ),
            encodeEntries(Note.serializer(), mapOf("n" to note)).getValue("n"),
        )
    }

    @Test
    fun aNoteWithoutTextIsRejected() {
        val text = """"notes": {"n": {"created": "2022-11-23T15:01:32Z"}}"""
        assertFailsWith<SerializationException> { decodeEntries(Note.serializer(), fragment(text, "notes")) }
    }

    @Test
    fun personalInfoOfEveryKindRoundTrips() {
        val (info, _) = assertEntriesRoundTrip(PersonalInfo.serializer(), FIGURE_44, "personalInfo")
        assertEquals(
            PersonalInfo(PersonalInfoKind.Expertise, "chemistry", PersonalInfoLevel.High),
            info.getValue("pi2"),
        )
        assertEquals(PersonalInfo(PersonalInfoKind.Hobby, "reading", PersonalInfoLevel.High), info.getValue("pi1"))
        assertEquals(
            PersonalInfo(PersonalInfoKind.Interest, "r&b music", PersonalInfoLevel.Medium),
            info.getValue("pi6"),
        )
    }

    @Test
    fun personalInfoKeepsListAsLabelAndVendorValues() {
        val text = """"personalInfo": {"p": {"kind": "example.com:skill", "value": "Kotlin", "level": "low",
            "listAs": 3, "label": "Work", "example.com:years": 4}}"""
        val skill = assertEntriesRoundTrip(PersonalInfo.serializer(), text, "personalInfo").first.getValue("p")
        assertEquals(PersonalInfoKind("example.com:skill"), skill.kind)
        assertEquals(PersonalInfoLevel.Low, skill.level)
        assertEquals(3L, skill.listAs)
        assertEquals("Work", skill.label)
        assertEquals(setOf("example.com:years"), skill.extra.keys)
    }

    @Test
    fun personalInfoWithAnotherTypeIsRejected() {
        val text = """"personalInfo": {"p": {"@type": "Note", "kind": "hobby", "value": "chess"}}"""
        assertFailsWith<SerializationException> {
            decodeEntries(PersonalInfo.serializer(), fragment(text, "personalInfo"))
        }
    }
}

// RFC 9553, Figure 43.
private const val FIGURE_43 = """
   "notes": {
     "n1": {
       "note": "Open office hours are 1600 to 1715 EST, Mon-Fri",
       "created": "2022-11-23T15:01:32Z",
       "author": {
         "name": "John"
       }
     }
   }
"""

// RFC 9553, Figure 44.
private const val FIGURE_44 = """
   "personalInfo": {
     "pi2": {
       "kind": "expertise",
       "value": "chemistry",
       "level": "high"
     },
     "pi1": {
       "kind": "hobby",
       "value": "reading",
       "level": "high"
     },
     "pi6": {
       "kind": "interest",
       "value": "r&b music",
       "level": "medium"
     }
   }
"""
