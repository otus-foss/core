package com.otus.avatar

import com.otus.jmap.GetMethod
import com.otus.jmap.GetResult
import com.otus.jmap.JmapCall
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapConnection
import com.otus.jmap.JmapConnections
import com.otus.jmap.JmapIds
import com.otus.jmap.QueryMethod
import com.otus.jmap.SetMethod
import com.otus.jmap.contacts.AddressBook
import com.otus.jmap.contacts.ContactCardFilter
import com.otus.jmap.contacts.contactCardPatch
import com.otus.jmap.contacts.toCreation
import com.otus.jmap.login.LoginStore
import com.otus.jscontact.Card
import com.otus.jscontact.CardKind
import com.otus.jscontact.EmailAddress
import com.otus.jscontact.Media
import com.otus.jscontact.MediaKind
import com.otus.jscontact.Name
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64
import com.otus.jmap.contacts.AddressBookType as BookType
import com.otus.jmap.contacts.ContactCardType as CardType

/**
 * Keeps each address's avatar on a self card: a contact card in the user's own address book with
 * that address. A photo is a standard JSContact `media` entry with a `data:` URI, because Stalwart
 * refuses `blobId` there, so other contact apps show it too. A style goes in the vCard property
 * [AVATAR_STYLE_PROPERTY], and saving one removes the card's photos so that every app agrees there
 * is none. Saving clears the login's cached avatars, so [service] reads the new one.
 */
public class JmapOwnAvatars(
    private val connections: JmapConnections,
    private val logins: LoginStore,
    private val service: AvatarService,
    private val options: () -> AvatarOptions,
) : OwnAvatars {
    override suspend fun avatar(
        accountKey: String,
        email: String,
        name: String,
    ): ResolvedAvatar = service.resolve(AvatarRequest(accountKey, email, name), options())

    override suspend fun save(
        accountKey: String,
        email: String,
        name: String,
        avatar: ContactAvatar?,
    ) {
        runCatching { write(accountKey, avatarEmailKey(email), name, avatar) }.onFailure { error ->
            if (error is CancellationException || error is OwnAvatarException) throw error
            throw OwnAvatarException(OwnAvatarFailure.Failed, error)
        }
        service.removeAccount(accountKey)
    }

    private suspend fun write(
        accountKey: String,
        email: String,
        name: String,
        avatar: ContactAvatar?,
    ) {
        val (connection, account) = contactsAccount(accountKey)
        val books = connection.call(GetMethod(BookType, account)).list.filter { it.myRights.mayWrite }
        val card = selfCard(connection, account, email, books.map { it.id }.toSet())
        val photo = (avatar as? ContactAvatar.Photo)?.toMedia()
        val method =
            when {
                card != null -> {
                    SetMethod(CardType, account, update = mapOf(requireNotNull(card.id) to card.patch(avatar, photo)))
                }

                avatar == null -> {
                    return
                }

                else -> {
                    val book = books.sortedByDescending { it.isDefault }.firstOrNull()
                    SetMethod(
                        CardType,
                        account,
                        create =
                            mapOf(NEW_CARD to newCard(email, name, book.writable().id, avatar, photo)),
                    )
                }
            }
        connection.call(method).firstError?.let { error ->
            val reason = if (error.type == "forbidden") OwnAvatarFailure.ReadOnly else OwnAvatarFailure.Failed
            throw OwnAvatarException(reason, IllegalStateException(error.description ?: error.type))
        }
    }

    /** The login's connection and the account that holds its contacts. */
    private suspend fun contactsAccount(accountKey: String): Pair<JmapConnection, String> {
        val login =
            withContext(Dispatchers.IO) { logins.logins().firstOrNull { it.id == accountKey } }
                ?: throw OwnAvatarException(OwnAvatarFailure.Failed)
        val connection = connections.connection(login)
        val account =
            connection.session().accountFor(JmapCapability.CONTACTS)
                ?: throw OwnAvatarException(OwnAvatarFailure.NoContacts)
        return connection to account
    }

    /** A card with [email] in a writable book, preferring one that already holds a style. */
    private suspend fun selfCard(
        connection: JmapConnection,
        account: String,
        email: String,
        writable: Set<String>,
    ): Card? {
        val cards =
            withCardProperties { properties ->
                lateinit var get: JmapCall<GetResult<Card>>
                val response =
                    connection.send {
                        val query =
                            call(QueryMethod(CardType, account, ContactCardFilter(email = email).toFilter().toJson()))
                        get = call(GetMethod(CardType, account, JmapIds.Result(query.resultOf("/ids")), properties))
                    }
                response[get].list
            }
        return cards
            .filter { card ->
                card.addressBookIds.orEmpty().any { it.value && it.key in writable } &&
                    card.emails
                        .orEmpty()
                        .values
                        .any { avatarEmailKey(it.address) == email }
            }.sortedWith(compareBy({ !hasAvatarStyle(it.extra[VCARD]) }, { it.id }))
            .firstOrNull()
    }
}

/** The book a new self card goes in; without one the user may write to, there is nowhere to save. */
private fun AddressBook?.writable(): AddressBook = this ?: throw OwnAvatarException(OwnAvatarFailure.ReadOnly)

private fun Card.patch(
    avatar: ContactAvatar?,
    photo: Media?,
): JsonObject {
    val oldPhotos =
        media
            .orEmpty()
            .filter { (key, value) -> value.kind == MediaKind.Photo && !(photo != null && key == PHOTO_KEY) }
            .keys
    val vCard = extra[VCARD]
    val style = (avatar as? ContactAvatar.Styled)?.style
    return contactCardPatch {
        oldPhotos.forEach { media.remove(it) }
        photo?.let { media.put(PHOTO_KEY, it) }
        if (style != null || hasAvatarStyle(vCard)) {
            raw {
                val properties = avatarPropertiesWith(vCard, style)
                // A patch path needs its parent on the server, so a card without `vCard` gets the whole object.
                if (vCard is JsonObject) {
                    set(properties, VCARD, PROPERTIES)
                } else {
                    set(
                        JsonObject(
                            mapOf(PROPERTIES to properties),
                        ),
                        VCARD,
                    )
                }
            }
        }
    }
}

private fun newCard(
    email: String,
    name: String,
    book: String,
    avatar: ContactAvatar,
    photo: Media?,
) = Card(
    kind = CardKind.Individual,
    name = name.trim().takeIf { it.isNotEmpty() }?.let { Name(full = it) },
    emails = mapOf(EMAIL_KEY to EmailAddress(email)),
    media = photo?.let { mapOf(PHOTO_KEY to it) },
    addressBookIds = mapOf(book to true),
    extra =
        (avatar as? ContactAvatar.Styled)
            ?.let { mapOf(VCARD to JsonObject(mapOf(PROPERTIES to avatarPropertiesWith(null, it.style)))) }
            .orEmpty(),
).toCreation()

private fun ContactAvatar.Photo.toMedia() =
    Media(MediaKind.Photo, uri = "data:$mediaType;base64,${Base64.encode(image.bytes)}", mediaType = mediaType)

private const val NEW_CARD = "self"
private const val EMAIL_KEY = "e1"
private const val PHOTO_KEY = "otus"
private const val VCARD = "vCard"
private const val PROPERTIES = "properties"
