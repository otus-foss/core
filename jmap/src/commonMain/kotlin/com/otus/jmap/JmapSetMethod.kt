package com.otus.jmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * `Foo/set` (RFC 8620, section 5.3). Objects and patches are JSON, so a call sends only the
 * properties it changes. Refer to an object created earlier in the same request as `#creationId`.
 */
public class SetMethod(
    type: JmapType<*>,
    accountId: String,
    private val create: Map<String, JsonObject> = emptyMap(),
    /** Patch objects keyed by id, such as `{"name": "Receipts"}` or `{"mailboxIds/a": true}`. */
    private val update: Map<String, JsonObject> = emptyMap(),
    private val destroy: List<String> = emptyList(),
    /** When set, the server rejects the whole call with `stateMismatch` if its state differs. */
    private val ifInState: String? = null,
    /** Type-specific arguments. Must not name an argument this method writes itself. */
    private val extra: Map<String, JsonElement> = emptyMap(),
) : JmapMethod<SetResult>("${type.name}/set", type.capability, accountId) {
    init {
        requireExtraArguments(extra, setOf("accountId", "ifInState", "create", "update", "destroy"))
    }

    override fun arguments(): JsonObject =
        buildJsonObject {
            put("accountId", JsonPrimitive(accountId))
            ifInState?.let { put("ifInState", JsonPrimitive(it)) }
            if (create.isNotEmpty()) put("create", JsonObject(create))
            if (update.isNotEmpty()) put("update", JsonObject(update))
            if (destroy.isNotEmpty()) put("destroy", strings(destroy))
            extra.forEach { (key, value) -> put(key, value) }
        }

    override fun decode(arguments: JsonObject): SetResult =
        decodeSetResult(arguments, create.keys, update.keys, destroy, hasState = true)
}

/** Decodes a `/set` response. Types without state, such as `PushSubscription`, pass [hasState] false. */
internal fun decodeSetResult(
    arguments: JsonObject,
    creations: Set<String>,
    updates: Set<String>,
    destructions: List<String>,
    hasState: Boolean,
): SetResult {
    val dto = JmapJson.decodeFromJsonElement(SetDto.serializer(), arguments)
    require(!hasState || dto.newState != null) { "set response has no newState" }
    val result =
        SetResult(
            oldState = dto.oldState,
            newState = dto.newState.orEmpty(),
            created = dto.created.orEmpty(),
            updated = dto.updated.orEmpty(),
            destroyed = dto.destroyed.orEmpty(),
            notCreated = dto.notCreated.orEmpty(),
            notUpdated = dto.notUpdated.orEmpty(),
            notDestroyed = dto.notDestroyed.orEmpty(),
        )
    result.validateOutcomes(creations, updates, destructions)
    return result
}

/** The outcome of a `/set` call. Each object succeeds or fails on its own. */
public data class SetResult(
    val oldState: String?,
    /** Empty for `PushSubscription/set`, which has no state (RFC 8620, section 7.2.2). */
    val newState: String,
    /** Server-set properties of each created object, keyed by creation id; they include `id`. */
    val created: Map<String, JsonObject>,
    /** Updated ids, with any properties the server changed beyond the patch. */
    val updated: Map<String, JsonObject?>,
    val destroyed: List<String>,
    val notCreated: Map<String, SetError>,
    val notUpdated: Map<String, SetError>,
    val notDestroyed: Map<String, SetError>,
) {
    /** The id the server gave the object created as [creationId], or null when it was not created. */
    public fun createdId(creationId: String): String? = created.idCreatedAs(creationId)

    /** The first rejection, checking creations, then updates, then destructions. */
    public val firstError: SetError?
        get() = notCreated.values.firstOrNull() ?: notUpdated.values.firstOrNull() ?: notDestroyed.values.firstOrNull()
}

/** Why the server rejected one object (RFC 8620, section 5.3), such as `alreadyExists` or `forbidden`. */
@Serializable
public data class SetError(
    val type: String,
    val description: String? = null,
    /** The rejected properties, for `invalidProperties`. */
    val properties: List<String> = emptyList(),
    /** The conflicting object, for `alreadyExists`. */
    val existingId: String? = null,
)

@Serializable
private data class SetDto(
    val oldState: String? = null,
    val newState: String? = null,
    val created: Map<String, JsonObject>? = null,
    val updated: Map<String, JsonObject?>? = null,
    val destroyed: List<String>? = null,
    val notCreated: Map<String, SetError>? = null,
    val notUpdated: Map<String, SetError>? = null,
    val notDestroyed: Map<String, SetError>? = null,
)

/** Extra server-side updates are permitted, but each submitted operation needs one outcome. */
private fun SetResult.validateOutcomes(
    creations: Set<String>,
    updates: Set<String>,
    destructions: List<String>,
) {
    checkOutcomes(creations, created.keys, notCreated.keys)
    checkOutcomes(updates.mapNotNull(::resolveCreatedId), updated.keys, notUpdated.keys)
    checkOutcomes(destructions.mapNotNull(::resolveCreatedId), destroyed.toSet(), notDestroyed.keys)
    if (created.keys.any { createdId(it) == null }) throw JmapException.NotJmap("created object has no id")
}

/** The server id in a `/set` or `/copy` `created` entry; both report it as the entry's `id`. */
internal fun Map<String, JsonObject>.idCreatedAs(creationId: String): String? =
    get(creationId)?.string("id")

private fun SetResult.resolveCreatedId(id: String): String? =
    // A creation reference from an earlier method may resolve to an id unknown to this method.
    if (id.startsWith('#')) createdId(id.drop(1)) else id

private fun checkOutcomes(
    requested: Collection<String>,
    succeeded: Set<String>,
    failed: Set<String>,
) {
    if (succeeded.any { it in failed } || requested.any { it !in succeeded && it !in failed }) {
        throw JmapException.NotJmap("missing or conflicting set outcome")
    }
}
