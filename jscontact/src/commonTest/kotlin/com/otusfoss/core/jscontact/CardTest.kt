package com.otusfoss.core.jscontact

import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class CardTest {
    @Test
    fun basicCardFigureRoundTrips() {
        val expected =
            Card(
                uid = "22B2C7DF-9120-4969-8460-05956FE6B065",
                kind = CardKind.Individual,
                name =
                    Name(
                        components =
                            listOf(
                                NameComponent("John", NameComponentKind.Given),
                                NameComponent("Doe", NameComponentKind.Surname),
                            ),
                        isOrdered = true,
                    ),
            )
        assertEquals(expected, RoundTrip.assertCardRoundTrip(CARD_FIGURE_6))
    }

    @Test
    fun typeAndVersionAreAlwaysWrittenFirst() {
        assertEquals(
            """{"@type":"Card","version":"1.0","uid":"u1"}""",
            JsContactJson.encodeToString(Card.serializer(), Card(uid = "u1")),
        )
    }

    @Test
    fun localizationFiguresRoundTripWithTheVersionAdded() {
        val version = mapOf("version" to JsonPrimitive("1.0"))
        val figure20 = RoundTrip.assertCardRoundTrip(CARD_FIGURE_20, version)
        assertEquals("zh-Hant", figure20.language)
        assertEquals(setOf("yue"), figure20.localizations?.keys)
        val figure39 = RoundTrip.assertCardRoundTrip(CARD_FIGURE_39, version)
        assertEquals(setOf("uk-Cyrl"), figure39.localizations?.keys)
        RoundTrip.assertCardRoundTrip(CARD_FIGURE_40, version)
    }

    @Test
    fun jmapExampleCardKeepsItsIdAndAddressBooks() {
        val card = RoundTrip.assertCardRoundTrip(CONTACT_CARD_EXAMPLE, mapOf("version" to JsonPrimitive("1.0")))
        assertEquals("3", card.id)
        assertEquals(mapOf("062adcfa-105d-455c-bc60-6db68b69c3f3" to true), card.addressBookIds)
        val email = card.emails?.get("0")
        assertEquals("joe.bloggs@example.com", email?.address)
        assertEquals(mapOf(ContactContext.Private to true), email?.contexts)
    }

    @Test
    fun aPartialCardFromAJmapGetDecodes() {
        val card = CardJson.decode("""{"id": "c1", "name": {"full": "Grace"}}""")
        assertEquals(Card(id = "c1", name = Name(full = "Grace")), card)
        assertNull(card.uid)
        assertEquals("1.0", card.version)
    }

    @Test
    fun theFiguresTogetherCoverEveryCardPropertyAndRoundTrip() {
        val card = RoundTrip.assertCardRoundTrip(CARD_OF_ALL_FIGURES)
        val rfc9553Properties =
            Card
                .serializer()
                .descriptor.elementNames
                .toSet() - setOf("id", "addressBookIds")
        assertEquals(rfc9553Properties, CardJson.parse(CARD_OF_ALL_FIGURES).keys - "@type")
        assertEquals(Instant.parse("2022-09-30T14:35:10Z"), card.created)
        assertEquals(CardKind.Group, card.kind)
        assertEquals(
            Timestamp(Instant.parse("2019-10-15T23:10:00Z")),
            card.anniversaries?.get("k9")?.date,
        )
        assertIs<PartialDate>(card.anniversaries?.get("k8")?.date)
        assertEquals("o2", card.titles?.get("k2")?.organizationId)
        assertEquals(CalendarKind.FreeBusy, card.calendars?.get("project-a")?.kind)
    }

    @Test
    fun unknownAndVendorPropertiesSurviveAtEveryLevel() {
        val card = RoundTrip.assertCardRoundTrip(CARD_WITH_EXTENSIONS)
        assertEquals(setOf("example.com:card", "futureCardProperty"), card.extra.keys)
        assertEquals(setOf("example.com:name"), card.name?.extra?.keys)
        assertEquals(
            setOf("example.com:part"),
            card.name
                ?.components
                ?.single()
                ?.extra
                ?.keys,
        )
        assertEquals(
            setOf("futureNickname"),
            card.nicknames
                ?.get("n")
                ?.extra
                ?.keys,
        )
        assertEquals(
            setOf("example.com:unit"),
            card.organizations
                ?.get("o")
                ?.units
                ?.single()
                ?.extra
                ?.keys,
        )
        assertEquals(setOf("example.com:date"), (card.anniversaries?.get("a1")?.date as? PartialDate)?.extra?.keys)
        assertEquals(setOf("example.com:utc"), (card.anniversaries?.get("a2")?.date as? Timestamp)?.extra?.keys)
        assertEquals(
            setOf("example.com:author"),
            card.notes
                ?.get("n")
                ?.author
                ?.extra
                ?.keys,
        )
    }

    @Test
    fun localizationsStayRawPatchObjects() {
        val card = CardJson.decode(CARD_FIGURE_20)
        val patch =
            CardJson
                .parse(CARD_FIGURE_20)
                .getValue("localizations")
                .jsonObject
                .getValue("yue")
        assertEquals<JsonElement?>(patch, card.localizations?.get("yue"))
    }
}

