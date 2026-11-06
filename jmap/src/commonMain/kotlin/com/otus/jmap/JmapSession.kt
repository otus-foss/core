package com.otus.jmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull

/** Capability URIs this suite uses. Servers differ: always check [JmapSession.accountFor]. */
public object JmapCapability {
    public const val CORE: String = "urn:ietf:params:jmap:core"
    public const val MAIL: String = "urn:ietf:params:jmap:mail"
    public const val SUBMISSION: String = "urn:ietf:params:jmap:submission"
    public const val CALENDARS: String = "urn:ietf:params:jmap:calendars"
    public const val CONTACTS: String = "urn:ietf:params:jmap:contacts"

    /** Sieve script management (RFC 9661). */
    public const val SIEVE: String = "urn:ietf:params:jmap:sieve"

    /** The server signs Web Push messages with VAPID (RFC 9749). */
    public const val WEBPUSH_VAPID: String = "urn:ietf:params:jmap:webpush-vapid"
}

/**
 * The parts of the session resource (RFC 8620, section 2) this client uses. URLs are absolute and
 * already checked against the security policy and the trusted origins.
 */
public data class JmapSession(
    val sessionUrl: String,
    val apiUrl: String,
    val username: String,
    val capabilities: Set<String>,
    val accounts: Map<String, JmapAccount>,
    val primaryAccounts: Map<String, String>,
    val state: String,
    /** Origins that may receive credentials for this session. */
    val trustedOrigins: Set<String>,
    /** RFC 8620 level-1 URI template; expanded and checked only when a blob is requested. */
    val downloadUrl: String? = null,
    /** RFC 8620 level-1 URI template with `{accountId}`; expanded and checked only when uploading. */
    val uploadUrl: String? = null,
    /** Each session capability's object as the server sent it, such as the core limits. */
    val capabilityObjects: Map<String, JsonObject> = emptyMap(),
) {
    /** The most objects one `/get` call may return (section 2), if the server says. */
    public val maxObjectsInGet: Long?
        get() = capabilityObjects[JmapCapability.CORE]?.unsigned("maxObjectsInGet")

    /** The most objects one `/set` call may create, update, and destroy (section 2), if the server says. */
    public val maxObjectsInSet: Long?
        get() = capabilityObjects[JmapCapability.CORE]?.unsigned("maxObjectsInSet")

    /** The most method calls one request may hold (section 2), if the server says. */
    public val maxCallsInRequest: Long?
        get() = capabilityObjects[JmapCapability.CORE]?.unsigned("maxCallsInRequest")

    /** Objects per `/get` call, which also sizes a page of `/query` ids: [maxObjectsInGet] up to [BATCH]. */
    public val getBatch: Int get() = batch(maxObjectsInGet, BATCH)

    /** Objects per `/set` call: [maxObjectsInSet] up to [BATCH]. */
    public val setBatch: Int get() = batch(maxObjectsInSet, BATCH)

    /** Method calls per request: [maxCallsInRequest] up to [CALL_BATCH]. */
    public val callBatch: Int get() = batch(maxCallsInRequest, CALL_BATCH)

    /** The largest upload the server accepts, in octets (section 2), if the server says. */
    public val maxSizeUpload: Long?
        get() = capabilityObjects[JmapCapability.CORE]?.unsigned("maxSizeUpload")

    /**
     * The server's VAPID public key (RFC 9749), base64url-encoded P-256. Push services such as
     * Google's FCM accept pushes only from a server that signs them with this key.
     */
    public val vapidPublicKey: String?
        get() =
            capabilityObjects[JmapCapability.WEBPUSH_VAPID]?.string("applicationServerKey")?.takeIf { it.isNotEmpty() }

    /** The primary account for [capability], else the first account offering it, else null. */
    public fun accountFor(capability: String): String? =
        primaryAccounts[capability]?.takeIf { accounts[it]?.capabilities?.contains(capability) == true }
            ?: accounts.entries.firstOrNull { capability in it.value.capabilities }?.key
}

/**
 * [advertised], or [default] when the server names no limit or a larger one. RFC 8620 suggests that
 * servers allow at least 500 objects and 16 calls (section 2), so the defaults fit any of them and
 * keep each request small.
 */
private fun batch(
    advertised: Long?,
    default: Int,
): Int = advertised?.coerceIn(1L, default.toLong())?.toInt() ?: default

private const val BATCH = 256
private const val CALL_BATCH = 16

public data class JmapAccount(
    val name: String,
    val isPersonal: Boolean,
    val isReadOnly: Boolean,
    val capabilities: Set<String>,
    /** Each account capability's object, such as the mail limits or the supported Sieve extensions. */
    val capabilityObjects: Map<String, JsonObject> = emptyMap(),
)

/** Lenient decoding: servers add properties and capabilities over time. */
public val JmapJson: Json =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

@Serializable
internal data class SessionDto(
    val capabilities: JsonObject,
    val accounts: Map<String, AccountDto>,
    val primaryAccounts: Map<String, String> = emptyMap(),
    val username: String = "",
    val apiUrl: String,
    val state: String = "",
    val downloadUrl: String? = null,
    val uploadUrl: String? = null,
)

@Serializable
internal class AccountDto(
    val name: String = "",
    val isPersonal: Boolean = false,
    val isReadOnly: Boolean = false,
    val accountCapabilities: JsonObject = JsonObject(emptyMap()),
)

/** Capability values are objects (section 2); anything else is treated as an empty object. */
internal fun JsonObject.capabilityObjects(): Map<String, JsonObject> =
    mapValues { (_, value) -> value as? JsonObject ?: JsonObject(emptyMap()) }

private fun JsonObject.unsigned(key: String): Long? =
    (get(key) as? JsonPrimitive)?.takeUnless { it.isString }?.longOrNull?.takeIf { it >= 0 }
