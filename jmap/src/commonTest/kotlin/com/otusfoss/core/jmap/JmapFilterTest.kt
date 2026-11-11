package com.otusfoss.core.jmap

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JmapFilterTest {
    private val music = hasKeyword("music")
    private val video = hasKeyword("video")
    private val piano = hasKeyword("piano")

    @Test
    fun buildsTheRfcExampleFilter() {
        // The filter of the Todo/query example in RFC 8620, section 5.7.
        assertEquals(
            parseObject(
                """{"operator": "OR", "conditions": [{"hasKeyword": "music"}, {"hasKeyword": "video"}]}""",
            ),
            anyOf(music, video).toJson(),
        )
    }

    @Test
    fun operatorsNestAndOneFilterStaysItself() {
        assertEquals(music, allOf(listOf(music)))
        assertEquals(music, anyOf(listOf(music)))
        assertEquals(
            parseObject(
                """{"operator": "AND", "conditions": [{"operator": "OR", "conditions":
                [{"hasKeyword": "music"}, {"hasKeyword": "video"}]}, {"hasKeyword": "piano"}]}""",
            ),
            allOf(anyOf(music, video), piano).toJson(),
        )
    }

    @Test
    fun notMatchesWhenNoConditionMatches() {
        assertEquals(
            parseObject(
                """{"operator": "NOT", "conditions": [{"hasKeyword": "music"}, {"hasKeyword": "video"}]}""",
            ),
            noneOf(music, video).toJson(),
        )
        assertEquals(parseObject("""{"operator": "AND", "conditions": []}"""), allOf().toJson())
    }

    @Test
    fun conditionsAreSentAsGivenAndCannotCarryAnOperator() {
        val condition = buildJsonObject { put("text", JsonPrimitive("ada")) }
        assertEquals(condition, JmapFilter.Condition(condition).toJson())
        assertFailsWith<IllegalArgumentException> {
            JmapFilter.Condition(buildJsonObject { put("operator", JsonPrimitive("AND")) })
        }
    }
}
