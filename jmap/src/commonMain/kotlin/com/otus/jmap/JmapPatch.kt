package com.otus.jmap

import com.otus.json.types.overlappingPatchPath
import com.otus.json.types.patchPath
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * Builds a PatchObject (RFC 8620, section 5.3) for a `/set` update, so a call sends only the
 * properties it changes:
 *
 * ```
 * jmapPatch {
 *     set(JsonPrimitive(true), "keywords", "chopin")
 *     remove("keywords", "mozart")
 * }
 * ```
 */
public fun jmapPatch(build: JmapPatchBuilder.() -> Unit): JsonObject = JmapPatchBuilder().apply(build).toJson()

/**
 * Collects the entries of one PatchObject (RFC 8620, section 5.3). Each path goes through
 * [com.otus.json.types.patchPath]. A later value for the same path replaces the earlier one. A path that is a prefix
 * of another would make the server reject the patch with `invalidPatch`, so it throws
 * [IllegalArgumentException] here. Never point into an array; set the whole array instead.
 */
public class JmapPatchBuilder internal constructor() {
    private val entries = linkedMapOf<String, JsonElement>()

    /** Sets the property at [path], such as `"name", "full"`, to [value]. Its parents must exist. */
    public fun set(
        value: JsonElement,
        vararg path: String,
    ) {
        put(patchPath(*path), value)
    }

    /** Removes the property at [path], or resets it to its default when the type defines one. */
    public fun remove(vararg path: String) {
        put(patchPath(*path), JsonNull)
    }

    /** Adds every entry of [patch], whose keys are already escaped paths, such as another patch's. */
    public fun include(patch: JsonObject) {
        patch.forEach { (key, value) -> put(key, value) }
    }

    internal fun toJson(): JsonObject = JsonObject(entries.toMap())

    private fun put(
        key: String,
        value: JsonElement,
    ) {
        val overlap = overlappingPatchPath(entries.keys + key)
        require(overlap == null) { "patch path $overlap overlaps another path of the patch" }
        entries[key] = value
    }
}
