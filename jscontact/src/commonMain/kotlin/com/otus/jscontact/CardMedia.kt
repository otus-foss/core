package com.otus.jscontact

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * A photo, logo, or sound of the entity (RFC 9553, section 2.6.4). Over JMAP, a server should return
 * [blobId] in place of a `data:` [uri], and a client may send [blobId] in place of [uri] (RFC 9610,
 * section 3).
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Media.Serializer::class)
public data class Media(
    /** Mandatory for a Media. */
    val kind: MediaKind,
    /**
     * Empty when the server sent [blobId] in its place (RFC 9610, section 3). RFC 9553 makes `uri`
     * mandatory, but a JMAP server omits it then, so decoding allows its absence. An empty value is never
     * encoded.
     */
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    override val uri: String = "",
    /**
     * The blob with the binary contents, which the server's download URL serves. A server that returns it
     * must also set [mediaType]. To set a new photo, a client uploads the file and sends the blob id it gets
     * (RFC 9610, sections 3, 3.5, and 7.5.3).
     */
    val blobId: String? = null,
    override val mediaType: String? = null,
    override val contexts: Map<ContactContext, Boolean>? = null,
    override val pref: Int? = null,
    override val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) : ContactResource {
    internal object Serializer : JsContactSerializer<Media>(
        generatedSerializer(),
        "Media",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of a [Media] (RFC 9553, section 2.6.4). Values outside the companion are vendor-specific and must
 * be `v-extension` names such as `example.com:foo` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class MediaKind(
    public val value: String,
) {
    /** The registered values (RFC 9553, section 3.7, Table 16). */
    public companion object {
        /** A photograph or avatar. */
        public val Photo: MediaKind = MediaKind("photo")

        /** Audio, such as how to pronounce the entity's name. */
        public val Sound: MediaKind = MediaKind("sound")

        /** A graphic image or logo of the entity. */
        public val Logo: MediaKind = MediaKind("logo")
    }
}
