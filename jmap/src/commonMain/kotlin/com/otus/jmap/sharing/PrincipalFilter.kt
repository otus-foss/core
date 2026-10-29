package com.otus.jmap.sharing

import com.otus.jmap.JmapFilter
import com.otus.jmap.strings
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * A `Principal/query` FilterCondition (RFC 9670, section 2.4.1). Null properties are left out, and a
 * Principal matches only when every given property matches. [email], [name], and [text] match
 * substrings. [type] and [timeZone] match exactly.
 */
public data class PrincipalFilter(
    /** Matches a Principal whose `accounts` has any of these account ids. */
    val accountIds: List<String>? = null,
    val email: String? = null,
    val name: String? = null,
    /** Matches the Principal's `name`, `email`, or `description`. */
    val text: String? = null,
    val type: PrincipalKind? = null,
    val timeZone: String? = null,
) {
    /** The condition as a filter, to send alone or to combine with other filters (RFC 8620, section 5.5). */
    public fun toFilter(): JmapFilter =
        JmapFilter.Condition(
            buildJsonObject {
                if (accountIds != null) put("accountIds", strings(accountIds))
                if (email != null) put("email", JsonPrimitive(email))
                if (name != null) put("name", JsonPrimitive(name))
                if (text != null) put("text", JsonPrimitive(text))
                if (type != null) put("type", JsonPrimitive(type.value))
                if (timeZone != null) put("timeZone", JsonPrimitive(timeZone))
            },
        )
}
