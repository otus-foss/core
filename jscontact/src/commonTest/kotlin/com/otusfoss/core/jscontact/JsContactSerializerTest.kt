package com.otusfoss.core.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JsContactSerializerTest {
    @Test
    fun rfcFooExampleRoundTripsWithItsType() {
        val foo = decode("""{"@type":"Foo","baz":5}""")
        assertEquals(DemoFoo(baz = 5), foo)
        assertEquals("""{"@type":"Foo","baz":5}""", encode(foo))
    }

    @Test
    fun typeMayBeOmittedOnReadButIsAlwaysWrittenFirst() {
        val foo = decode("""{"label":"Home","baz":5}""")
        assertEquals(DemoFoo(baz = 5, label = "Home"), foo)
        assertEquals("""{"@type":"Foo","baz":5,"label":"Home"}""", encode(foo))
    }

    @Test
    fun valuesNestedTooDeeplyToEncodeFailToDecode() {
        val tooDeep = "[".repeat(1_000) + "]".repeat(1_000)
        for (member in listOf("\"x\": $tooDeep", "\"localizations\": {\"de\": {\"x\": $tooDeep}}")) {
            assertFailsWith<SerializationException>(member.take(20)) { CardJson.cardWith(member) }
        }
        val card = CardJson.cardWith("\"x\": " + "[".repeat(64) + "]".repeat(64))
        val written = JsContactJson.encodeToString(Card.serializer(), card)
        assertEquals(card, CardJson.decode(written))
    }

    @Test
    fun aTypeThatDiffersEvenOnlyInCaseIsRejected() {
        for (type in listOf("\"foo\"", "\"Bar\"", "\"Resource\"", "5", "null", "{}")) {
            assertFailsWith<SerializationException> { decode("""{"@type":$type,"baz":5}""") }
        }
    }

    @Test
    fun aMissingMandatoryPropertyFails() {
        assertFailsWith<SerializationException> { decode("""{"@type":"Foo"}""") }
    }

    @Test
    fun onlyObjectsDecode() {
        for (text in listOf("[]", "5", "\"Foo\"")) {
            assertFailsWith<SerializationException> { decode(text) }
        }
    }

    @Test
    fun vendorAndUnknownPropertiesSurviveARoundTrip() {
        // The vendor members are RFC 9553, figure 3; "futureProperty" is an unknown registered-style name.
        val text =
            """{"@type":"Foo","baz":5,"example.com:foo":"bar","example.com:foo2":{"bar":"baz"},
               "futureProperty":[1,true],"extra":"reserved, but still kept for validation"}"""
        val foo = decode(text)
        assertEquals(
            setOf("example.com:foo", "example.com:foo2", "futureProperty", "extra"),
            foo.extra.keys,
        )
        assertEquals(JsonPrimitive("bar"), foo.extra["example.com:foo"])
        assertEquals(CardJson.parse(text), CardJson.parse(encode(foo)))
        assertTrue(encode(foo).startsWith("""{"@type":"Foo","baz":5,"""), encode(foo))
    }

    @Test
    fun nestedObjectsKeepTheirOwnUnknownProperties() {
        val text = """{"baz":1,"bar":{"name":"n","example.com:x":true}}"""
        val foo = decode(text)
        assertEquals(DemoBar("n", mapOf("example.com:x" to JsonPrimitive(true))), foo.bar)
        assertTrue(foo.extra.isEmpty(), foo.extra.toString())
        assertEquals(
            """{"@type":"Foo","baz":1,"bar":{"@type":"Bar","name":"n","example.com:x":true}}""",
            encode(foo),
        )
    }

    @Test
    fun extraNeverOverridesTheTypeOrADeclaredProperty() {
        val foo =
            DemoFoo(
                baz = 1,
                extra =
                    mapOf(
                        "@type" to JsonPrimitive("Other"),
                        "label" to JsonPrimitive("smuggled"),
                        "baz" to JsonPrimitive(2),
                        "example.com:kept" to JsonPrimitive(3),
                    ),
            )
        assertEquals("""{"@type":"Foo","baz":1,"example.com:kept":3}""", encode(foo))
    }

    @Test
    fun nullOptionalPropertiesAreLeftOut() {
        val foo = decode("""{"baz":1,"label":null,"contexts":null}""")
        assertEquals(DemoFoo(baz = 1), foo)
        assertEquals("""{"@type":"Foo","baz":1}""", encode(foo))
    }

    @Test
    fun contextsAreOpenValueClassKeys() {
        val foo = decode("""{"baz":1,"contexts":{"work":true,"example.com:lab":true}}""")
        assertEquals(
            mapOf(ContactContext.Work to true, ContactContext("example.com:lab") to true),
            foo.contexts,
        )
        assertEquals(
            buildJsonObject {
                put("@type", "Foo")
                put("baz", 1)
                put("contexts", buildJsonObject { put("work", true) })
            },
            CardJson.parse(encode(DemoFoo(baz = 1, contexts = mapOf(ContactContext.Work to true)))),
        )
    }

    @Test
    fun otherFormatsAreRejected() {
        assertFailsWith<SerializationException> { DemoFoo.serializer().serialize(NonJsonEncoder(), DemoFoo(baz = 1)) }
        assertFailsWith<SerializationException> { DemoFoo.serializer().deserialize(NonJsonDecoder()) }
    }

    private fun decode(text: String): DemoFoo = JsContactJson.decodeFromString(DemoFoo.serializer(), text)

    private fun encode(foo: DemoFoo): String = JsContactJson.encodeToString(DemoFoo.serializer(), foo)
}

/** The demonstration object of RFC 9553, section 1.3.1, written the way every JSContact class is. */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = DemoFoo.Serializer::class)
private data class DemoFoo(
    val baz: Long,
    val contexts: Map<ContactContext, Boolean>? = null,
    val label: String? = null,
    val bar: DemoBar? = null,
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    object Serializer : JsContactSerializer<DemoFoo>(
        generatedSerializer(),
        "Foo",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = DemoBar.Serializer::class)
private data class DemoBar(
    val name: String,
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    object Serializer : JsContactSerializer<DemoBar>(
        generatedSerializer(),
        "Bar",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

@OptIn(ExperimentalSerializationApi::class)
private class NonJsonEncoder : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()
}

@OptIn(ExperimentalSerializationApi::class)
private class NonJsonDecoder : AbstractDecoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = CompositeDecoder.DECODE_DONE
}
