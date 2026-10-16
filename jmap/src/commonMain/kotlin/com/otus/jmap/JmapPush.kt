package com.otus.jmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

/**
 * A push subscription (RFC 8620, section 7.2). It belongs to the user, not to an account. The
 * server never returns the subscription's URL or keys.
 */
@Serializable
public data class PushSubscription(
    val id: String,
    val deviceClientId: String,
    /** Null until the client sends back the code from [PushMessage.Verification]. */
    val verificationCode: String? = null,
    /** The UTCDate after which the server deletes the subscription. */
    val expires: String? = null,
    /** The data type names pushed, or null for all types. */
    val types: List<String>? = null,
)

/** Web Push encryption keys (RFC 8291), base64url-encoded, so the push service sees only ciphertext. */
@Serializable
public data class PushKeys(
    /** The P-256 public key in uncompressed form. */
    val p256dh: String,
    /** The 16-byte authentication secret. */
    val auth: String,
)

/**
 * A subscription to create. The server POSTs each [PushMessage] to [url]. [expires] is a UTCDate;
 * the server may shorten it. Null [types] asks for every data type.
 */
@Serializable
public data class NewPushSubscription(
    val deviceClientId: String,
    val url: String,
    val keys: PushKeys? = null,
    val expires: String? = null,
    val types: List<String>? = null,
) {
    init {
        require(allows(url)) { "push URLs must use https" }
    }

    public companion object {
        /** Whether a server may push to [url]; RFC 8620, section 7.2 allows only https. */
        public fun allows(url: String): Boolean = url.startsWith("https://")
    }
}

/** `PushSubscription/get` (RFC 8620, section 7.2.1). Null [ids] returns all of the user's subscriptions. */
public class PushSubscriptionGetMethod(
    private val ids: List<String>? = null,
) : JmapMethod<GetResult<PushSubscription>>("PushSubscription/get", JmapCapability.CORE, accountId = null) {
    override fun arguments(): JsonObject = buildJsonObject { put("ids", ids?.let(::strings) ?: JsonNull) }

    override fun decode(arguments: JsonObject): GetResult<PushSubscription> {
        val dto = JmapJson.decodeFromJsonElement(GetDto.serializer(), arguments)
        return GetResult(
            state = "",
            list = JmapJson.decodeFromJsonElement(ListSerializer(PushSubscription.serializer()), dto.list),
            notFound = dto.notFound,
        )
    }
}

/**
 * `PushSubscription/set` (RFC 8620, section 7.2.2). Only `verificationCode`, `expires`, and
 * `types` can be patched; the URL and keys are fixed at creation.
 */
public class PushSubscriptionSetMethod(
    private val create: Map<String, NewPushSubscription> = emptyMap(),
    private val update: Map<String, JsonObject> = emptyMap(),
    private val destroy: List<String> = emptyList(),
) : JmapMethod<SetResult>("PushSubscription/set", JmapCapability.CORE, accountId = null) {
    override fun arguments(): JsonObject =
        buildJsonObject {
            if (create.isNotEmpty()) {
                put(
                    "create",
                    JsonObject(
                        create.mapValues { JmapJson.encodeToJsonElement(NewPushSubscription.serializer(), it.value) },
                    ),
                )
            }
            if (update.isNotEmpty()) put("update", JsonObject(update))
            if (destroy.isNotEmpty()) put("destroy", strings(destroy))
        }

    override fun decode(arguments: JsonObject): SetResult =
        decodeSetResult(arguments, create.keys, update.keys, destroy, hasState = false)
}

/** What a server pushes (RFC 8620, section 7), as a push body or an event stream's `data`. */
public sealed interface PushMessage {
    /** New states by account id, then by data type name. Fetch `/changes` since the old state. */
    @Serializable
    public data class StateChange(
        val changed: Map<String, Map<String, String>>,
    ) : PushMessage

    /** Proof that the URL reached this client. Send [verificationCode] back with `PushSubscription/set`. */
    @Serializable
    public data class Verification(
        val pushSubscriptionId: String,
        val verificationCode: String,
    ) : PushMessage

    public companion object {
        /** Reads a message, or null for an unknown `@type`. Bad JSON throws [JmapException.NotJmap]. */
        public fun decode(text: String): PushMessage? =
            jmapDecoding {
                val json = JmapJson.parseToJsonElement(text).jsonObject
                when ((json["@type"] as? JsonPrimitive)?.content) {
                    "StateChange" -> JmapJson.decodeFromJsonElement(StateChange.serializer(), json)
                    "PushVerification" -> JmapJson.decodeFromJsonElement(Verification.serializer(), json)
                    else -> null
                }
            }
    }
}
