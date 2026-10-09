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

/** Uses standard contacts capabilities only. Never searches another saved login's address books. */
public class JmapContactPhotoSource(
    private val connections: JmapConnections,
    private val logins: LoginStore,
    private val images: AvatarImageLoader,
) : ContactPhotoSource {
    override suspend fun photo(
        request: AvatarRequest,
        allowExternalImages: Boolean,
    ): AvatarImage? =
        avatarOrNull {
            val login =
                withContext(Dispatchers.IO) { logins.logins().firstOrNull { it.id == request.accountKey } }
                    ?: return@avatarOrNull null
            val connection = connections.connection(login)
            val session = connection.session()
            if (JmapCapability.CONTACTS !in session.capabilities) return@avatarOrNull null
            // Contacts may live in a separate JMAP account from mail, within the same login.
            val account = session.accountFor(JmapCapability.CONTACTS) ?: return@avatarOrNull null
            val books = subscribedBooks(connection, account)
            if (books.isEmpty()) return@avatarOrNull null
            findContactPhoto(connection, account, request.email, books) {
                loadCard(connection, account, it, allowExternalImages)
            }
        }

    private suspend fun subscribedBooks(
        connection: JmapConnection,
        account: String,
    ): Set<String> {
        val books = connection.call(GetMethod(AddressBookType, account)).list
        return books.filter { it.isSubscribed && it.myRights.mayRead }.map { it.id }.toSet()
    }

    private suspend fun loadCard(
        connection: JmapConnection,
        account: String,
        card: ContactCard,
        external: Boolean,
    ): AvatarImage? =
        card.media.values
            .filter { it.kind == "photo" || it.kind == "logo" }
            .sortedWith(compareBy({ it.kind != "photo" }, { it.pref ?: Int.MAX_VALUE }, { it.blobId ?: it.uri }))
            .firstNotNullOfOrNull { photo -> avatarOrNull { load(connection, account, photo, external) } }

    private suspend fun load(
        connection: JmapConnection,
        account: String,
        media: ContactMedia,
        external: Boolean,
    ): AvatarImage? {
        if (media.blobId != null && media.mediaType in CONTACT_RASTER_TYPES) {
            val blob = connection.download(account, media.blobId, requireNotNull(media.mediaType), MAX_AVATAR_BYTES)
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
private val CONTACT_RASTER_TYPES = setOf("image/png", "image/jpeg", "image/webp")
