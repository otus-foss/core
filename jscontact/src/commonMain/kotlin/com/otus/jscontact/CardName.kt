package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * The name of the entity a Card represents (RFC 9553, section 2.2.1.1). At least one of
 * [components] and [full] must be set.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Name.Serializer::class)
public data class Name(
    /** The parts of the name. At least one of them must not be a separator. */
    val components: List<NameComponent>? = null,
    /** Whether joining the [components] values in list order gives the full name. Absent means `false`. */
    val isOrdered: Boolean? = null,
    /** The text to insert between components. Set it only when the components are ordered. */
    val defaultSeparator: String? = null,
    /** The whole name as one string. Required when [components] is absent. */
    val full: String? = null,
    /** The verbatim string to compare when sorting by a component kind. Set it only with [components]. */
    val sortAs: Map<NameComponentKind, String>? = null,
    /** The script of each [NameComponent.phonetic], as an RFC 5646 script subtag (section 1.5.4). */
    val phoneticScript: String? = null,
    /** The phonetic system of each [NameComponent.phonetic] (section 1.5.4). */
    val phoneticSystem: PhoneticSystem? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<Name>(
        generatedSerializer(),
        "Name",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/** One part of a [Name], such as a given name or a surname (RFC 9553, section 2.2.1.2). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = NameComponent.Serializer::class)
public data class NameComponent(
    /** The text, which may hold several words such as "van Gogh". A separator holds its verbatim text. */
    val value: String,
    val kind: NameComponentKind,
    /** The pronunciation. The parent [Name] must then set its phonetic script or system. */
    val phonetic: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<NameComponent>(
        generatedSerializer(),
        "NameComponent",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of a [NameComponent] (RFC 9553, section 2.2.1.2). Other values are vendor-specific, in
 * the form `example.com:baz` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class NameComponentKind(
    public val value: String,
) {
    /** The registered component kinds (RFC 9553, section 2.2.1.2). */
    public companion object {
        /** An honorific title or prefix, such as "Mr.", "Ms.", or "Dr.". */
        public val Title: NameComponentKind = NameComponentKind("title")

        /** A given name, also known as first name or personal name. */
        public val Given: NameComponentKind = NameComponentKind("given")

        /** A name between the given name and the surname, such as a middle name or a patronymic. */
        public val Given2: NameComponentKind = NameComponentKind("given2")

        /** A surname, also known as last name or family name. */
        public val Surname: NameComponentKind = NameComponentKind("surname")

        /** A secondary surname, used in some cultures, also known as maternal surname. */
        public val Surname2: NameComponentKind = NameComponentKind("surname2")

        /** A credential or honorific suffix, such as "B.A." or "Esq.". */
        public val Credential: NameComponentKind = NameComponentKind("credential")

        /** A generation marker, such as "Jr." or "III". */
        public val Generation: NameComponentKind = NameComponentKind("generation")

        /**
         * The verbatim text between two ordered components. It takes precedence over
         * [Name.defaultSeparator]. Two separators must not be adjacent, and unordered names have none.
         */
        public val Separator: NameComponentKind = NameComponentKind("separator")
    }
}

/**
 * The phonetic system of a `phonetic` value, set on [Name] and on Address (RFC 9553, section 1.5.4).
 * Other values are vendor-specific, in the form `example.com:baz` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class PhoneticSystem(
    public val value: String,
) {
    /** The registered phonetic systems (RFC 9553, section 1.5.4). */
    public companion object {
        /** The International Phonetic Alphabet. */
        public val Ipa: PhoneticSystem = PhoneticSystem("ipa")

        /** Jyutping, the Cantonese romanization system. */
        public val Jyut: PhoneticSystem = PhoneticSystem("jyut")

        /** Hanyu Pinyin, the Standard Mandarin romanization system. */
        public val Piny: PhoneticSystem = PhoneticSystem("piny")
    }
}
