package com.otus.jscontact

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Finds the localization for [language] and applies it, see [Card.localized]. An exact key wins over one
 * that differs in case, since language tags are case-insensitive (RFC 5646, section 2.1.1).
 */
internal fun Card.localizedTo(language: String): Card {
    val tags = localizations.orEmpty()
    val tag =
        tags.keys.firstOrNull { it == language }
            ?: tags.keys.firstOrNull { it.equals(language, ignoreCase = true) }
    return tag?.let { localizedWith(it, tags.getValue(it)) } ?: this
}

/**
 * Applies [patch], the localization for [tag], to a copy of this Card without `localizations`, and sets
 * [tag] as the copy's language (RFC 9553, section 2.7.1). The copy goes through JSON because patch paths
 * are JSON Pointers. Throws [IllegalArgumentException] for a patch that is invalid (section 1.4.3), that
 * targets `localizations`, that removes `@type` or `version`, or whose result does not decode.
 */
internal fun Card.localizedWith(
    tag: String,
    patch: JsonObject,
): Card {
    require(patch.keys.none { it.substringBefore('/') == LOCALIZATIONS }) {
        "The \"$tag\" localization patches localizations (RFC 9553, section 2.7.1)"
    }
    require(FILLED_IN_BY_DECODING.none { patch[it] is JsonNull }) {
        "The \"$tag\" localization removes a mandatory property (RFC 9553, sections 1.4.3 and 2.1)"
    }
    val copy = JsContactJson.encodeToJsonElement(Card.serializer(), this).jsonObject - LOCALIZATIONS
    return JsContactJson
        .decodeFromJsonElement(Card.serializer(), JsContactPatch.apply(JsonObject(copy), patch))
        .copy(language = tag)
}

private const val LOCALIZATIONS = "localizations"

/**
 * The mandatory Card members that decoding fills in when they are absent (RFC 9553, sections 2.1.1 and 2.1.2).
 * Rule 4 of section 1.4.3 allows a null patch only on an optional property, and the localized Card cannot show
 * that one of these was removed, so the patch itself is checked.
 */
private val FILLED_IN_BY_DECODING = setOf(TYPE_PROPERTY, "version")
