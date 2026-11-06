package com.otus.jmap.contacts

import com.otus.jscontact.Card
import com.otus.jscontact.toJsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * The `update` PatchObject of `ContactCard/set` (RFC 8620, section 5.3) that turns [before] into
 * [after]: it sets each top-level property that differs and removes the ones [after] dropped.
 * Whole properties keep the patch valid on any server, since a path into a property needs its
 * parent to exist there. Compare cards read with the same `properties`, so that a property left
 * out of the read does not count as dropped.
 */
public fun cardPatch(
    before: Card,
    after: Card,
): JsonObject {
    val old = before.toJsonObject()
    val new = after.toJsonObject()
    return JsonObject(
        (old.keys + new.keys)
            .filter { it != "id" && old[it] != new[it] }
            .associateWith { new[it] ?: JsonNull },
    )
}
