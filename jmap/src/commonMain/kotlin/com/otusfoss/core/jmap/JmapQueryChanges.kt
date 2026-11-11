package com.otusfoss.core.jmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * `Foo/queryChanges` (RFC 8620, section 5.6) updates a cached `/query` result. Pass the [filter] and
 * [sort] of the `/query` that returned [sinceQueryState]. [extra] carries other arguments, such as
 * `upToId` or `calculateTotal`, and must not name the ones this method writes. The errors
 * `cannotCalculateChanges` and `tooManyChanges` arrive as [JmapException.MethodError]: run the
 * `/query` again, or retry `tooManyChanges` with a higher [maxChanges].
 */
public class QueryChangesMethod(
    type: JmapType<*>,
    accountId: String,
    private val sinceQueryState: String,
    private val filter: JmapFilter? = null,
    private val sort: List<JmapSort> = emptyList(),
    private val maxChanges: Int? = null,
    private val extra: Map<String, JsonElement> = emptyMap(),
) : JmapMethod<QueryChangesResult>("${type.name}/queryChanges", type.capability, accountId) {
    init {
        requireExtraArguments(extra, setOf("accountId", "filter", "sort", "sinceQueryState", "maxChanges"))
    }

    override fun arguments(): JsonObject =
        buildJsonObject {
            put("accountId", JsonPrimitive(accountId))
            if (filter != null) put("filter", filter.toJson())
            if (sort.isNotEmpty()) put("sort", sortJson(sort))
            put("sinceQueryState", JsonPrimitive(sinceQueryState))
            if (maxChanges != null) put("maxChanges", JsonPrimitive(maxChanges))
            extra.forEach { (key, value) -> put(key, value) }
        }

    override fun decode(arguments: JsonObject): QueryChangesResult {
        val dto = JmapJson.decodeFromJsonElement(QueryChangesDto.serializer(), arguments)
        return QueryChangesResult(
            oldQueryState = dto.oldQueryState,
            newQueryState = dto.newQueryState,
            total = dto.total,
            removed = dto.removed.orEmpty(),
            added = dto.added.orEmpty(),
        )
    }
}

/**
 * How a query's results changed (RFC 8620, section 5.6). Splice every cached id in [removed] out,
 * then splice each of [added] in at its index, lowest first, then resize the cache to [total]. An
 * id in both lists moved.
 */
public data class QueryChangesResult(
    val oldQueryState: String,
    val newQueryState: String,
    /** Present only when the request set `calculateTotal`. */
    val total: Long? = null,
    /** Ids no longer in the results. It may name ids the client never cached; skip those. */
    val removed: List<String> = emptyList(),
    /** Ids new to the results, in index order. */
    val added: List<AddedItem> = emptyList(),
) {
    init {
        require(total == null || total in 0..MAX_JMAP_UNSIGNED_INT) { "Invalid query total" }
        // Splicing in order only rebuilds the new results when the server sorted them (section 5.6).
        require(added.zipWithNext().all { (before, after) -> before.index < after.index }) { "added out of order" }
    }
}

/** An id that joined a query's results at [index] in the new results (RFC 8620, section 5.6). */
@Serializable
public data class AddedItem(
    val id: String,
    val index: Long,
) {
    init {
        require(index in 0..MAX_JMAP_UNSIGNED_INT) { "Invalid added index" }
    }
}

/** RFC 8620's own example (section 5.7) answers `"added": null`, so both lists accept null. */
@Serializable
private class QueryChangesDto(
    val oldQueryState: String,
    val newQueryState: String,
    val total: Long? = null,
    val removed: List<String>? = null,
    val added: List<AddedItem>? = null,
)
