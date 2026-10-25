package com.otus.jscontact

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class CardLocalizationTest {
    @Test
    fun topLevelLocalizationReplacesTheWholeName() {
        val localized = CardJson.decode(CARD_FIGURE_39).localized("uk-Cyrl")
        val expected =
            Name(
                components =
                    listOf(
                        NameComponent("г-н", NameComponentKind.Title),
                        NameComponent("Иван", NameComponentKind.Given),
                        NameComponent("Петрович", NameComponentKind.Given2),
                        NameComponent("Васильев", NameComponentKind.Surname),
                    ),
            )
        assertEquals(Card(language = "uk-Cyrl", name = expected), localized)
        assertNull(localized.localizations)
    }

    @Test
    fun nestedLocalizationPatchesOnlyThatValue() {
        val card = CardJson.decode(CARD_FIGURE_40)
        val localized = card.localized("es")
        assertEquals(mapOf("t1" to Title("escritor", TitleKind.Title)), localized.titles)
        assertEquals(card.name, localized.name)
        assertEquals("es", localized.language)
    }

    @Test
    fun phoneticLocalizationAddsPronunciations() {
        val name = CardJson.decode(CARD_FIGURE_20).localized("yue").name
        assertEquals(PhoneticSystem.Jyut, name?.phoneticSystem)
        assertEquals("Latn", name?.phoneticScript)
        assertEquals(
            listOf("syun1", "zung1saan1", "man4", "jat6sin1"),
            name?.components?.map { it.phonetic },
        )
        assertEquals(listOf("孫", "中山", "文", "逸仙"), name?.components?.map { it.value })
    }

    @Test
    fun languageTagsMatchIgnoringCaseButAnExactTagWins() {
        val card = CardJson.decode(CARD_FIGURE_39)
        assertEquals(card.localized("uk-Cyrl"), card.localized("UK-cyrl"))
        val twoCases =
            CardJson.cardWith(
                """"name": {"full": "Jane"},
                   "localizations": {"EN": {"name/full": "A"}, "en": {"name/full": "B"}}""",
            )
        assertEquals("B", twoCases.localized("en").name?.full)
        assertEquals("A", twoCases.localized("EN").name?.full)
    }

    @Test
    fun aCardWithoutThatLanguageStaysAsItIs() {
        val card = CardJson.decode(CARD_FIGURE_39)
        assertSame(card, card.localized("fr"))
        val plain = CardJson.decode(CARD_FIGURE_6)
        assertSame(plain, plain.localized("en"))
    }

    @Test
    fun anInvalidPatchObjectFailsWhole() {
        val card =
            CardJson.cardWith(
                """"name": {"full": "Jane"}, "localizations": {"en": {"name/full": "J", "missing/x": 1}}""",
            )
        assertFailsWith<IllegalArgumentException> { card.localized("en") }
    }

    @Test
    fun aPatchMustNotTargetLocalizations() {
        for (patch in listOf(""""localizations": {}""", """"localizations/de": {}""")) {
            val card = CardJson.cardWith(""""localizations": {"en": {$patch}, "de": {}}""")
            assertFailsWith<IllegalArgumentException> { card.localized("en") }
        }
    }

    @Test
    fun aPatchMustNotRemoveTheTypeOrTheVersion() {
        for (property in listOf("@type", "version")) {
            val card = CardJson.cardWith(""""localizations": {"en": {"$property": null}}""")
            assertFailsWith<IllegalArgumentException>(property) { card.localized("en") }
        }
        val versioned = CardJson.cardWith(""""localizations": {"en": {"version": "1.1"}}""")
        assertEquals("1.1", versioned.localized("en").version)
    }

    @Test
    fun aPatchedValueMustFitItsProperty() {
        val card = CardJson.cardWith(""""name": {"full": "Jane"}, "localizations": {"en": {"name": 5}}""")
        assertFailsWith<IllegalArgumentException> { card.localized("en") }
    }

    @Test
    fun unknownPropertiesSurviveLocalization() {
        val card =
            CardJson.cardWith(
                """"example.com:x": 1, "name": {"full": "Jane", "example.com:y": 2},
                   "localizations": {"de": {"name/full": "Johanna"}}""",
            )
        val localized = card.localized("de")
        assertEquals(mapOf("example.com:x" to JsonPrimitive(1)), localized.extra)
        assertEquals(Name(full = "Johanna", extra = mapOf("example.com:y" to JsonPrimitive(2))), localized.name)
        assertEquals("u1", localized.uid)
    }
}
