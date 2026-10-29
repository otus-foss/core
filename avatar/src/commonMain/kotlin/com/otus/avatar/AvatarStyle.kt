package com.otus.avatar

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * An avatar the user built in Otus instead of a photo: a color and what sits on it. [color] is a
 * name each app maps to a theme color, such as `Blue`, or a custom `#RRGGBB`. The names in [mark]
 * are keys each app maps to its own symbols, so this type holds no UI values.
 */
public data class AvatarStyle(
    val color: String,
    val mark: AvatarMark,
)

/** What an [AvatarStyle] draws on its color. */
public sealed interface AvatarMark {
    /** Up to [MAX_AVATAR_LETTERS] letters, such as initials. */
    public data class Letters(
        val text: String,
    ) : AvatarMark

    public data class Icon(
        val name: String,
    ) : AvatarMark

    /**
     * The website icon of the address's domain instead of a mark. It shows even when the app has
     * website icons off, because the user chose it for their own address; the color is the
     * fallback while there is none.
     */
    public data object Website : AvatarMark
}

public const val MAX_AVATAR_LETTERS: Int = 3

/**
 * The vCard property that holds an [AvatarStyle] on a contact card, as an RFC 6350 extension
 * property. JSContact carries it in `vCard.properties` (RFC 9555, section 2.15), the form a server
 * that stores vCards, such as Stalwart, keeps; Stalwart rejects unknown top-level card properties.
 * Other contact apps keep the property but ignore it.
 */
public const val AVATAR_STYLE_PROPERTY: String = "x-otus-avatar"

/** The style in the `vCard` value of a card, or null when it holds none Otus can read. */
public fun avatarStyleIn(vCard: JsonElement?): AvatarStyle? =
    vCard
        .avatarProperties()
        .firstOrNull { it.isAvatarStyle() }
        ?.getOrNull(VALUE_INDEX)
        ?.let { (it as? JsonPrimitive)?.contentOrNull }
        ?.let(::decodedAvatarStyle)

/**
 * The `vCard.properties` of a card after setting [style] in [vCard], keeping its other properties.
 * A null [style] removes it.
 */
public fun avatarPropertiesWith(
    vCard: JsonElement?,
    style: AvatarStyle?,
): JsonArray =
    JsonArray(vCard.avatarProperties().filterNot { it.isAvatarStyle() } + listOfNotNull(style?.vCardProperty()))

/** This style as a jCard property: its name, no parameters, its value type, and its value. */
private fun AvatarStyle.vCardProperty(): JsonArray =
    JsonArray(
        listOf(
            JsonPrimitive(AVATAR_STYLE_PROPERTY),
            JsonObject(emptyMap()),
            JsonPrimitive("text"),
            JsonPrimitive(encoded()),
        ),
    )

/** Whether the `vCard` value of a card holds a style. */
public fun hasAvatarStyle(vCard: JsonElement?): Boolean = vCard.avatarProperties().any { it.isAvatarStyle() }

private fun JsonElement?.avatarProperties(): List<JsonArray> =
    ((this as? JsonObject)?.get(VCARD_PROPERTIES) as? JsonArray).orEmpty().filterIsInstance<JsonArray>()

private fun JsonArray.isAvatarStyle(): Boolean =
    (firstOrNull() as? JsonPrimitive)?.contentOrNull.equals(AVATAR_STYLE_PROPERTY, ignoreCase = true)

private const val VALUE_INDEX = 3

/** Letters keep their case but lose spaces, and stop at [MAX_AVATAR_LETTERS]. */
public fun avatarLetters(text: String): String = text.filterNot(Char::isWhitespace).take(MAX_AVATAR_LETTERS)

internal fun AvatarStyle.encoded(): String = StyleJson.encodeToString(AvatarStyleDto.serializer(), toDto())

internal fun decodedAvatarStyle(text: String): AvatarStyle? =
    runCatching { StyleJson.decodeFromString(AvatarStyleDto.serializer(), text) }.getOrNull()?.toStyle()

@Serializable
private data class AvatarStyleDto(
    val color: String,
    val letters: String? = null,
    val icon: String? = null,
    val website: Boolean? = null,
)

private fun AvatarStyle.toDto(): AvatarStyleDto =
    when (mark) {
        is AvatarMark.Letters -> AvatarStyleDto(color, letters = mark.text)
        is AvatarMark.Icon -> AvatarStyleDto(color, icon = mark.name)
        AvatarMark.Website -> AvatarStyleDto(color, website = true)
    }

private fun AvatarStyleDto.toStyle(): AvatarStyle? {
    val mark =
        when {
            website == true -> AvatarMark.Website
            letters != null -> avatarLetters(letters).takeIf { it.isNotEmpty() }?.let(AvatarMark::Letters)
            icon != null -> AvatarMark.Icon(icon)
            else -> null
        }
    return mark?.takeIf { color.isNotBlank() }?.let { AvatarStyle(color, it) }
}

private val StyleJson = Json { ignoreUnknownKeys = true }
