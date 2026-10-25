package com.otus.avatar

import com.otus.jmap.GetMethod
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapConnection
import com.otus.jmap.JmapConnections
import com.otus.jmap.login.LoginStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64

/**
 * Uses standard contacts capabilities, plus the [AVATAR_STYLE_PROPERTY] Otus writes. Never searches
 * another saved login's address books.
 */
public class JmapContactAvatarSource(
    private val connections: JmapConnections,
    private val logins: LoginStore,
    private val images: AvatarImageLoader,
) : ContactAvatarSource {
    override suspend fun avatar(
        request: AvatarRequest,
        allowExternalImages: Boolean,
    ): ContactAvatar? =
        nullOnFailure {
            val login =
                withContext(Dispatchers.IO) { logins.logins().firstOrNull { it.id == request.accountKey } }
                    ?: return@nullOnFailure null
            val connection = connections.connection(login)
            val session = connection.session()
            if (JmapCapability.CONTACTS !in session.capabilities) return@nullOnFailure null
            // Contacts may live in a separate JMAP account from mail, within the same login.
            val account = session.accountFor(JmapCapability.CONTACTS) ?: return@nullOnFailure null
            val books = subscribedBooks(connection, account)
            if (books.isEmpty()) return@nullOnFailure null
            findContactAvatar(connection, account, request.email, books) { card ->
                card.style?.let(ContactAvatar::Styled) ?: loadCard(connection, account, card, allowExternalImages)
            }
        }

    private suspend fun subscribedBooks(
        connection: JmapConnection,
        account: String,
    ): Set<String> {
        val books = connection.call(GetMethod(AddressBookType, account)).list
        // Servers such as Stalwart may leave the user's own books unsubscribed (RFC 9610, section 2).
        return books
            .filter { it.myRights.mayRead && (it.isSubscribed || it.myRights.mayWrite) }
            .map { it.id }
            .toSet()
    }

    private suspend fun loadCard(
        connection: JmapConnection,
        account: String,
        card: ContactCard,
        external: Boolean,
    ): ContactAvatar.Photo? =
        card.media.values
            .filter { it.kind == "photo" || it.kind == "logo" }
            .sortedWith(compareBy({ it.kind != "photo" }, { it.pref ?: Int.MAX_VALUE }, { it.blobId ?: it.uri }))
            .firstNotNullOfOrNull { photo ->
                avatarOrNull { load(connection, account, photo, external) }?.let {
                    ContactAvatar.Photo(it, photo.mediaType ?: DEFAULT_PHOTO_TYPE)
                }
            }

    private suspend fun load(
        connection: JmapConnection,
        account: String,
        media: ContactMedia,
        external: Boolean,
    ): AvatarImage? {
        if (media.blobId != null && media.mediaType in CONTACT_RASTER_TYPES) {
            val mediaType = requireNotNull(media.mediaType)
            val blob =
                connection.download(
                    account,
                    media.blobId,
                    mediaType,
                    MAX_AVATAR_BYTES,
                    name = AVATAR_DOWNLOAD_NAME,
                )
            return blob.bytes.takeIf { it.isNotEmpty() && blob.mediaType in CONTACT_RASTER_TYPES }?.let(::AvatarImage)
        }
        val uri = media.uri
        return when {
            uri == null -> null
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
        val bytes = Base64.decode(uri.substringAfter(',', ""))
        bytes.takeIf { it.isNotEmpty() && it.size <= MAX_AVATAR_BYTES }?.let(::AvatarImage)
    } else {
        null
    }
}

private const val MAX_INLINE_IMAGE_CHARS = 700_000
private const val AVATAR_DOWNLOAD_NAME = "avatar"
private const val DEFAULT_PHOTO_TYPE = "image/jpeg"
private val CONTACT_RASTER_TYPES = setOf("image/png", "image/jpeg", "image/webp")
