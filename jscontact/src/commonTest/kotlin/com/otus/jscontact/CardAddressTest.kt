package com.otus.jscontact

import com.otus.jscontact.RoundTrip.assertEntriesRoundTrip
import com.otus.jscontact.RoundTrip.fragment
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class CardAddressTest {
    @Test
    fun theUsAddressOfFigure31RoundTrips() {
        val (addresses, encoded) = assertEntriesRoundTrip(Address.serializer(), FIGURE_31, "addresses")
        assertEquals(
            Address(
                contexts = mapOf(ContactContext.Work to true),
                components =
                    listOf(
                        part(AddressComponentKind.Number, "54321"),
                        part(AddressComponentKind.Separator, " "),
                        part(AddressComponentKind.Name, "Oak St"),
                        part(AddressComponentKind.Locality, "Reston"),
                        part(AddressComponentKind.Region, "VA"),
                        part(AddressComponentKind.Separator, " "),
                        part(AddressComponentKind.Postcode, "20190"),
                        part(AddressComponentKind.Country, "USA"),
                    ),
                countryCode = "US",
                defaultSeparator = ", ",
                isOrdered = true,
            ),
            addresses.getValue("k23"),
        )
        val address = encoded.getValue("k23").jsonObject
        assertEquals(JsonPrimitive("Address"), address["@type"])
        val component =
            address
                .getValue("components")
                .jsonArray
                .first()
                .jsonObject
        assertEquals(JsonPrimitive("AddressComponent"), component["@type"])
    }

    @Test
    fun theThaiAddressOfFigure32RoundTrips() {
        val address = assertEntriesRoundTrip(Address.serializer(), FIGURE_32, "addresses").first.getValue("k25")
        assertEquals(
            listOf(
                AddressComponentKind.Number,
                AddressComponentKind.Name,
                AddressComponentKind.Subdistrict,
                AddressComponentKind.District,
                AddressComponentKind.Locality,
                AddressComponentKind.Country,
                AddressComponentKind.Postcode,
            ),
            address.components?.map { it.kind },
        )
        assertEquals(" Watthana", address.components?.get(3)?.value)
        assertEquals(true, address.isOrdered)
    }

    @Test
    fun theTokyoAddressOfFigure33AndItsJapaneseLocalizationRoundTrip() {
        val address = assertEntriesRoundTrip(Address.serializer(), FIGURE_33, "addresses").first.getValue("k26")
        assertEquals("2-7-2 Marunouchi, Chiyoda-ku, Tokyo 100-8994", address.full)
        assertEquals(part(AddressComponentKind.Block, "2-7"), address.components?.first())
        val patch = fragment(FIGURE_33, "localizations").getValue("jp").jsonObject.getValue("addresses/k26")
        val localized = RoundTrip.assertRoundTrip(Address.serializer(), patch)
        assertEquals("", localized.defaultSeparator)
        assertEquals(part(AddressComponentKind.Postcode, "〒100-8994"), localized.components?.last())
    }

    @Test
    fun billingAndDeliveryContextsArePartOfAnAddress() {
        val address =
            JsContactJson.decodeFromString(
                Address.serializer(),
                """{"contexts": {"billing": true, "delivery": true}, "full": "54321 Oak St, Reston, VA 20190, USA"}""",
            )
        assertEquals(mapOf(ContactContext.Billing to true, ContactContext.Delivery to true), address.contexts)
    }

    @Test
    fun phoneticsAndUnknownComponentPropertiesRoundTrip() {
        val json =
            """{"components": [{"kind": "district", "value": "丸ノ内", "phonetic": "Marunouchi",
                 "example.com:verified": true}, {"kind": "example.com:ward", "value": "1"}],
               "isOrdered": false, "phoneticScript": "Latn", "phoneticSystem": "example.com:hepburn",
               "timeZone": "Asia/Tokyo", "coordinates": "geo:35.68,139.76", "futureProperty": {"a": [true]}}"""
        val address = RoundTrip.assertRoundTrip(Address.serializer(), JsContactJson.parseToJsonElement(json))
        assertEquals(PhoneticSystem("example.com:hepburn"), address.phoneticSystem)
        assertEquals(false, address.isOrdered)
        assertEquals(
            setOf("example.com:verified"),
            address.components
                ?.first()
                ?.extra
                ?.keys,
        )
        assertEquals(AddressComponentKind("example.com:ward"), address.components?.last()?.kind)
        assertEquals(setOf("futureProperty"), address.extra.keys)
    }
}

private fun part(
    kind: AddressComponentKind,
    value: String,
): AddressComponent = AddressComponent(value, kind)

private const val FIGURE_31 = """
   "addresses": {
     "k23": {
       "contexts": {
         "work": true
       },
       "components": [
         { "kind": "number", "value": "54321" },
         { "kind": "separator", "value": " " },
         { "kind": "name", "value": "Oak St" },
         { "kind": "locality", "value": "Reston" },
         { "kind": "region", "value": "VA" },
         { "kind": "separator", "value": " " },
         { "kind": "postcode", "value": "20190" },
         { "kind": "country", "value": "USA" }
       ],
       "countryCode": "US",
       "defaultSeparator": ", ",
       "isOrdered": true
     }
   }
"""

private const val FIGURE_32 = """
   "addresses": {
     "k25": {
       "components": [
         { "kind": "number", "value": "46" },
         { "kind": "name", "value": "1 Sukhumvit 51 Alley" },
         { "kind": "subdistrict", "value": "Khlong Tan Nuea" },
         { "kind": "district", "value": " Watthana" },
         { "kind": "locality", "value": "Bangkok" },
         { "kind": "country", "value": "Thailand" },
         { "kind": "postcode", "value": "10110" }
       ],
       "defaultSeparator": ", ",
       "isOrdered": true
     }
   }
"""

private const val FIGURE_33 = """
   "addresses": {
      "k26": {
        "components": [
          { "kind": "block", "value": "2-7" },
          { "kind": "separator", "value": "-" },
          { "kind": "number", "value": "2" },
          { "kind": "separator", "value": " " },
          { "kind": "district", "value": "Marunouchi" },
          { "kind": "locality", "value": "Chiyoda-ku" },
          { "kind": "region", "value": "Tokyo" },
          { "kind": "separator", "value": " " },
          { "kind": "postcode", "value": "100-8994" }
        ],
        "defaultSeparator": ", ",
        "full": "2-7-2 Marunouchi, Chiyoda-ku, Tokyo 100-8994",
        "isOrdered": true
      }
   },
   "localizations": {
     "jp": {
       "addresses/k26": {
         "components": [
           { "kind": "region", "value": "東京都" },
           { "kind": "locality", "value": "千代田区" },
           { "kind": "district", "value": "丸ノ内" },
           { "kind": "block", "value": "2-7" },
           { "kind": "separator", "value": "-" },
           { "kind": "number", "value": "2" },
           { "kind": "postcode", "value": "〒100-8994" }
         ],
         "defaultSeparator": "",
         "full": "〒100-8994東京都千代田区丸ノ内2-7-2",
         "isOrdered": true
       }
     }
   }
"""
