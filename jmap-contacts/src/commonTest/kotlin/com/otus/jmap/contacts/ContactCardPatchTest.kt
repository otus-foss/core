package com.otus.jmap.contacts

import com.otus.jscontact.CardKind
import com.otus.jscontact.ContactContext
import com.otus.jscontact.EmailAddress
import com.otus.jscontact.Media
import com.otus.jscontact.MediaKind
import com.otus.jscontact.Name
import com.otus.jscontact.NameComponent
import com.otus.jscontact.NameComponentKind
import com.otus.jscontact.Relation
import com.otus.jscontact.RelationType
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class ContactCardPatchTest {
    @Test
    fun wholePropertiesAreSetAsACardWritesThemOrRemoved() {
        val joe = Name(listOf(NameComponent("Joe", NameComponentKind.Given)), isOrdered = true)
        val patch =
            contactCardPatch {
                name.set(joe)
                kind.set(CardKind.Group)
                updated.set(Instant.parse("2024-12-01T10:15:30.500Z"))
                nicknames.remove()
                speakToAs.remove()
            }
        assertEquals(
            parseObject(
                """{"name": {"@type": "Name", "components": [{"@type": "NameComponent", "value": "Joe",
                "kind": "given"}], "isOrdered": true}, "kind": "group", "updated": "2024-12-01T10:15:30.5Z",
                "nicknames": null, "speakToAs": null}""",
            ),
            patch,
        )
    }

    @Test
    fun mapEntriesChangeOneAtATime() {
        // RFC 8620, section 5.3: a path into an Id map leaves its other entries, unknown ones included.
        val work = EmailAddress("joe@work.example.com", contexts = mapOf(ContactContext.Work to true))
        val patch =
            contactCardPatch {
                emails.put("e2", work)
                phones.remove("p1")
                media.put("m1", Media(MediaKind.Photo, blobId = "Gb5f55i6", mediaType = "image/png"))
                localizations.put("de", buildJsonObject { put("titles/t1/name", JsonPrimitive("Leiterin")) })
            }
        assertEquals(
            parseObject(
                """{"emails/e2": {"@type": "EmailAddress", "address": "joe@work.example.com",
                "contexts": {"work": true}}, "phones/p1": null,
                "media/m1": {"@type": "Media", "kind": "photo", "blobId": "Gb5f55i6", "mediaType": "image/png"},
                "localizations/de": {"titles/t1/name": "Leiterin"}}""",
            ),
            patch,
        )
    }

    @Test
    fun movingACardAddsTheNewBookAndRemovesTheOldOneInOnePatch() {
        // RFC 9610, section 3: a card belongs to at least one AddressBook at all times.
        val patch =
            contactCardPatch {
                addressBookIds.add(AUTOSAVED)
                addressBookIds.remove(PERSONAL)
            }
        assertEquals(parseObject("""{"addressBookIds/$AUTOSAVED": true, "addressBookIds/$PERSONAL": null}"""), patch)
    }

    @Test
    fun setsAreWrittenWithTrueValues() {
        val patch =
            contactCardPatch {
                members.set(setOf("urn:uuid:a", "urn:uuid:b"))
                keywords.add("internet")
                addressBookIds.set(setOf(PERSONAL))
            }
        assertEquals(
            parseObject(
                """{"members": {"urn:uuid:a": true, "urn:uuid:b": true}, "keywords/internet": true,
                "addressBookIds": {"$PERSONAL": true}}""",
            ),
            patch,
        )
    }

    @Test
    fun freeFormKeysAreEscapedAsJsonPointerSegments() {
        // RFC 6901, section 3: "~" becomes "~0" and "/" becomes "~1" in each segment.
        val patch =
            contactCardPatch {
                relatedTo.put("https://example.com/a/b~c", Relation(mapOf(RelationType.Friend to true)))
                keywords.remove("work/home")
            }
        assertEquals(
            parseObject(
                """{"relatedTo/https:~1~1example.com~1a~1b~0c": {"@type": "Relation", "relation": {"friend": true}},
                "keywords/work~1home": null}""",
            ),
            patch,
        )
    }

    @Test
    fun rawPathsReachInsideAnEntry() {
        val patch =
            contactCardPatch {
                raw { set(JsonPrimitive("joe@example.com"), "emails", "e1", "address") }
                raw { remove("emails", "e1", "label") }
                prodId.set("Otus")
            }
        assertEquals(
            parseObject("""{"emails/e1/address": "joe@example.com", "emails/e1/label": null, "prodId": "Otus"}"""),
            patch,
        )
    }

    @Test
    fun overlappingPathsFailBeforeSending() {
        // RFC 8620, section 5.3: one path may not be a prefix of another in the same patch.
        assertFailsWith<IllegalArgumentException> {
            contactCardPatch {
                emails.set(emptyMap())
                emails.put("e1", EmailAddress("joe@example.com"))
            }
        }
        assertFailsWith<IllegalArgumentException> {
            contactCardPatch {
                emails.put("e1", EmailAddress("joe@example.com"))
                raw { set(JsonPrimitive("Joe"), "emails", "e1", "label") }
            }
        }
    }

    @Test
    fun aLaterValueForTheSamePathReplacesTheEarlierOne() {
        val patch =
            contactCardPatch {
                kind.set(CardKind.Org)
                kind.remove()
            }
        assertEquals(parseObject("""{"kind": null}"""), patch)
    }
}
