package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * A public key, certificate, or other cryptographic resource of the entity (RFC 9553, section 2.6.1). It has
 * only the Resource properties (section 1.4.4).
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = CryptoKey.Serializer::class)
public data class CryptoKey(
    override val uri: String,
    /** The kind of key. RFC 9553 registers no values for it, so any value written must be vendor-specific. */
    val kind: String? = null,
    override val mediaType: String? = null,
    override val contexts: Map<ContactContext, Boolean>? = null,
    override val pref: Int? = null,
    override val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) : ContactResource {
    internal object Serializer : JsContactSerializer<CryptoKey>(
        generatedSerializer(),
        "CryptoKey",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/** A directory that holds information about the entity (RFC 9553, section 2.6.2). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Directory.Serializer::class)
public data class Directory(
    /** Whether [uri] is a whole directory or the entity's own entry. Mandatory for a Directory. */
    val kind: DirectoryKind,
    override val uri: String,
    override val mediaType: String? = null,
    override val contexts: Map<ContactContext, Boolean>? = null,
    override val pref: Int? = null,
    override val label: String? = null,
    /**
     * The position among the Card's directories of the same [kind]. It must be above zero, and entries with
     * equal or missing values have no defined order (RFC 9553, section 2.6.2).
     */
    val listAs: Long? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) : ContactResource {
    internal object Serializer : JsContactSerializer<Directory>(
        generatedSerializer(),
        "Directory",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of a Directory (RFC 9553, section 2.6.2). Values outside the companion are vendor-specific and
 * must be `v-extension` names such as `example.com:foo` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class DirectoryKind(
    public val value: String,
) {
    /** The registered values (RFC 9553, section 3.7, Table 14). */
    public companion object {
        /** A directory service the entity belongs to, such as a company directory with its co-workers. */
        public val Directory: DirectoryKind = DirectoryKind("directory")

        /** The entity's own entry within a directory. */
        public val Entry: DirectoryKind = DirectoryKind("entry")
    }
}

/** A link to a resource that fits no other resource property (RFC 9553, section 2.6.3). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Link.Serializer::class)
public data class Link(
    override val uri: String,
    /** What the link is for. Optional for a Link. */
    val kind: LinkKind? = null,
    override val mediaType: String? = null,
    override val contexts: Map<ContactContext, Boolean>? = null,
    override val pref: Int? = null,
    override val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) : ContactResource {
    internal object Serializer : JsContactSerializer<Link>(
        generatedSerializer(),
        "Link",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of a Link (RFC 9553, section 2.6.3). Values outside the companion are vendor-specific and must be
 * `v-extension` names such as `example.com:foo` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class LinkKind(
    public val value: String,
) {
    /** The registered values (RFC 9553, section 3.7, Table 15). */
    public companion object {
        /** A URI that reaches the entity, including web forms that need user interaction. */
        public val Contact: LinkKind = LinkKind("contact")
    }
}
