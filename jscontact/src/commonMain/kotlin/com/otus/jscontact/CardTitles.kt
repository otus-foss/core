package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/** A nickname of the entity, as a value of the Card `nicknames` map (RFC 9553, section 2.2.2). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Nickname.Serializer::class)
public data class Nickname(
    val name: String,
    val contexts: Map<ContactContext, Boolean>? = null,
    /** 1 to 100, where 1 is the most preferred nickname (section 1.5.3). */
    val pref: Int? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer :
        JsContactSerializer<Nickname>(
            generatedSerializer(),
            "Nickname",
            { it.extra },
            { value, extra -> value.copy(extra = extra) },
        )
}

/** A job title or role of the entity, as a value of the Card `titles` map (RFC 9553, section 2.2.5). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Title.Serializer::class)
public data class Title(
    val name: String,
    /** Absent means [TitleKind.Title]. */
    val kind: TitleKind? = null,
    /** The key of the [Organization] in the Card `organizations` map, which may lack that key. */
    val organizationId: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer :
        JsContactSerializer<Title>(
            generatedSerializer(),
            "Title",
            { it.extra },
            { value, extra -> value.copy(extra = extra) },
        )
}

/**
 * Whether a [Title] is an organizational position or a more temporary role (RFC 9553, section
 * 2.2.5). Other values are vendor-specific, in the form `example.com:baz` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class TitleKind(
    public val value: String,
) {
    /** The registered title kinds (RFC 9553, section 2.2.5). */
    public companion object {
        /** An organizational position, such as "Research Scientist". */
        public val Title: TitleKind = TitleKind("title")

        /** A temporary assignment, such as "Project Leader" in project management. */
        public val Role: TitleKind = TitleKind("role")
    }
}