// Unknown and vendor-specific properties on every kind of JSContact object a Card holds.
private const val CARD_WITH_EXTENSIONS = """
{
  "@type": "Card", "version": "1.0", "uid": "u1",
  "example.com:card": {"nested": [1, {"deep": true}]},
  "futureCardProperty": "kept",
  "name": {
    "full": "Jane", "example.com:name": 1,
    "components": [{"kind": "given", "value": "Jane", "example.com:part": "x"}]
  },
  "relatedTo": {"u2": {"relation": {"friend": true}, "example.com:relation": 1}},
  "nicknames": {"n": {"name": "J", "futureNickname": "x"}},
  "organizations": {
    "o": {"name": "ACME", "units": [{"name": "R&D", "example.com:unit": 1}], "example.com:org": 2}
  },
  "speakToAs": {
    "grammaticalGender": "neuter", "example.com:speak": 1,
    "pronouns": {"p": {"pronouns": "they/them", "example.com:pronouns": 1}}
  },
  "titles": {"t": {"name": "Boss", "example.com:title": 1}},
  "emails": {"e": {"address": "jane@example.com", "example.com:email": 1}},
  "onlineServices": {"s": {"user": "jane", "example.com:service": 1}},
  "phones": {"p": {"number": "tel:+1-555-0100", "example.com:phone": 1}},
  "preferredLanguages": {"l": {"language": "en", "example.com:language": 1}},
  "calendars": {"c": {"kind": "calendar", "uri": "https://example.com/c", "example.com:calendar": 1}},
  "schedulingAddresses": {"s": {"uri": "mailto:jane@example.com", "example.com:scheduling": 1}},
  "addresses": {
    "a": {"full": "Main St", "example.com:address": 1, "components": [{"kind": "name", "value": "Main St", "x9": 1}]}
  },
  "cryptoKeys": {"k": {"uri": "https://example.com/k", "example.com:key": 1}},
  "directories": {"d": {"kind": "entry", "uri": "https://example.com/d", "example.com:directory": 1}},
  "links": {"l": {"uri": "https://example.com", "example.com:link": 1}},
  "media": {"m": {"kind": "photo", "blobId": "b1", "mediaType": "image/png", "example.com:media": 1}},
  "anniversaries": {
    "a1": {"kind": "birth", "date": {"year": 1990, "example.com:date": 1}, "example.com:anniversary": 1},
    "a2": {"kind": "death", "date": {"@type": "Timestamp", "utc": "2019-10-15T23:10:00Z", "example.com:utc": 1}}
  },
  "notes": {"n": {"note": "Hi", "author": {"name": "Ada", "example.com:author": 1}, "example.com:note": 1}},
  "personalInfo": {"p": {"kind": "hobby", "value": "chess", "example.com:info": 1}}
}
"""
