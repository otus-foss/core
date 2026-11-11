package com.otusfoss.core.jmap.contacts

import com.otusfoss.core.jscontact.Card
import com.otusfoss.core.jscontact.toJsonObject
import com.otusfoss.core.json.types.patchPath
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * The `update` PatchObject of `ContactCard/set` (RFC 8620, section 5.3) that turns [before] into
 * [after]. A property that is an object on both sides, such as `media` or `emails`, changes one
 * entry at a time (`media/k1`), so entries this change did not touch are never sent again: some
 * servers refuse to take back what they return, such as Stalwart with a `blobId` in `media`. Any
 * other property that differs is set whole, or removed when [after] dropped it. Compare cards read
 * with the same `properties`, so that a property left out of the read does not count as dropped.
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
            .flatMap { key -> propertyPatch(key, old[key], new[key]).entries }
            .associate { it.toPair() },
    )
}

private fun propertyPatch(
    key: String,
    old: JsonElement?,
    new: JsonElement?,
): Map<String, JsonElement> =
    if (old is JsonObject && new is JsonObject) {
        (old.keys + new.keys)
            .filter { old[it] != new[it] }
            .associate { entry -> patchPath(key, entry) to (new[entry] ?: JsonNull) }
    } else {
        mapOf(key to (new ?: JsonNull))
    }
