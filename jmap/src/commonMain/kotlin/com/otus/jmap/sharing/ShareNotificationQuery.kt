package com.otus.jmap.sharing

import com.otus.jmap.JmapFilter
import com.otus.jmap.JmapJson
import com.otus.jmap.JmapSort
import com.otus.jmap.UtcDateSerializer
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Instant

/**
 * A `ShareNotification/query` FilterCondition (RFC 9670, section 3.4.1). Null properties are left
 * out, and a notification matches only when every given property matches. [after] includes its
 * instant and [before] excludes it, so adjacent ranges never overlap.
 */
public data class ShareNotificationFilter(
    /** Matches notifications created at or after this instant. */
    val after: Instant? = null,
    /** Matches notifications created before this instant. */
    val before: Instant? = null,
    /** Matches this data type name exactly, such as `AddressBook`. */
    val objectType: String? = null,
    /** Matches this account id exactly. */
    val objectAccountId: String? = null,
) {
    /** The condition as a filter, to send alone or to combine with other filters (RFC 8620, section 5.5). */
    public fun toFilter(): JmapFilter =
        JmapFilter.Condition(
            buildJsonObject {
                after?.let { put("after", JmapJson.encodeToJsonElement(UtcDateSerializer, it)) }
                before?.let { put("before", JmapJson.encodeToJsonElement(UtcDateSerializer, it)) }
                objectType?.let { put("objectType", JsonPrimitive(it)) }
                objectAccountId?.let { put("objectAccountId", JsonPrimitive(it)) }
            },
        )
}

/** Comparators for `ShareNotification/query` (RFC 9670, section 3.4.2). */
public object ShareNotificationSort {
    /** By creation time, the one sort property every server MUST support (RFC 9670, section 3.4.2). */
    public fun created(isAscending: Boolean = true): JmapSort =
        JmapSort(property = "created", isAscending = isAscending)
}
