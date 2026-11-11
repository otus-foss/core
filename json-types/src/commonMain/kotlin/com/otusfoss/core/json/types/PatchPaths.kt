package com.otusfoss.core.json.types

/**
 * Joins [segments] into one PatchObject key: a JSON Pointer without its leading `/` (RFC 8620,
 * section 5.3, which RFC 9553, section 1.4.3, reuses). Each segment escapes `~` as `~0` and then
 * `/` as `~1` (RFC 6901, section 3), so `patchPath("keywords", "a/b~c")` is `keywords/a~1b~0c`.
 */
public fun patchPath(vararg segments: String): String {
    require(segments.isNotEmpty()) { "a patch path names at least one property" }
    // Escaping `~` first keeps the `~1` written for `/` from being escaped again.
    return segments.joinToString("/") { it.replace("~", "~0").replace("/", "~1") }
}

/**
 * The unescaped reference tokens of the PatchObject key [key], the reverse of [patchPath] (RFC
 * 6901, sections 3 and 4). Throws [IllegalArgumentException] for a `~` followed by neither 0 nor 1.
 */
public fun patchTokens(key: String): List<String> {
    require(!BAD_ESCAPE.containsMatchIn(key)) { "patch path \"$key\" has a \"~\" not followed by 0 or 1 (RFC 6901)" }
    // RFC 6901, section 4, decodes "~1" before "~0", so "~01" becomes "~1" and not "/".
    return key.split('/').map { it.replace("~1", "/").replace("~0", "~") }
}

/**
 * The first of [keys] that is a prefix of another one, or null. A PatchObject with such a pair is
 * invalid (RFC 8620, section 5.3), because one entry would change the other's parent.
 */
public fun overlappingPatchPath(keys: Collection<String>): String? {
    val tokens = keys.associateWith(::patchTokens)
    val parents =
        tokens.values
            .flatMap { path -> (1 until path.size).map { path.subList(0, it) } }
            .toSet()
    return tokens.entries.firstOrNull { it.value in parents }?.key
}

private val BAD_ESCAPE = Regex("~(?![01])")
