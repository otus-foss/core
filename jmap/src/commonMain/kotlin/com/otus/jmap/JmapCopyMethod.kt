package com.otus.jmap

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * `Foo/copy` (RFC 8620, section 5.4) copies objects from [fromAccountId] into [accountId], the only
 * way to move objects between accounts. Each object in [create] names its source object's `id` and
 * any properties the copy changes. The client checks the type's capability in both accounts before
 * sending.
 *
 * With [onSuccessDestroyOriginal], the server then destroys each copied original through an implicit
 * `Foo/set` in the from account. That response shares this call's id: read it through
 * [JmapResponse.responses] and decode it with a [SetMethod] that destroys the copied source ids.
 */
public class CopyMethod(
    type: JmapType<*>,
    public val fromAccountId: String,
    accountId: String,
    private val create: Map<String, JsonObject>,
    private val onSuccessDestroyOriginal: Boolean = false,
    /** When set, the server rejects the whole call with `stateMismatch` if the from account's state differs. */
    private val ifFromInState: String? = null,
    /** When set, the server rejects the whole call with `stateMismatch` if the target account's state differs. */
    private val ifInState: String? = null,
) : JmapMethod<CopyResult>("${type.name}/copy", type.capability, accountId) {
    private val toAccountId: String = accountId

    override val accountIds: List<String> = listOf(fromAccountId, toAccountId)

    init {
        require(fromAccountId != toAccountId) { "a copy needs two different accounts" }
        require(create.values.all { it.string("id") != null }) { "each copy names its source object's id" }
    }

    override fun arguments(): JsonObject =
        buildJsonObject {
            put("fromAccountId", JsonPrimitive(fromAccountId))
            ifFromInState?.let { put("ifFromInState", JsonPrimitive(it)) }
            put("accountId", JsonPrimitive(toAccountId))
            ifInState?.let { put("ifInState", JsonPrimitive(it)) }
            put("create", JsonObject(create))
            if (onSuccessDestroyOriginal) put("onSuccessDestroyOriginal", JsonPrimitive(true))
        }

    override fun decode(arguments: JsonObject): CopyResult {
        // A copy response has the creation half of a set response, so it gets the same checks.
        val outcome = decodeSetResult(arguments, create.keys, emptySet(), emptyList(), hasState = true)
        return CopyResult(
            fromAccountId = fromAccountId,
            accountId = toAccountId,
            oldState = outcome.oldState,
            newState = outcome.newState,
            created = outcome.created,
            notCreated = outcome.notCreated,
        )
    }
}

/** The outcome of a `/copy` call (RFC 8620, section 5.4). Each copy succeeds or fails on its own. */
public data class CopyResult(
    val fromAccountId: String,
    val accountId: String,
    /** The target account's state before the copies, or null when the server does not know it. */
    val oldState: String?,
    /** The target account's state after the copies. */
    val newState: String,
    /** Server-set properties of each copy, keyed by creation id; they include the copy's new `id`. */
    val created: Map<String, JsonObject>,
    /** Why each rejected copy failed, such as `alreadyExists` with the existing object's id. */
    val notCreated: Map<String, SetError>,
) {
    /** The id of the copy made as [creationId], or null when it failed. It differs from the source id. */
    public fun createdId(creationId: String): String? = created.idCreatedAs(creationId)
}
