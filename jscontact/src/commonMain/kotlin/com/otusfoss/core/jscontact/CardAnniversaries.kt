package com.otusfoss.core.jscontact

import com.otusfoss.core.json.types.UtcDateSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.jvm.JvmInline
import kotlin.time.Instant

/** A memorable date of the entity, such as a birthday (RFC 9553, section 2.8.1). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Anniversary.Serializer::class)
public data class Anniversary(
    val kind: AnniversaryKind,
    /** A [PartialDate] or a [Timestamp] in the Gregorian calendar. */
    @Serializable(with = AnniversaryDateSerializer::class)
    val date: AnniversaryDate,
    /** A place tied to the date, such as the place of birth or death. */
    val place: Address? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<Anniversary>(
        generatedSerializer(),
        "Anniversary",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of an [Anniversary] (RFC 9553, section 2.8.1). Values outside the companion are vendor-specific
 * and must be `v-extension` names such as `example.com:foo` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class AnniversaryKind(
    public val value: String,
) {
    /** The registered values (RFC 9553, section 3.7, Table 11). */
    public companion object {
        /** A birthday. */
        public val Birth: AnniversaryKind = AnniversaryKind("birth")

        /** A day of death. */
        public val Death: AnniversaryKind = AnniversaryKind("death")

        /** A wedding day. */
        public val Wedding: AnniversaryKind = AnniversaryKind("wedding")
    }
}

/**
 * The date of an [Anniversary], either a [PartialDate] or a [Timestamp] (RFC 9553, section 2.8.1). The JSON
 * names the type in `@type`. A date without `@type` is a PartialDate, the property's defaultType, so a
 * Timestamp always carries `"@type": "Timestamp"` (section 1.3.4).
 */
public sealed interface AnniversaryDate

/**
 * A whole or partial Gregorian date: a year, a month in a year, a day in a month, or a full date (RFC 9553,
 * section 2.8.1). Decoding keeps values out of range, so validation reports them instead of failing a page.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = PartialDate.Serializer::class)
public data class PartialDate(
    val year: Long? = null,
    /** From 1 to 12. When set, [year] or [day] must be set too. */
    val month: Long? = null,
    /** From 1 to 31, and valid for the month and year. When set, [month] must be set too. */
    val day: Long? = null,
    /**
     * The calendar system the date occurs in, as a lowercase CLDR name or a vendor value. [year], [month],
     * and [day] stay Gregorian whatever it names.
     */
    val calendarScale: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) : AnniversaryDate {
    internal object Serializer : JsContactSerializer<PartialDate>(
        generatedSerializer(),
        PARTIAL_DATE,
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/** A complete point in time in UTC (RFC 9553, section 2.8.1). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Timestamp.Serializer::class)
public data class Timestamp(
    @Serializable(with = UtcDateSerializer::class)
    val utc: Instant,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) : AnniversaryDate {
    internal object Serializer : JsContactSerializer<Timestamp>(
        generatedSerializer(),
        TIMESTAMP,
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * Picks the [AnniversaryDate] type from `@type`, with PartialDate as the default (RFC 9553, sections 1.3.4
 * and 2.8.1). kotlinx polymorphism does not fit, because it wants a discriminator on every value and has no
 * default type. Each concrete serializer then checks the object and writes its own `@type`.
 */
internal object AnniversaryDateSerializer : KSerializer<AnniversaryDate> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.otusfoss.core.jscontact.AnniversaryDate")

    override fun deserialize(decoder: Decoder): AnniversaryDate {
        val input = decoder as? JsonDecoder ?: throw SerializationException("AnniversaryDate needs JSON")
        val element = input.decodeJsonElement()
        val strategy: DeserializationStrategy<AnniversaryDate> =
            when (val type = ((element as? JsonObject)?.get(TYPE_PROPERTY) as? JsonPrimitive)?.content) {
                null, PARTIAL_DATE -> PartialDate.serializer()
                TIMESTAMP -> Timestamp.serializer()
                else -> throw SerializationException("Unknown AnniversaryDate type: $type")
            }
        return input.json.decodeFromJsonElement(strategy, element)
    }

    override fun serialize(
        encoder: Encoder,
        value: AnniversaryDate,
    ) {
        when (value) {
            is PartialDate -> encoder.encodeSerializableValue(PartialDate.serializer(), value)
            is Timestamp -> encoder.encodeSerializableValue(Timestamp.serializer(), value)
        }
    }
}

private const val PARTIAL_DATE = "PartialDate"
private const val TIMESTAMP = "Timestamp"
