package com.otus.jmap

import com.otus.json.types.patchPath
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JmapPatchTest {
    @Test
    fun buildsTheRfcMinimalPatch() {
        // The minimal Todo/set patch of RFC 8620, section 5.7.
        val patch =
            jmapPatch {
                set(JsonPrimitive(true), "keywords", "chopin")
                remove("keywords", "mozart")
            }
        assertEquals(parseObject("""{"keywords/chopin": true, "keywords/mozart": null}"""), patch)
    }

    @Test
    fun escapesEachSegmentAsAJsonPointer() {
        // Member names from the examples of RFC 6901, section 5, without the leading "/".
        assertEquals("a~1b", patchPath("a/b"))
        assertEquals("m~0n", patchPath("m~n"))
        assertEquals("c%d", patchPath("c%d"))
        assertEquals("k\"l", patchPath("k\"l"))
        // Escaping "~" first keeps a literal "~1" from turning into "/".
        assertEquals("~01", patchPath("~1"))
        assertEquals("keywords/a~1b~0c", patchPath("keywords", "a/b~c"))
        val escaped = jmapPatch { set(JsonPrimitive(true), "keywords", "a/b") }
        assertEquals(parseObject("""{"keywords/a~1b": true}"""), escaped)
    }

    @Test
    fun overlappingPathsAreRejectedBeforeTheServerSeesThem() {
        // RFC 8620, section 5.3: no pointer may be a prefix of another, as in "alerts/1/offset" and "alerts".
        assertFailsWith<IllegalArgumentException> {
            jmapPatch {
                remove("alerts")
                set(JsonPrimitive("-PT5M"), "alerts", "1", "offset")
            }
        }
        assertFailsWith<IllegalArgumentException> {
            jmapPatch {
                set(JsonPrimitive("-PT5M"), "alerts", "1", "offset")
                include(parseObject("""{"alerts": null}"""))
            }
        }
        val siblings =
            jmapPatch {
                set(JsonPrimitive("Ada"), "name")
                set(JsonPrimitive("Ada"), "names")
                set(JsonPrimitive("Grace"), "name")
            }
        assertEquals(parseObject("""{"name": "Grace", "names": "Ada"}"""), siblings)
    }

    @Test
    fun includeMergesAnotherPatch() {
        val names = jmapPatch { set(JsonPrimitive("Ada"), "name", "full") }
        val patch =
            jmapPatch {
                include(names)
                remove("emails", "e1")
            }
        assertEquals(parseObject("""{"name/full": "Ada", "emails/e1": null}"""), patch)
    }

    @Test
    fun aPathNamesAtLeastOneProperty() {
        assertFailsWith<IllegalArgumentException> { patchPath() }
        assertFailsWith<IllegalArgumentException> { jmapPatch { remove() } }
    }
}
