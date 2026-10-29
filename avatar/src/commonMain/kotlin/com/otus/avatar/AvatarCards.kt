package com.otus.avatar

import com.otus.jscontact.Card
import kotlinx.serialization.json.JsonObject

/** The `media` key under which Otus keeps the photo of one of the user's own avatars. */
public const val AVATAR_PHOTO_KEY: String = "otus"

/** The JSContact property that carries a card's vCard properties, such as its avatar style. */
internal const val VCARD_PROPERTY = "vCard"

/** The key in a `vCard` value that lists its properties. */
internal const val VCARD_PROPERTIES = "properties"

/** The avatar style Otus keeps on this card, or null. It shows only when the card was read with `vCard`. */
public val Card.avatarStyle: AvatarStyle? get() = avatarStyleIn(extra[VCARD_PROPERTY])

/**
 * Whether this card holds one of the user's own avatars, as the avatar settings save them: a
 * style, or a photo under [AVATAR_PHOTO_KEY]. For its addresses it wins over every other card.
 */
public val Card.isAvatarCard: Boolean get() = avatarStyle != null || AVATAR_PHOTO_KEY in media.orEmpty()

/** This card without an avatar style, keeping its other vCard properties. */
public fun Card.withoutAvatarStyle(): Card = withAvatarStyle(null)

/** This card with [style] as its avatar style, or none when it is null, keeping its other vCard properties. */
public fun Card.withAvatarStyle(style: AvatarStyle?): Card {
    val vCard = extra[VCARD_PROPERTY]
    if (style == null && !hasAvatarStyle(vCard)) return this
    val properties = avatarPropertiesWith(vCard, style)
    val withProperties = JsonObject((vCard as? JsonObject).orEmpty() + (VCARD_PROPERTIES to properties))
    return copy(extra = extra + (VCARD_PROPERTY to withProperties))
}
