package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

/**
 * An email address of the entity, an entry of the Card `emails` map (RFC 9553, section 2.3.1).
 * A lower [pref] ranks the address higher among the Card's addresses, from 1 to 100, and a missing
 * one ranks last (section 1.5.3).
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = EmailAddress.Serializer::class)
public data class EmailAddress(
    /** An RFC 5322 addr-spec (section 3.4.1), such as `jane_doe@example.com`. */
    val address: String,
    val contexts: Map<ContactContext, Boolean>? = null,
    val pref: Int? = null,
    val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<EmailAddress>(
        generatedSerializer(),
        "EmailAddress",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * A messaging service, social media profile, or other online presence of the entity, an entry of
 * the Card `onlineServices` map (RFC 9553, section 2.3.2). A valid service sets [uri] or [user].
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = OnlineService.Serializer::class)
public data class OnlineService(
    /** The service or protocol name, such as `Mastodon`. Names that match ignoring case are equal. */
    val service: String? = null,
    /** The entity's identifier at the service, an RFC 3986 URI such as `xmpp:alice@example.com`. */
    val uri: String? = null,
    /** The entity's free-text name at the service, such as `@alice@example2.com`. */
    val user: String? = null,
    val contexts: Map<ContactContext, Boolean>? = null,
    val pref: Int? = null,
    val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<OnlineService>(
        generatedSerializer(),
        "OnlineService",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * A language to contact the entity in, an entry of the Card `preferredLanguages` map (RFC 9553,
 * section 2.3.4). It has no label.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = LanguagePref.Serializer::class)
public data class LanguagePref(
    /** An RFC 5646 language tag, such as `en` or `fr`. */
    val language: String,
    val contexts: Map<ContactContext, Boolean>? = null,
    /** Ranks the language only among languages with the same [contexts], from 1 (most preferred) to 100. */
    val pref: Int? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<LanguagePref>(
        generatedSerializer(),
        "LanguagePref",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}
