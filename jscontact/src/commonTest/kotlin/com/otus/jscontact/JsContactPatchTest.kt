package com.otus.jscontact

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsContactPatchTest {
    @Test
    fun figure20PatchesInsideArrayMembers() {
        val name = localize(PATCH_FIGURE_20, "yue").getValue("name").jsonObject
        val components = name.getValue("components").jsonArray.map { it.jsonObject }
        assertEquals(JsonPrimitive("jyut"), name["phoneticSystem"])
        assertEquals(JsonPrimitive("Latn"), name["phoneticScript"])
        assertEquals(
            listOf("syun1", "zung1saan1", "man4", "jat6sin1").map { JsonPrimitive(it) },
            components.map { it["phonetic"] },
        )
        assertEquals(JsonPrimitive("孫"), components[0]["value"])
    }

    @Test
    fun figure39ReplacesATopLevelProperty() {
        val figure = json(PATCH_FIGURE_39)
        val localized = localize(PATCH_FIGURE_39, "uk-Cyrl")
        val expected = figure.getValue("localizations").jsonObject.getValue("uk-Cyrl")
        assertEquals(expected, localized)
    }

    @Test
    fun figure40PatchesANestedProperty() {
        assertEquals(
            json(
                """{"name": {"full": "Gabriel García Márquez"},
                    "titles": {"t1": {"kind": "title", "name": "escritor"}}}""",
            ),
            localize("{$PATCH_FIGURE_40}", "es"),
        )
    }

    @Test
    fun nullRemovesAMemberAndIgnoresAnAbsentOne() {
        val target = json("""{"emails": {"e1": {"address": "a@example.com"}, "e2": {"address": "b@example.com"}}}""")
        assertEquals(
            json("""{"emails": {"e2": {"address": "b@example.com"}}}"""),
            patched(target, """{"emails/e1": null, "emails/e9": null, "notes": null}"""),
        )
    }

    @Test
    fun aValueAddsOrReplacesAMember() {
        val target = json("""{"name": {"full": "Jane"}}""")
        assertEquals(json("""{"name": {"full": "Jane Doe"}}"""), patched(target, """{"name/full": "Jane Doe"}"""))
        assertEquals(
            json("""{"name": {"full": "Jane", "isOrdered": true}, "kind": "individual"}"""),
            patched(target, """{"name/isOrdered": true, "kind": "individual"}"""),
        )
    }

    @Test
    fun anExistingArrayMemberCanBeReplaced() {
        assertEquals(
            json("""{"name": {"components": [{"kind": "given", "value": "Jane"}, $SURNAME]}}"""),
            patched(json(COMPONENTS), """{"name/components/0": {"kind": "given", "value": "Jane"}}"""),
        )
    }

    @Test
    fun arraysRejectInsertsAndDeletes() {
        val invalid =
            listOf(
                """{"name/components/0": null}""",
                """{"name/components/-": {"kind": "given", "value": "Jane"}}""",
                """{"name/components/-/value": "Jane"}""",
                """{"name/components/2": {"kind": "given", "value": "Jane"}}""",
                """{"name/components/01": {"kind": "given", "value": "Jane"}}""",
                """{"name/components/first": {"kind": "given", "value": "Jane"}}""",
            )
        invalid.forEach { patch ->
            assertFailsWith<IllegalArgumentException>(patch) { patched(json(COMPONENTS), patch) }
        }
    }

    @Test
    fun everyParentMustAlreadyExist() {
        val target = json("""{"name": {"full": "Jane"}, "nicknames": null}""")
        val invalid =
            listOf(
                """{"nonexistent/child": 1}""",
                """{"name/components/0": {"kind": "given", "value": "Jane"}}""",
                """{"name/full/0": "J"}""",
                """{"nicknames/k1": {"name": "JJ"}}""",
            )
        invalid.forEach { patch ->
            assertFailsWith<IllegalArgumentException>(patch) { patched(target, patch) }
        }
    }

    @Test
    fun aPointerMustNotBeAPrefixOfAnother() {
        val target = json("""{"addresses": {"1": {"city": "Vienna"}}}""")
        assertFailsWith<IllegalArgumentException> {
            patched(target, """{"addresses": {"1": {"city": "Graz"}}, "addresses/1/city": "X"}""")
        }
        assertFailsWith<IllegalArgumentException> {
            patched(target, """{"addresses/1": {"city": "Graz"}, "addresses/1/city": "X"}""")
        }
    }

    @Test
    fun prefixesCompareWholeTokens() {
        val target = json("""{"name": {"full": "Jane"}}""")
        assertEquals(
            json("""{"name": {"full": "Jane Doe"}, "names": 1, "name~": 2}"""),
            patched(target, """{"name/full": "Jane Doe", "names": 1, "name~0": 2}"""),
        )
    }

    @Test
    fun escapedTokensAreDecoded() {
        val target = json("""{"a/b": {"m~n": 1, "~1": 2}}""")
        assertEquals(
            json("""{"a/b": {"m~n": 3, "~1": 4}}"""),
            patched(target, """{"a~1b/m~0n": 3, "a~1b/~01": 4}"""),
        )
    }

    @Test
    fun aTildeMustStartAnEscape() {
        for (key in listOf("a~2", "a~", "~", "a~~0")) {
            assertFailsWith<IllegalArgumentException>(key) { patched(json("""{"a": 1}"""), """{"$key": 1}""") }
        }
    }

    @Test
    fun oneInvalidPatchRejectsTheWholePatchObject() {
        val target = json("""{"name": {"full": "Jane"}}""")
        assertFailsWith<IllegalArgumentException> {
            patched(target, """{"name/full": "Jane Doe", "kind": "individual", "missing/child": 1}""")
        }
        assertEquals(json("""{"name": {"full": "Jane"}}"""), target)
    }

    @Test
    fun theTypePropertyIsPatchedLikeAnyOther() {
        assertEquals(
            json("""{"@type": "Card", "name": {"@type": "Name", "full": "Jane"}}"""),
            patched(json("""{"@type": "Card", "name": {"full": "Jane"}}"""), """{"name/@type": "Name"}"""),
        )
    }

    @Test
    fun anEmptyPatchObjectChangesNothing() {
        val target = json(COMPONENTS)
        assertEquals(target, patched(target, "{}"))
    }

    /** Localizes a Card as RFC 9553, section 2.7.1, describes: patch a copy without `localizations`. */
    private fun localize(
        figure: String,
        language: String,
    ): JsonObject {
        val card = json(figure)
        val localizations = card.getValue("localizations").jsonObject
        return JsContactPatch.apply(JsonObject(card - "localizations"), localizations.getValue(language).jsonObject)
    }

    private fun patched(
        target: JsonObject,
        patch: String,
    ): JsonObject = JsContactPatch.apply(target, json(patch))

    private fun json(text: String): JsonObject = CardJson.parse(text)
}

