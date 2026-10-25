package com.otus.jmap

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
        assertEquals(anyOf(music, video), music or video)
    }

    @Test
    fun operatorsNestAndChainsOfOneOperatorStayFlat() {
        assertEquals(allOf(music, video, piano), music and video and piano)
        assertEquals(allOf(music, video, piano), music and (video and piano))
        assertEquals(allOf(anyOf(music, video), piano), (music or video) and piano)
        assertEquals(
            parseObject(
                """{"operator": "AND", "conditions": [{"operator": "OR", "conditions":
                [{"hasKeyword": "music"}, {"hasKeyword": "video"}]}, {"hasKeyword": "piano"}]}""",
            ),
            ((music or video) and piano).toJson(),
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
