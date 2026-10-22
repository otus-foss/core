package com.otus.jscontact

import com.otus.jscontact.RoundTrip.assertEntriesRoundTrip
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CardContactsTest {
    @Test
    fun emailsOfFigure25RoundTrip() {
        val (emails, encoded) = assertEntriesRoundTrip(EmailAddress.serializer(), FIGURE_25, "emails")
        assertEquals(
            mapOf(
                "e1" to EmailAddress("jqpublic@xyz.example.com", contexts = mapOf(ContactContext.Work to true)),
                "e2" to EmailAddress("jane_doe@example.com", pref = 1),
            ),
            emails,
        )
        assertEquals(JsonPrimitive("EmailAddress"), encoded.getValue("e1").jsonObject["@type"])
    }

    @Test
    fun onlineServicesOfFigure26RoundTrip() {
        val (services, encoded) = assertEntriesRoundTrip(OnlineService.serializer(), FIGURE_26, "onlineServices")
        assertEquals(
            mapOf(
                "x1" to OnlineService(uri = "xmpp:alice@example.com"),
                "x2" to
                    OnlineService(
                        service = "Mastodon",
                        uri = "https://example2.com/@alice",
                        user = "@alice@example2.com",
                    ),
            ),
            services,
        )
        assertEquals(JsonPrimitive("OnlineService"), encoded.getValue("x1").jsonObject["@type"])
    }

    @Test
    fun phonesOfFigure27RoundTrip() {
        val (phones, encoded) = assertEntriesRoundTrip(Phone.serializer(), FIGURE_27, "phones")
        assertEquals(
            Phone(
                number = "tel:+1-555-555-5555;ext=5555",
                features = mapOf(PhoneFeature.Voice to true),
                contexts = mapOf(ContactContext.Private to true),
                pref = 1,
            ),
            phones.getValue("tel0"),
        )
        assertEquals(Phone("tel:+1-201-555-0123", contexts = mapOf(ContactContext.Work to true)), phones["tel3"])
        assertEquals(JsonPrimitive("Phone"), encoded.getValue("tel0").jsonObject["@type"])
    }

    @Test
    fun preferredLanguagesOfFigure28RoundTrip() {
        val (languages, encoded) = assertEntriesRoundTrip(LanguagePref.serializer(), FIGURE_28, "preferredLanguages")
        val work = mapOf(ContactContext.Work to true)
        assertEquals(
            mapOf(
                "l1" to LanguagePref("en", work, pref = 1),
                "l2" to LanguagePref("fr", work, pref = 2),
                "l3" to LanguagePref("fr", mapOf(ContactContext.Private to true)),
            ),
            languages,
        )
        assertEquals(JsonPrimitive("LanguagePref"), encoded.getValue("l3").jsonObject["@type"])
    }

    @Test
    fun vendorFeaturesAndUnknownPropertiesRoundTrip() {
        val json =
            """{"number": "+1 555 0100", "features": {"example.com:satellite": true, "mobile": true},
               "example.com:carrier": {"name": "Acme"}, "futureProperty": 1}"""
        val phone = RoundTrip.assertRoundTrip(Phone.serializer(), JsContactJson.parseToJsonElement(json))
        assertEquals(mapOf(PhoneFeature("example.com:satellite") to true, PhoneFeature.Mobile to true), phone.features)
        assertEquals(setOf("example.com:carrier", "futureProperty"), phone.extra.keys)
    }

    @Test
    fun aMatchingTypeDecodesAndAnotherTypeFails() {
        assertEquals(
            EmailAddress("jane_doe@example.com"),
            JsContactJson.decodeFromString(
                EmailAddress.serializer(),
                """{"@type": "EmailAddress", "address": "jane_doe@example.com"}""",
            ),
        )
        assertFailsWith<SerializationException> {
            JsContactJson.decodeFromString(
                EmailAddress.serializer(),
                """{"@type": "Phone", "address": "jane_doe@example.com"}""",
            )
        }
    }
}

private const val FIGURE_25 = """
   "emails": {
     "e1": {
       "contexts": {
         "work": true
       },
       "address": "jqpublic@xyz.example.com"
     },
     "e2": {
       "address": "jane_doe@example.com",
       "pref": 1
     }
   }
"""

private const val FIGURE_26 = """
   "onlineServices": {
     "x1": {
       "uri": "xmpp:alice@example.com"
     },
     "x2": {
       "service": "Mastodon",
       "user": "@alice@example2.com",
       "uri": "https://example2.com/@alice"
     }
   }
"""

private const val FIGURE_27 = """
   "phones": {
     "tel0": {
       "contexts": {
         "private": true
       },
       "features": {
         "voice": true
       },
       "number": "tel:+1-555-555-5555;ext=5555",
       "pref": 1
     },
     "tel3": {
       "contexts": {
         "work": true
       },
       "number": "tel:+1-201-555-0123"
     }
   }
"""

private const val FIGURE_28 = """
   "preferredLanguages": {
     "l1": {
       "language": "en",
       "contexts": {
         "work": true
       },
       "pref": 1
     },
     "l2": {
       "language": "fr",
       "contexts": {
         "work": true
       },
       "pref": 2
     },
     "l3": {
       "language": "fr",
       "contexts": {
         "private": true
       }
     }
   }
"""