private const val SURNAME = """{"kind": "surname", "value": "Doe"}"""

private const val COMPONENTS = """{"name": {"components": [{"kind": "given", "value": "J."}, $SURNAME]}}"""

// RFC 9553, figure 20.
private const val PATCH_FIGURE_20 = """
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

// RFC 9553, figure 39.
private const val PATCH_FIGURE_39 = """
{
  "name": {
    "components": [
      { "kind": "title", "value": "Mr." },
      { "kind": "given", "value": "Ivan" },
      { "kind": "given2", "value": "Petrovich" },
      { "kind": "surname", "value": "Vasiliev" }
    ]
  },
  "localizations": {
    "uk-Cyrl": {
      "name": {
        "components": [
          { "kind": "title", "value": "г-н" },
          { "kind": "given", "value": "Иван" },
          { "kind": "given2", "value": "Петрович" },
          { "kind": "surname", "value": "Васильев" }
        ]
      }
    }
  }
}
"""

// RFC 9553, figure 40, a fragment of members.
private const val PATCH_FIGURE_40 = """
"name": {
  "full": "Gabriel García Márquez"
},
"titles": {
  "t1": {
    "kind": "title",
    "name": "novelist"
  }
},
"localizations": {
  "es": {
    "titles/t1/name": "escritor"
  }
}
"""
