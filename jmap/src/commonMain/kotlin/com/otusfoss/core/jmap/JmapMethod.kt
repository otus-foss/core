package com.otusfoss.core.jmap

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * One method a request can call (RFC 8620, section 3.2). [capability] is added to `using` and
 * checked against the session; [accountId], when set, is checked against that account.
 */
public abstract class JmapMethod<out T>(
    public val name: String,
    public val capability: String,
    public val accountId: String?,
) {
    /**
     * Every account the call reads or writes. The client checks [capability] in each before
     * sending, so a `/copy` cannot reach an account that lacks the type.
     */
    internal open val accountIds: List<String>
        get() = listOfNotNull(accountId)

    public abstract fun arguments(): JsonObject

    /**
     * Decodes response arguments. Use serializers, checked JSON accessors, and `require` for
     * validation; [jmapDecoding] normalizes their failures. Other programming errors propagate.
     */
    public abstract fun decode(arguments: JsonObject): T
}

/** A data type such as `Mailbox`, `Email`, or `CalendarEvent`, with the serializer for its objects. */
public class JmapType<T>(
    public val name: String,
    public val capability: String,
    public val serializer: KSerializer<T>,
)

/** A call within a request. Use [resultOf] to feed its result into a later call (section 3.7). */
public class JmapCall<out T> internal constructor(
    public val method: JmapMethod<T>,
    internal val id: String,
) {
    public fun resultOf(path: String): JmapReference = JmapReference(id, method.name, path)
}

/** A result reference (RFC 8620, section 3.7). */
public class JmapReference internal constructor(
    private val resultOf: String,
    private val name: String,
    private val path: String,
) {
    internal fun toJson(): JsonObject =
        buildJsonObject {
            put("resultOf", JsonPrimitive(resultOf))
            put("name", JsonPrimitive(name))
            put("path", JsonPrimitive(path))
        }
}

/** Collects the calls of one request, in order. */
public class JmapRequestBuilder internal constructor() {
    internal val calls: MutableList<JmapCall<*>> = mutableListOf()

    public fun <T> call(method: JmapMethod<T>): JmapCall<T> {
        val call = JmapCall(method, "c${calls.size}")
        calls += call
        return call
    }
}

/** Which objects a `/get` returns. */
public sealed class JmapIds {
    public data object All : JmapIds()

    public data class Of(
        val ids: List<String>,
    ) : JmapIds()

    public data class Result(
        val reference: JmapReference,
    ) : JmapIds()
}

internal fun strings(values: List<String>): JsonArray = JsonArray(values.map { JsonPrimitive(it) })

/**
 * Type-specific arguments cannot replace the ones a method writes itself, so `extra` cannot point
 * a call at an account other than the one its capability was checked for. `#name` result
 * references count as `name`.
 */
internal fun requireExtraArguments(
    extra: Map<String, JsonElement>,
    standard: Set<String>,
) {
    val clashes = extra.keys.filter { it.removePrefix("#") in standard }
    require(clashes.isEmpty()) { "extra arguments replace standard ones: $clashes" }
}
