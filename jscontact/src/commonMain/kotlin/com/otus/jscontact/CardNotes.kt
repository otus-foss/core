package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline
import kotlin.time.Instant

/** A free-text note about the Card (RFC 9553, section 2.8.3). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Note.Serializer::class)
public data class Note(
    /** The text of the note. */
    val note: String,
    /** When the note was written. */
    @Serializable(with = UtcDateTimeSerializer::class)
    val created: Instant? = null,
    val author: Author? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<Note>(
        generatedSerializer(),
        "Note",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/** The author of a [Note] (RFC 9553, section 2.8.3). At least one of [name] and [uri] must be set. */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Author.Serializer::class)
public data class Author(
    val name: String? = null,
    /** A URI that identifies the author. */
    val uri: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<Author>(
        generatedSerializer(),
        "Author",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * A hobby, interest, or field of expertise of the entity (RFC 9553, section 2.8.4). It has no `contexts` and
 * no `pref`.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = PersonalInfo.Serializer::class)
public data class PersonalInfo(
    val kind: PersonalInfoKind,
    /** The information itself, such as "chemistry". */
    val value: String,
    /** How expert or engaged the entity is. */
    val level: PersonalInfoLevel? = null,
    /**
     * The position among the Card's entries of the same [kind]. It must be above zero, and entries with
     * equal or missing values have no defined order (RFC 9553, section 2.8.4).
     */
    val listAs: Long? = null,
    /** A custom label (RFC 9553, section 1.5.2). */
    val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<PersonalInfo>(
        generatedSerializer(),
        "PersonalInfo",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of a [PersonalInfo] (RFC 9553, section 2.8.4). Values outside the companion are vendor-specific
 * and must be `v-extension` names such as `example.com:foo` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class PersonalInfoKind(
    public val value: String,
) {
    /** The registered values (RFC 9553, section 3.7, Table 18). */
    public companion object {
        /** A field of expertise or a credential. */
        public val Expertise: PersonalInfoKind = PersonalInfoKind("expertise")

        /** A hobby. */
        public val Hobby: PersonalInfoKind = PersonalInfoKind("hobby")

        /** An interest. */
        public val Interest: PersonalInfoKind = PersonalInfoKind("interest")
    }
}

/**
 * How expert or engaged the entity is in a [PersonalInfo] (RFC 9553, section 2.8.4). Values outside the
 * companion are vendor-specific and must be `v-extension` names (section 1.8.2).
 */
@Serializable
@JvmInline
public value class PersonalInfoLevel(
    public val value: String,
) {
    /** The registered values (RFC 9553, section 3.7, Table 20). */
    public companion object {
        /** A high level. */
        public val High: PersonalInfoLevel = PersonalInfoLevel("high")

        /** A medium level. */
        public val Medium: PersonalInfoLevel = PersonalInfoLevel("medium")

        /** A low level. */
        public val Low: PersonalInfoLevel = PersonalInfoLevel("low")
    }
}
