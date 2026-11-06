package com.otus.avatar

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AvatarStyleTest {
    @Test
    fun everyMarkSurvivesTheVCardProperty() {
        listOf(AvatarMark.Letters("FR"), AvatarMark.Icon("work"), AvatarMark.Website).forEach { mark ->
            val style = AvatarStyle("#4D6F92", mark)
            val vCard = JsonObject(mapOf("properties" to avatarPropertiesWith(null, style)))
            assertEquals(style, avatarStyleIn(vCard))
        }
    }

    @Test
    fun aDesignSystemColorNameSavedBeforeTheIdsReadsAsItsId() {
        val saved = """{"properties": [["x-otus-avatar", {}, "text", "{\"color\": \"Blue\", \"letters\": \"AL\"}"]]}"""
        assertEquals(AvatarStyle("blue", AvatarMark.Letters("AL")), avatarStyleIn(Json.parseToJsonElement(saved)))
        assertEquals(AvatarColor.Purple, AvatarColor.of("Purple"))
        assertNull(AvatarColor.of("#4D6F92"))
    }

    @Test
    fun savingAStyleKeepsTheCardsOtherVCardProperties() {
        val vCard =
            Json.parseToJsonElement(
                """{"properties": [["x-abc", {}, "text", "keep"], ["X-OTUS-AVATAR", {}, "text", "{}"]]}""",
            )
        val properties = avatarPropertiesWith(vCard, AvatarStyle("blue", AvatarMark.Icon("home")))
        assertEquals(listOf("x-abc", AVATAR_STYLE_PROPERTY), properties.map { it.jsonArray[0].jsonPrimitive.content })
    }

    @Test
    fun removingAStyleLeavesOnlyTheOtherProperties() {
        val vCard =
            JsonObject(
                mapOf("properties" to avatarPropertiesWith(null, AvatarStyle("blue", AvatarMark.Icon("home")))),
            )
        assertTrue(hasAvatarStyle(vCard))
        assertTrue(avatarPropertiesWith(vCard, null).isEmpty())
    }

    @Test
    fun cardsWithoutAReadableStyleHaveNone() {
        assertNull(avatarStyleIn(null))
        assertFalse(hasAvatarStyle(Json.parseToJsonElement("""{"properties": [["x-abc", {}, "text", "x"]]}""")))
        assertNull(
            avatarStyleIn(Json.parseToJsonElement("""{"properties": [["x-otus-avatar", {}, "text", "not json"]]}""")),
        )
        assertNull(
            avatarStyleIn(
                Json.parseToJsonElement("""{"properties": [["x-otus-avatar", {}, "text", "{\"color\": \"Blue\"}"]]}"""),
            ),
        )
    }

    @Test
    fun lettersLoseSpacesAndStopAtThree() {
        assertEquals("ABC", avatarLetters(" A B CD"))
    }
}
