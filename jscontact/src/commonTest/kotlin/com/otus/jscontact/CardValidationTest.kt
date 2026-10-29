package com.otus.jscontact

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardValidationTest {
    @Test
    fun aCardNeedsAUid() {
        val problem = Card().validate().single()
        assertEquals("/uid", problem.path)
        assertTrue("2.1.9" in problem.message, problem.message)
    }

    @Test
    fun theVersionIsARegisteredMajorVersionOne() {
        for (version in listOf("1", "1.", ".0", "v1.0", "1.0.0", "", "2.0", "0.1")) {
            assertEquals(listOf("/version"), paths(Card(uid = "u1", version = version)), version)
        }
        for (version in listOf("1.0", "1.10")) {
            assertEquals(emptyList(), paths(Card(uid = "u1", version = version)), version)
        }
    }

    @Test
    fun onlyGroupsHaveMembers() {
        assertEquals(listOf("/members"), paths(Card(uid = "u1", members = mapOf("u2" to true))))
        assertEquals(emptyList(), paths(Card(uid = "u1", kind = CardKind.Group, members = mapOf("u2" to true))))
    }

    @Test
    fun setValuesMustBeTrue() {
        assertEquals(
            listOf("/members/u2"),
            paths(Card(uid = "u1", kind = CardKind.Group, members = mapOf("u2" to false, "u3" to true))),
        )
        assertEquals(listOf("/keywords/a~1b"), paths(Card(uid = "u1", keywords = mapOf("a/b" to false))))
        CardJson.assertProblemAt(
            "/relatedTo/u2/relation/friend",
            """"relatedTo": {"u2": {"relation": {"friend": false}}}""",
        )
        CardJson.assertProblemAt(
            "/phones/p/features/voice",
            """"phones": {"p": {"number": "1", "features": {"voice": false}}}""",
        )
        CardJson.assertProblemAt(
            "/addresses/a/contexts/work",
            """"addresses": {"a": {"full": "x", "contexts": {"work": false}}}""",
        )
        CardJson.assertProblemAt("/addressBookIds/b1", """"addressBookIds": {"b1": false}""")
    }

    @Test
    fun otherContextsMayBeFalse() {
        CardJson.assertValid(""""emails": {"e": {"address": "a@example.com", "contexts": {"work": false}}}""")
    }

    @Test
    fun aProductIdIsNeverEmpty() {
        assertEquals(listOf("/prodId"), paths(Card(uid = "u1", prodId = "")))
    }

    @Test
    fun jmapIdsUseTheIdSyntaxAndACardHasAnAddressBook() {
        assertEquals(listOf("/id"), paths(Card(uid = "u1", id = "bad id")))
        assertEquals(listOf("/addressBookIds"), paths(Card(uid = "u1", addressBookIds = emptyMap())))
        assertEquals(listOf("/addressBookIds/a b"), paths(Card(uid = "u1", addressBookIds = mapOf("a b" to true))))
        assertEquals(emptyList(), paths(Card(uid = "u1", id = "c-1_A", addressBookIds = mapOf("b1" to true))))
    }

    @Test
    fun propertyNamesMustBeRegisteredStyleOrVendorSpecific() {
        val members =
            """"Phones": {}, "extra": 1, "Future": 1, "bad_name": 1, "@Type": 1, "example.com:a~b": 1,
               "example.com/x:y": 1, "example.com:ok": 1, "ex-ample.com:a b:c": 1, "futureThing": 1, "@future": 1,
               "example.com:😀": 1, "😀.example:x": 1"""
        assertEquals(
            listOf("/Phones", "/extra", "/Future", "/bad_name", "/@Type", "/example.com:a~0b", "/example.com~1x:y"),
            CardJson.problemPaths(members),
        )
        CardJson.assertProblemAt("/emails/e/Address", """"emails": {"e": {"address": "a@example.com", "Address": 1}}""")
        CardJson.assertProblemAt(
            "/name/components/0/Kind",
            """"name": {"components": [{"kind": "given", "value": "A", "Kind": 1}]}""",
        )
    }

    @Test
    fun localizationTagsAreLanguageTags() {
        CardJson.assertProblemAt("/localizations/not a tag", """"localizations": {"not a tag": {}}""")
        CardJson.assertValid(""""localizations": {"de-AT": {}, "x-private": {}}""")
    }

    @Test
    fun aLocalizationThatDoesNotApplyIsReportedAtItsTag() {
        CardJson.assertProblemAt("/localizations/en", """"localizations": {"en": {"missing/x": 1}}""")
        CardJson.assertProblemAt("/localizations/en", """"localizations": {"en": {"localizations": {}}}""")
        CardJson.assertProblemAt("/localizations/en", """"localizations": {"en": {"name": 5}}""")
        CardJson.assertProblemAt("/localizations/en", """"localizations": {"en": {"version": null}}""")
        CardJson.assertProblemAt("/localizations/en", """"localizations": {"en": {"@type": null}}""")
    }

    @Test
    fun aLocalizationMustLeaveValidValues() {
        val card =
            CardJson.cardWith(
                """"emails": {"e": {"address": "a@example.com"}},
               "localizations": {"en": {"emails/e/pref": 0}}""",
            )
        val problem = card.validate().single()
        assertEquals("/localizations/en", problem.path)
        assertTrue(problem.message.startsWith("/emails/e/pref: "), problem.message)
    }

    @Test
    fun aLocalizationIsNotBlamedForProblemsTheCardAlreadyHas() {
        assertEquals(listOf("/uid"), CardJson.decode(CARD_FIGURE_20).validate().map { it.path })
        CardJson.assertProblemAt(
            "/emails/e/pref",
            """"emails": {"e": {"address": "a@example.com", "pref": 0}},
               "localizations": {"en": {"emails/e/label": "x"}}""",
        )
    }

    private fun paths(card: Card): List<String> = card.validate().map { it.path }
}
