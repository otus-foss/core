package com.otus.jscontact

import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NameFiguresTest {
    @Test
    fun phoneticNameFigureKeepsEachPronunciation() {
        val name = name(FIGURE_1)
        assertEquals(PhoneticSystem.Ipa, name.phoneticSystem)
        assertEquals(listOf("/ˈdʒɑːn/", "/smɪθ/"), name.components.orEmpty().map { it.phonetic })
    }

    @Test
    fun twoWordSurnameFigureStaysOneComponent() {
        val expected =
            Name(
                components =
                    listOf(
                        NameComponent("Vincent", NameComponentKind.Given),
                        NameComponent("van Gogh", NameComponentKind.Surname),
                    ),
                isOrdered = true,
            )
        assertEquals(expected, name(FIGURE_16))
    }

    @Test
    fun secondSurnameFigureKeepsComponentOrder() {
        assertEquals(
            listOf(NameComponentKind.Given, NameComponentKind.Surname, NameComponentKind.Surname2),
            name(FIGURE_17).components.orEmpty().map { it.kind },
        )
    }

    @Test
    fun fullNameFigureNeedsNoComponents() {
        val name = RoundTrip.assertRoundTrip(Name.serializer(), JsContactJson.parseToJsonElement("{$FIGURE_18}"))
        assertEquals(Name(full = "Mr. John Q. Public, Esq."), name)
    }

    @Test
    fun sortAsFigureKeysSortValuesByComponentKind() {
        assertEquals(
            mapOf(NameComponentKind.Surname to "Pau Shou Chang", NameComponentKind.Given to "Robert"),
            name(FIGURE_19).sortAs,
        )
    }

    @Test
    fun localizationFigureAddsPhoneticsToTheName() {
        val card = JsContactJson.parseToJsonElement(FIGURE_20).jsonObject
        val base = RoundTrip.assertRoundTrip(Name.serializer(), card.getValue("name"))
        assertEquals(listOf("孫", "中山", "文", "逸仙"), base.components.orEmpty().map { it.value })
        assertNull(base.phoneticSystem)
        val yue =
            card
                .getValue("localizations")
                .jsonObject
                .getValue("yue")
                .jsonObject
        val localized =
            JsContactJson.decodeFromJsonElement(Name.serializer(), JsContactPatch.apply(card, yue).getValue("name"))
        assertEquals(PhoneticSystem.Jyut, localized.phoneticSystem)
        assertEquals("Latn", localized.phoneticScript)
        assertEquals(
            listOf("syun1", "zung1saan1", "man4", "jat6sin1"),
            localized.components.orEmpty().map { it.phonetic },
        )
    }

    @Test
    fun vendorKindsAndPropertiesSurviveARoundTrip() {
        // The vendor kind is Figure 4 and the vendor properties are Figure 3 (RFC 9553, section 1.8).
        val component =
            RoundTrip.assertRoundTrip(
                NameComponent.serializer(),
                JsContactJson.parseToJsonElement(
                    """{"value": "Jo", "kind": "example.com:baz",
                        "example.com:foo": "bar", "example.com:foo2": {"bar": "baz"}}""",
                ),
            )
        assertEquals(NameComponentKind("example.com:baz"), component.kind)
        assertEquals(setOf("example.com:foo", "example.com:foo2"), component.extra.keys)
    }

    private fun name(figure: String): Name =
        RoundTrip.assertRoundTrip(Name.serializer(), RoundTrip.fragment(figure, "name"))
}

private const val FIGURE_1 = """
"name": {
  "components": [{
    "kind": "given",
    "value": "John",
    "phonetic": "/ˈdʒɑːn/"
  }, {
    "kind": "surname",
    "value": "Smith",
    "phonetic": "/smɪθ/"
  }],
  "phoneticSystem": "ipa"
}
"""

private const val FIGURE_16 = """
"name": {
  "components": [
    { "kind": "given", "value": "Vincent" },
    { "kind": "surname", "value": "van Gogh" }
  ],
  "isOrdered": true
}
"""

private const val FIGURE_17 = """
"name": {
  "components": [
    { "kind": "given", "value": "Diego" },
    { "kind": "surname", "value": "Rivera" },
    { "kind": "surname2", "value": "Barrientos" }
  ],
  "isOrdered": true
}
"""

private const val FIGURE_18 = """
"full": "Mr. John Q. Public, Esq."
"""

private const val FIGURE_19 = """
"name": {
  "components": [
    { "kind": "given", "value": "Robert" },
    { "kind": "given2", "value": "Pau" },
    { "kind": "surname", "value": "Shou Chang" }
  ],
  "sortAs": {
    "surname": "Pau Shou Chang",
    "given": "Robert"
  },
  "isOrdered": true
}
"""

private const val FIGURE_20 = """
{
  "@type": "Card",
  "language": "zh-Hant",
  "name": {
    "components": [
      { "kind": "surname", "value": "孫" },
      { "kind": "given", "value": "中山" },
      { "kind": "given2", "value": "文" },
      { "kind": "given2", "value": "逸仙" }
    ]
  },
  "localizations": {
    "yue": {
      "name/phoneticSystem": "jyut",
      "name/phoneticScript": "Latn",
      "name/components/0/phonetic": "syun1",
      "name/components/1/phonetic": "zung1saan1",
      "name/components/2/phonetic": "man4",
      "name/components/3/phonetic": "jat6sin1"
    }
  }
}
"""
