package com.otusfoss.core.jscontact

import com.otusfoss.core.json.types.overlappingPatchPath
import com.otusfoss.core.json.types.patchTokens
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * Applies a JSContact PatchObject (RFC 9553, section 1.4.3), the patch format of Card
 * localizations (section 2.7.1).
 */
public object JsContactPatch {
    /**
     * Returns [target] with every patch in [patch] applied. Each key is a JSON Pointer (RFC 6901)
     * without its leading "/", so `~0` stands for `~` and `~1` for `/` inside a reference token.
     * A null value removes the member, and does nothing when the member is absent. Any other value
     * adds the member or replaces it, including an existing array member.
     *
     * Throws [IllegalArgumentException] and applies nothing when any patch is invalid: a `~` that
     * starts neither `~0` nor `~1`, a parent that does not exist in [target], an array index that
     * is `-`, malformed, or past the end, null as the value of an array member, or two pointers
     * where one is a prefix of the other. Whether a value fits its property (rule 4) depends on the
     * object type, so callers validate the result.
     */
    public fun apply(
        target: JsonObject,
        patch: JsonObject,
    ): JsonObject {
        val overlapping = overlappingPatchPath(patch.keys)
        require(overlapping == null) { "Patch \"$overlapping\" is a prefix of another patch (RFC 9553, section 1.4.3)" }
        // Without overlaps no patch adds or removes another one's parent, so the order is irrelevant.
        return patch.keys.associateWith(::patchTokens).entries.fold(target) { result, (key, tokens) ->
            requireNotNull(result.patchedMember(tokens.first(), tokens.drop(1), patch.getValue(key))) {
                "Patch \"$key\" has no existing parent or array member to set (RFC 9553, section 1.4.3)"
            }
        }
    }
}

/** This element with [value] at [tokens], or null when the patch does not fit it. */
private fun JsonElement.patched(
    tokens: List<String>,
    value: JsonElement,
): JsonElement? =
    when (this) {
        is JsonObject -> patchedMember(tokens.first(), tokens.drop(1), value)
        is JsonArray -> patchedItem(tokens.first(), tokens.drop(1), value)
        else -> null
    }

private fun JsonObject.patchedMember(
    token: String,
    rest: List<String>,
    value: JsonElement,
): JsonObject? =
    when {
        rest.isNotEmpty() -> get(token)?.patched(rest, value)?.let { JsonObject(this + (token to it)) }
        value is JsonNull -> JsonObject(this - token)
        else -> JsonObject(this + (token to value))
    }

// Arrays only take replacements: no "-", no index past the end, and no null that would delete.
private fun JsonArray.patchedItem(
    token: String,
    rest: List<String>,
    value: JsonElement,
): JsonArray? {
    val index = token.takeIf { ARRAY_INDEX.matches(it) }?.toIntOrNull()?.takeIf { it in indices } ?: return null
    val item = if (rest.isEmpty()) value.takeIf { it !is JsonNull } else this[index].patched(rest, value)
    return item?.let { replacement -> JsonArray(mapIndexed { i, old -> if (i == index) replacement else old }) }
}

private val ARRAY_INDEX = Regex("0|[1-9][0-9]*")
