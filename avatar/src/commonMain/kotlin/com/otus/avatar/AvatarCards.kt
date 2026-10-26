package com.otus.avatar

import com.otus.jscontact.Card
import kotlinx.serialization.json.JsonObject

/** The `media` key under which Otus keeps the photo of one of the user's own avatars. */
public const val AVATAR_PHOTO_KEY: String = "otus"

/** The JSContact property that carries a card's vCard properties, such as its avatar style. */
internal const val VCARD_PROPERTY = "vCard"

/** The avatar style Otus keeps on this card, or null. It shows only when the card was read with `vCard`. */
public val Card.avatarStyle: AvatarStyle? get() = avatarStyleIn(extra[VCARD_PROPERTY])

/**
 * Whether this card holds one of the user's own avatars, as the avatar settings save them: a
 * style, or a photo under [AVATAR_PHOTO_KEY]. For its addresses it wins over every other card.
 */
public val Card.isAvatarCard: Boolean get() = avatarStyle != null || AVATAR_PHOTO_KEY in media.orEmpty()

/** This card without an avatar style, keeping its other vCard properties. */
public fun Card.withoutAvatarStyle(): Card {
    val vCard = extra[VCARD_PROPERTY]
    if (!hasAvatarStyle(vCard)) return this
    val properties = avatarPropertiesWith(vCard, null)
    return copy(
        extra =
            extra + (VCARD_PROPERTY to JsonObject((vCard as? JsonObject).orEmpty() + ("properties" to properties))),
    )
}
