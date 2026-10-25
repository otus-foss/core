package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * How to address, speak to, or refer to the entity a Card represents (RFC 9553, section 2.2.4). At
 * least one of [grammaticalGender] and [pronouns] must be set.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = SpeakToAs.Serializer::class)
public data class SpeakToAs(
    /** The grammatical gender for salutations. It says nothing about gender identity or assigned sex. */
    val grammaticalGender: GrammaticalGender? = null,
    /** The pronouns the contact chooses, keyed by Id (section 1.4.1). */
    val pronouns: Map<String, Pronouns>? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer :
        JsContactSerializer<SpeakToAs>(
            generatedSerializer(),
            "SpeakToAs",
            { it.extra },
            { value, extra -> value.copy(extra = extra) },
        )
}

/** Pronouns the contact uses for themselves, such as "they/them" (RFC 9553, section 2.2.4). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Pronouns.Serializer::class)
public data class Pronouns(
    /** The pronouns in any form. A localization may replace them (section 2.7.1). */
    val pronouns: String,
    val contexts: Map<ContactContext, Boolean>? = null,
    /** 1 to 100, where 1 is most preferred among pronouns of the same context (section 1.5.3). */
    val pref: Int? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer :
        JsContactSerializer<Pronouns>(
            generatedSerializer(),
            "Pronouns",
            { it.extra },
            { value, extra -> value.copy(extra = extra) },
        )
}

/**
 * The grammatical gender for salutations and other grammatical constructs (RFC 9553, section 2.2.4).
 * German, for example, writes "Sehr geehrte" for feminine and "Sehr geehrter" for masculine. Other
 * values are vendor-specific, in the form `example.com:baz` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class GrammaticalGender(
    public val value: String,
) {
    /** The registered grammatical genders (RFC 9553, section 2.2.4). */
    public companion object {
        /** The animate gender, in languages that split nouns into animate and inanimate. */
        public val Animate: GrammaticalGender = GrammaticalGender("animate")

        /** The common gender, in languages such as Swedish that merged masculine and feminine. */
        public val Common: GrammaticalGender = GrammaticalGender("common")

        /** The feminine gender, as in the German salutation "Sehr geehrte". */
        public val Feminine: GrammaticalGender = GrammaticalGender("feminine")

        /** The inanimate gender, the counterpart of [Animate]. */
        public val Inanimate: GrammaticalGender = GrammaticalGender("inanimate")

        /** The masculine gender, as in the German salutation "Sehr geehrter". */
        public val Masculine: GrammaticalGender = GrammaticalGender("masculine")

        /** The neuter gender, in languages such as German that have one. */
        public val Neuter: GrammaticalGender = GrammaticalGender("neuter")
    }
}
