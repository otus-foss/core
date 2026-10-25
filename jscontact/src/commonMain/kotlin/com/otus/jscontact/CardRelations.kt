package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * How a related Card relates to this one, as a value of the Card `relatedTo` map, which is keyed by
 * the related Card's `uid` (RFC 9553, section 2.1.8). An absent or empty [relation] leaves the
 * relationship undefined.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Relation.Serializer::class)
public data class Relation(
    /** The set of relation types. Each value must be `true`. */
    val relation: Map<RelationType, Boolean>? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer :
        JsContactSerializer<Relation>(
            generatedSerializer(),
            "Relation",
            { it.extra },
            { value, extra -> value.copy(extra = extra) },
        )
}

/**
 * What the related Card is to this Card, as a key of [Relation.relation] (RFC 9553, section 2.1.8).
 * The registered values are the vCard RELATED types (RFC 6350, section 6.6.6). Other values are
 * vendor-specific, in the form `example.com:baz` (RFC 9553, section 1.8.2).
 */
@Serializable
@JvmInline
public value class RelationType(
    public val value: String,
) {
    /** The registered relation types (RFC 9553, section 2.1.8). */
    public companion object {
        /** Someone the entity has exchanged greetings with, and not much more. */
        public val Acquaintance: RelationType = RelationType("acquaintance")

        /** Someone who may act on behalf of the entity. */
        public val Agent: RelationType = RelationType("agent")

        /** The entity's child, by birth or adoption. */
        public val Child: RelationType = RelationType("child")

        /** Someone who shares the entity's street address. */
        public val CoResident: RelationType = RelationType("co-resident")

        /** Someone who works with the entity or at the same organization. */
        public val CoWorker: RelationType = RelationType("co-worker")

        /** Someone in the same field of study or activity. */
        public val Colleague: RelationType = RelationType("colleague")

        /** Someone the entity knows how to reach. */
        public val Contact: RelationType = RelationType("contact")

        /** Someone the entity has a crush on. */
        public val Crush: RelationType = RelationType("crush")

        /** Someone the entity is dating. */
        public val Date: RelationType = RelationType("date")

        /** The entity's emergency contact. */
        public val Emergency: RelationType = RelationType("emergency")

        /** A friend of the entity. */
        public val Friend: RelationType = RelationType("friend")

        /** A relative, or someone the entity counts as extended family. */
        public val Kin: RelationType = RelationType("kin")

        /** The entity itself, described by another Card. */
        public val Me: RelationType = RelationType("me")

        /** Someone the entity has met in person. */
        public val Met: RelationType = RelationType("met")

        /** Someone who inspires the entity. */
        public val Muse: RelationType = RelationType("muse")

        /** Someone who lives near the entity. */
        public val Neighbor: RelationType = RelationType("neighbor")

        /** The entity's parent. */
        public val Parent: RelationType = RelationType("parent")

        /** Someone who shares a parent with the entity. */
        public val Sibling: RelationType = RelationType("sibling")

        /** The entity's spouse. */
        public val Spouse: RelationType = RelationType("spouse")

        /** Someone the entity is intimate with and committed to. */
        public val Sweetheart: RelationType = RelationType("sweetheart")
    }
}
