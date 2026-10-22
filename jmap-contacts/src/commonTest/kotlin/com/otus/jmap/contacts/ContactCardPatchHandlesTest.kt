package com.otus.jmap.contacts

import com.otus.jscontact.Card
import com.otus.jscontact.JsContactJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

/** Every handle of [ContactCardPatch] writes its Card property under the name and in the form a Card uses. */
class ContactCardPatchHandlesTest {
    private val card = JsContactJson.decodeFromString(Card.serializer(), FULL_CARD)

    @Test
    fun settingEveryHandleWritesTheWholeCardButItsServerSetId() {
        val patch =
            contactCardPatch {
                version.set(card.version)
                created.set(card.created!!)
                kind.set(card.kind!!)
                language.set(card.language!!)
                members.set(card.members!!.keys)
                prodId.set(card.prodId!!)
                relatedTo.set(card.relatedTo!!)
                uid.set(card.uid!!)
                updated.set(card.updated!!)
                name.set(card.name!!)
                nicknames.set(card.nicknames!!)
                organizations.set(card.organizations!!)
                speakToAs.set(card.speakToAs!!)
                titles.set(card.titles!!)
                emails.set(card.emails!!)
                onlineServices.set(card.onlineServices!!)
                phones.set(card.phones!!)
                preferredLanguages.set(card.preferredLanguages!!)
                calendars.set(card.calendars!!)
                schedulingAddresses.set(card.schedulingAddresses!!)
                addresses.set(card.addresses!!)
                cryptoKeys.set(card.cryptoKeys!!)
                directories.set(card.directories!!)
                links.set(card.links!!)
                media.set(card.media!!)
                localizations.set(card.localizations!!)
                anniversaries.set(card.anniversaries!!)
                keywords.set(card.keywords!!.keys)
                notes.set(card.notes!!)
                personalInfo.set(card.personalInfo!!)
                addressBookIds.set(card.addressBookIds!!.keys)
            }
        // The vendor property is not a Card property, so no handle writes it.
        assertEquals(JsonObject(card.json() - "@type" - "id" - VENDOR_PROPERTY), patch)
    }

    @Test
    fun anEntryIsWrittenAsItAppearsInTheWholeMap() {
        val patch =
            contactCardPatch {
                phones.put("tel0", card.phones!!.getValue("tel0"))
                anniversaries.put("k8", card.anniversaries!!.getValue("k8"))
            }
        assertEquals(
            card
                .json()
                .getValue("phones")
                .jsonObject
                .getValue("tel0"),
            patch["phones/tel0"],
        )
        assertEquals(
            card
                .json()
                .getValue("anniversaries")
                .jsonObject
                .getValue("k8"),
            patch["anniversaries/k8"],
        )
    }

    @Test
    fun aCreationIsTheCardWithoutItsServerSetId() {
        assertEquals(JsonObject(card.json() - "id"), card.toCreation())
        assertEquals(JsonPrimitive("kept as received"), card.toCreation()[VENDOR_PROPERTY])
    }

    private fun Card.json(): JsonObject = JsContactJson.encodeToJsonElement(Card.serializer(), this).jsonObject
}

private const val VENDOR_PROPERTY = "example.com:foo"

/** A ContactCard with every Card property, its values taken from the examples of RFC 9553, section 2. */
private const val FULL_CARD = """
{"@type": "Card", "version": "1.0", "id": "c1", "addressBookIds": {"b1": true},
 "created": "2022-09-30T14:35:10Z", "kind": "group", "language": "de-AT",
 "members": {"urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af": true},
 "prodId": "ACME Contacts App version 1.23.5",
 "relatedTo": {"urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6": {"relation": {"friend": true}}},
 "uid": "22B2C7DF-9120-4969-8460-05956FE6B065", "updated": "2021-10-31T22:27:10Z",
 "name": {"components": [{"kind": "given", "value": "Vincent"}, {"kind": "surname", "value": "van Gogh"}],
   "isOrdered": true},
 "nicknames": {"k391": {"name": "Johnny"}},
 "organizations": {"o1": {"name": "ABC, Inc.", "units": [{"name": "North American Division"}]}},
 "speakToAs": {"grammaticalGender": "neuter", "pronouns": {"k19": {"pronouns": "they/them", "pref": 2}}},
 "titles": {"le9": {"kind": "title", "name": "Research Scientist"}},
 "emails": {"e1": {"contexts": {"work": true}, "address": "jqpublic@xyz.example.com"}},
 "onlineServices": {"x1": {"uri": "xmpp:alice@example.com"}},
 "phones": {"tel0": {"contexts": {"private": true}, "features": {"voice": true},
   "number": "tel:+1-555-555-5555;ext=5555"}},
 "preferredLanguages": {"l1": {"language": "en", "contexts": {"work": true}, "pref": 1}},
 "calendars": {"calA": {"kind": "calendar", "uri": "webcal://calendar.example.com/calA.ics"}},
 "schedulingAddresses": {"sched1": {"uri": "mailto:janedoe@example.com"}},
 "addresses": {"k23": {"components": [{"kind": "number", "value": "54321"}, {"kind": "name", "value": "Oak St"}],
   "countryCode": "US"}},
 "cryptoKeys": {"mykey1": {"uri": "https://www.example.com/keys/jdoe.cer"}},
 "directories": {"dir1": {"kind": "entry", "uri": "https://dir.example.com/addrbook/jdoe/Jean%20Dupont.vcf"}},
 "links": {"link3": {"kind": "contact", "uri": "mailto:contact@example.com", "pref": 1}},
 "media": {"res45": {"kind": "photo", "blobId": "Gb5f55i6", "mediaType": "image/jpeg"}},
 "localizations": {"fr": {"titles/le9/name": "Chercheur scientifique"}},
 "anniversaries": {"k8": {"kind": "birth", "date": {"year": 1953, "month": 4, "day": 15}}},
 "keywords": {"internet": true},
 "notes": {"n1": {"note": "Open office hours are 1600 to 1715 EST, Mon-Fri", "created": "2022-11-23T15:01:32Z"}},
 "personalInfo": {"pi2": {"kind": "expertise", "value": "chemistry", "level": "high"}},
 "example.com:foo": "kept as received"}
"""
