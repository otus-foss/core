package com.otus.jmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * `Foo/get` (RFC 8620, section 5.1). [extra] carries type-specific arguments and must not name
 * `accountId`, `ids`, or `properties`.
 */
public class GetMethod<T>(
    private val type: JmapType<T>,
    accountId: String,
    private val ids: JmapIds = JmapIds.All,
    private val properties: List<String>? = null,
    private val extra: Map<String, JsonElement> = emptyMap(),
) : JmapMethod<GetResult<T>>("${type.name}/get", type.capability, accountId) {
    init {
        requireExtraArguments(extra, setOf("accountId", "ids", "properties"))
    }

    override fun arguments(): JsonObject =
        buildJsonObject {
            put("accountId", JsonPrimitive(accountId))
            when (ids) {
                JmapIds.All -> put("ids", JsonNull)
                is JmapIds.Of -> put("ids", strings(ids.ids))
                is JmapIds.Result -> put("#ids", ids.reference.toJson())
            }
            if (properties != null) put("properties", strings(properties))
            extra.forEach { (key, value) -> put(key, value) }
        }

    override fun decode(arguments: JsonObject): GetResult<T> {
        val dto = JmapJson.decodeFromJsonElement(GetDto.serializer(), arguments)
        return GetResult(
            dto.state,
            JmapJson.decodeFromJsonElement(ListSerializer(type.serializer), dto.list),
            dto.notFound,
        )
    }
}

public data class GetResult<T>(
    /** Empty for `PushSubscription/get`, which has no state (RFC 8620, section 7.2.1). */
    val state: String,
    val list: List<T>,
    val notFound: List<String>,
)

/**
 * A Comparator (RFC 8620, section 5.5): sort by [property], reversed when [isAscending] is false.
 * [collation] names an RFC 4790 algorithm from the core capability's `collationAlgorithms`; null
 * lets the server choose, and only string properties use it.
 */
public data class JmapSort(
    val property: String,
    val isAscending: Boolean = true,
    val collation: String? = null,
)

/**
 * `Foo/query` (RFC 8620, section 5.5). [filter] is type-specific; build it with [JmapFilter]. [extra]
 * carries other arguments, such as `position`, and must not name `accountId`, `filter`, `sort`, or
 * `limit`.
 */
public class QueryMethod(
    type: JmapType<*>,
    accountId: String,
    private val filter: JsonObject? = null,
    private val sort: List<JmapSort> = emptyList(),
    private val limit: Int? = null,
    private val extra: Map<String, JsonElement> = emptyMap(),
) : JmapMethod<QueryResult>("${type.name}/query", type.capability, accountId) {
    init {
        requireExtraArguments(extra, setOf("accountId", "filter", "sort", "limit"))
    }

    override fun arguments(): JsonObject =
        buildJsonObject {
            put("accountId", JsonPrimitive(accountId))
            if (filter != null) put("filter", filter)
            if (sort.isNotEmpty()) put("sort", sortJson(sort))
            if (limit != null) put("limit", JsonPrimitive(limit))
            extra.forEach { (key, value) -> put(key, value) }
        }

    override fun decode(arguments: JsonObject): QueryResult =
        JmapJson.decodeFromJsonElement(QueryResult.serializer(), arguments)
}

/** A `/query` response (RFC 8620, section 5.5). [ids] start at [position] in the full results. */
@Serializable
public data class QueryResult(
    val queryState: String = "",
    val ids: List<String>,
    val position: Long = 0,
    /** Present only when the request set `calculateTotal`. */
    val total: Long? = null,
    /** Whether `/queryChanges` can follow this filter and sort; a later call may still fail. */
    val canCalculateChanges: Boolean = false,
    /** The limit the server applied, present only when it set or changed one. Page by this size. */
    val limit: Long? = null,
) {
    init {
        require(position in 0..MAX_JMAP_UNSIGNED_INT) { "Invalid query position" }
        require(total == null || total in 0..MAX_JMAP_UNSIGNED_INT) { "Invalid query total" }
        require(limit == null || limit in 0..MAX_JMAP_UNSIGNED_INT) { "Invalid query limit" }
    }
}

/** Comparators as `/query` and `/queryChanges` send them; `collation` only when one is chosen. */
internal fun sortJson(sort: List<JmapSort>): JsonArray =
    JsonArray(
        sort.map { comparator ->
            buildJsonObject {
                put("property", JsonPrimitive(comparator.property))
                put("isAscending", JsonPrimitive(comparator.isAscending))
                val collation = comparator.collation
                if (collation != null) put("collation", JsonPrimitive(collation))
            }
        },
    )

/** The largest UnsignedInt (RFC 8620, section 1.3): 2^53 - 1, the safe integer range of a double. */
internal const val MAX_JMAP_UNSIGNED_INT: Long = 9_007_199_254_740_991L

/** `Foo/changes` (RFC 8620, section 5.2), for incremental sync. */
public class ChangesMethod(
    type: JmapType<*>,
    accountId: String,
    private val sinceState: String,
    private val maxChanges: Int? = null,
) : JmapMethod<ChangesResult>("${type.name}/changes", type.capability, accountId) {
    override fun arguments(): JsonObject =
        buildJsonObject {
            put("accountId", JsonPrimitive(accountId))
            put("sinceState", JsonPrimitive(sinceState))
            if (maxChanges != null) put("maxChanges", JsonPrimitive(maxChanges))
        }

    override fun decode(arguments: JsonObject): ChangesResult =
        JmapJson.decodeFromJsonElement(ChangesResult.serializer(), arguments)
}

@Serializable
public data class ChangesResult(
    val oldState: String,
    val newState: String,
    val hasMoreChanges: Boolean,
    val created: List<String> = emptyList(),
    val updated: List<String> = emptyList(),
    val destroyed: List<String> = emptyList(),
)

@Serializable
internal class GetDto(
    val state: String = "",
    val list: JsonArray,
    val notFound: List<String> = emptyList(),
)
