package com.otus.jscontact

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * Serializes one JSContact object class (RFC 9553, section 1.3.1) through its plugin-generated
 * serializer [generated], and keeps every member that class does not declare in its `extra` map.
 * Implementations must preserve unknown and vendor-specific properties (sections 1.7.4 and 1.8.1),
 * and kotlinx.serialization can only drop or reject them.
 *
 * Decoding fails when a present `@type` differs from [typeName] (section 1.3.4). Encoding always
 * writes `@type` first, because a top-level object must carry it and every other position allows
 * it. Only JSON can carry members the class does not declare, so other formats fail.
 */
internal abstract class JsContactSerializer<T : Any>(
    private val generated: KSerializer<T>,
    private val typeName: String,
    private val extraOf: (T) -> Map<String, JsonElement>,
    private val withExtra: (T, Map<String, JsonElement>) -> T,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = generated.descriptor

    private val knownNames: Set<String> = descriptor.elementNames.toSet()

    override fun deserialize(decoder: Decoder): T {
        val input = decoder.asJson()
        val members =
            input.decodeJsonElement() as? JsonObject
                ?: throw SerializationException("$typeName is not a JSON object")
        requireType(members[TYPE])
        val properties = members - TYPE
        val value = input.json.decodeFromJsonElement(generated, JsonObject(properties.filterKeys { it in knownNames }))
        return withExtra(value, properties.filterKeys { it !in knownNames })
    }

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) {
        val output = encoder.asJson()
        val properties = output.json.encodeToJsonElement(generated, value).jsonObject
        // A key the class declares is never taken from extra, so encoding and decoding agree.
        val members =
            mapOf(TYPE to JsonPrimitive(typeName)) +
                (properties - TYPE) +
                extraOf(value).filterKeys { it != TYPE && it !in knownNames }
        output.encodeJsonElement(JsonObject(members))
    }

    private fun requireType(type: JsonElement?) {
        val name = (type as? JsonPrimitive)?.takeIf { it.isString }?.content
        if (type != null && name != typeName) {
            throw SerializationException("Expected @type \"$typeName\", found $type")
        }
    }

    private fun Decoder.asJson(): JsonDecoder =
        this as? JsonDecoder ?: throw SerializationException("$typeName can only be read from JSON")

    private fun Encoder.asJson(): JsonEncoder =
        this as? JsonEncoder ?: throw SerializationException("$typeName can only be written as JSON")

    private companion object {
        const val TYPE = "@type"
    }
}
