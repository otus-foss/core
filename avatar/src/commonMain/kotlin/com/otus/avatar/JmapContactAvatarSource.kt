package com.otus.avatar

import com.otus.jmap.GetMethod
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapConnection
import com.otus.jmap.JmapConnections
import com.otus.jmap.contacts.AddressBook
import com.otus.jmap.contacts.AddressBookType
import com.otus.jscontact.Card
import com.otus.jscontact.Media
import com.otus.jscontact.MediaKind
import kotlin.io.encoding.Base64

/**
 * Uses standard contacts capabilities, plus the [AVATAR_STYLE_PROPERTY] Otus writes. Never searches
 * another saved login's address books. A Website style counts only in the user's own books: it
 * fetches the website icon even with website icons off, which a shared card must not turn on.
 */
public class JmapContactAvatarSource(
    private val connections: JmapConnections,
    private val images: AvatarImageLoader,
) : ContactAvatarSource {
    override suspend fun avatar(
        request: AvatarRequest,
        allowExternalImages: Boolean,
    ): ContactAvatar? =
        nullOnFailure {
            // Contacts may live in a separate JMAP account from mail, within the same login.
            val contacts = connections.account(request.accountKey, JmapCapability.CONTACTS) ?: return@nullOnFailure null
            val connection = contacts.connection
            val account = contacts.id
            val books = readableBooks(connection, account)
            if (books.isEmpty()) return@nullOnFailure null
            val own = books.filter { it.myRights.mayWrite }.map { it.id }.toSet()
            findContactAvatar(connection, account, request.email, books.map { it.id }.toSet(), own) { card ->
                card.avatarStyle
                    ?.takeIf { card.isIn(own) || it.mark != AvatarMark.Website }
                    ?.let(ContactAvatar::Styled)
                    ?: loadCard(connection, account, card, allowExternalImages)
            }
        }

    // Servers such as Stalwart may leave the user's own books unsubscribed (RFC 9610, section 2).
    private suspend fun readableBooks(
        connection: JmapConnection,
        account: String,
    ): List<AddressBook> =
        connection
            .call(GetMethod(AddressBookType, account))
            .list
            .filter { it.myRights.mayRead && (it.isSubscribed || it.myRights.mayWrite) }

    private suspend fun loadCard(
        connection: JmapConnection,
        account: String,
        card: Card,
        external: Boolean,
    ): ContactAvatar.Photo? =
        card.media
            .orEmpty()
            .values
            .filter { it.kind == MediaKind.Photo || it.kind == MediaKind.Logo }
            .sortedWith(
                compareBy({ it.kind != MediaKind.Photo }, { it.pref ?: Int.MAX_VALUE }, { it.blobId ?: it.uri }),
            ).firstNotNullOfOrNull { photo ->
                avatarOrNull { load(connection, account, photo, external) }?.let {
                    ContactAvatar.Photo(it, photo.mediaType ?: DEFAULT_PHOTO_TYPE)
                }
            }

    private suspend fun load(
        connection: JmapConnection,
        account: String,
        media: Media,
        external: Boolean,
    ): AvatarImage? {
        val blobId = media.blobId
        if (blobId != null && media.mediaType in CONTACT_RASTER_TYPES) {
            val mediaType = requireNotNull(media.mediaType)
            val blob =
                connection.download(
                    account,
                    blobId,
                    mediaType,
                    MAX_AVATAR_BYTES,
                    name = AVATAR_DOWNLOAD_NAME,
                )
            return blob.bytes.takeIf { it.isNotEmpty() && blob.mediaType in CONTACT_RASTER_TYPES }?.let(::AvatarImage)
        }
        val uri = media.uri
        return when {
            uri.isEmpty() -> null
            uri.startsWith("data:") -> inlineContactImage(uri)
            external -> images.load(uri)
            else -> null
        }
    }
}

/** Some servers keep data URIs instead of returning a blobId. Bound input before Base64 decoding. */
private fun inlineContactImage(uri: String): AvatarImage? {
    if (uri.length > MAX_INLINE_IMAGE_CHARS) return null
    val header = uri.substringBefore(',')
    val supported =
        header.endsWith(";base64") &&
            header.removePrefix("data:").removeSuffix(";base64") in CONTACT_RASTER_TYPES
    return if (supported) {
        Base64
            .decode(uri.substringAfter(',', ""))
            .takeIf { it.isNotEmpty() && it.size <= MAX_AVATAR_BYTES }
            ?.let(::AvatarImage)
    } else {
        null
    }
}

private const val MAX_INLINE_IMAGE_CHARS = 700_000
private const val AVATAR_DOWNLOAD_NAME = "avatar"
private const val DEFAULT_PHOTO_TYPE = "image/jpeg"
private val CONTACT_RASTER_TYPES = setOf("image/png", "image/jpeg", "image/webp")
