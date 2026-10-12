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
            properties?.let { put("properties", strings(it)) }
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
    val state: String,
    val list: List<T>,
    val notFound: List<String>,
)

public data class JmapSort(
    val property: String,
    val isAscending: Boolean = true,
)

/**
 * `Foo/query` (RFC 8620, section 5.5). [filter] is type-specific. [extra] carries other arguments,
 * such as `position`, and must not name `accountId`, `filter`, `sort`, or `limit`.
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
            filter?.let { put("filter", it) }
            if (sort.isNotEmpty()) {
                put(
                    "sort",
                    JsonArray(
                        sort.map {
                            buildJsonObject {
                                put("property", JsonPrimitive(it.property))
                                put("isAscending", JsonPrimitive(it.isAscending))
                            }
                        },
                    ),
                )
            }
            limit?.let { put("limit", JsonPrimitive(it)) }
            extra.forEach { (key, value) -> put(key, value) }
        }

    override fun decode(arguments: JsonObject): QueryResult =
        JmapJson.decodeFromJsonElement(QueryResult.serializer(), arguments)
}

@Serializable
public data class QueryResult(
    val queryState: String = "",
    val ids: List<String>,
    val position: Long = 0,
    val total: Long? = null,
) {
    init {
        require(position in 0..MAX_JMAP_UNSIGNED_INT) { "Invalid query position" }
        require(total == null || total in 0..MAX_JMAP_UNSIGNED_INT) { "Invalid query total" }
    }
}

private const val MAX_JMAP_UNSIGNED_INT = 9_007_199_254_740_991L

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
            maxChanges?.let { put("maxChanges", JsonPrimitive(it)) }
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
private class GetDto(
    val state: String = "",
    val list: JsonArray,
    val notFound: List<String> = emptyList(),
)
