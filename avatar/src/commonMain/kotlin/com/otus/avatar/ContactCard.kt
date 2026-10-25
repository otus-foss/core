package com.otus.avatar

import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** RFC 9610 ContactCard and RFC 9553 JSContact fields used for avatar matching. */
internal val ContactCardType = JmapType("ContactCard", JmapCapability.CONTACTS, ContactCard.serializer())
internal val AddressBookType = JmapType("AddressBook", JmapCapability.CONTACTS, AvatarAddressBook.serializer())

@Serializable
internal data class ContactCard(
    val id: String,
    val addressBookIds: Map<String, Boolean> = emptyMap(),
    val emails: Map<String, ContactEmail> = emptyMap(),
    val media: Map<String, ContactMedia> = emptyMap(),
    @SerialName("vCard") private val vCard: JsonElement? = null,
) {
    val style: AvatarStyle? get() = avatarStyleIn(vCard)

    /** Whether Otus wrote this card's avatar: a style, or the photo under [OWN_PHOTO_KEY]. */
    val isOtusAvatar: Boolean get() = style != null || OWN_PHOTO_KEY in media

    fun isIn(books: Set<String>): Boolean = addressBookIds.any { it.value && it.key in books }
}

/** The `media` key of the photo [JmapOwnAvatars] saves. */
internal const val OWN_PHOTO_KEY = "otus"

@Serializable
internal data class ContactEmail(
    val address: String,
)

@Serializable
internal data class ContactMedia(
    val kind: String,
    val blobId: String? = null,
    val uri: String? = null,
    val mediaType: String? = null,
    val pref: Int? = null,
)

@Serializable
internal data class AvatarAddressBook(
    val id: String,
    val isSubscribed: Boolean = true,
    val myRights: AvatarAddressBookRights,
)

@Serializable
internal data class AvatarAddressBookRights(
    val mayRead: Boolean = false,
    val mayWrite: Boolean = false,
)
