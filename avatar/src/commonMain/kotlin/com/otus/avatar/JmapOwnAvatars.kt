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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64
import com.otus.jmap.contacts.AddressBookType as BookType
import com.otus.jmap.contacts.ContactCardType as CardType

/**
 * Keeps each address's avatar on a self card: a contact card in the user's own address book with
 * that address. A photo is a standard JSContact `media` entry with a `data:` URI, because Stalwart
 * refuses `blobId` there, so other contact apps show it too. A style goes in the vCard property
 * [AVATAR_STYLE_PROPERTY], and saving one removes the card's photos so that every app agrees there
 * is none. Saving clears the login's cached avatars and hands [service] the saved one, since a
 * search right after a save may not find a new card while the server indexes it. For the same
 * reason, a card made here is remembered, so the next save updates it instead of making another.
 */
public class JmapOwnAvatars(
    private val connections: JmapConnections,
    private val logins: LoginStore,
    private val service: AvatarService,
    private val options: () -> AvatarOptions,
) : OwnAvatars {
    /** The id of each self card made here, by login and address, until a search finds it. */
    private val made = mutableMapOf<Pair<String, String>, String>()
    private val madeLock = Mutex()

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
        if (avatar != null) service.saved(AvatarRequest(accountKey, email, name), avatar)
    }

    private suspend fun write(
        accountKey: String,
        email: String,
        name: String,
        avatar: ContactAvatar?,
    ) {
        val (connection, account) = contactsAccount(accountKey)
        val books = connection.call(GetMethod(BookType, account)).list.filter { it.myRights.mayWrite }
        val madeId = madeLock.withLock { made[accountKey to email] }
        val card = selfCard(connection, account, email, books.map { it.id }.toSet(), madeId)
        val photo = (avatar as? ContactAvatar.Photo)?.toMedia()
        val method =
            when {
                card != null -> SetMethod(CardType, account, update = card.update(avatar, photo))
                avatar == null -> return
                else -> SetMethod(CardType, account, create = newCard(email, name, books, avatar, photo))
            }
        val result = connection.call(method)
        val error = result.firstError
        if (error != null) {
            val reason = if (error.type == "forbidden") OwnAvatarFailure.ReadOnly else OwnAvatarFailure.Failed
            throw OwnAvatarException(reason, IllegalStateException(error.description ?: error.type))
        }
        val created = result.createdId(NEW_CARD) ?: return
        madeLock.withLock { made[accountKey to email] = created }
    }

    /** The login's connection and the account that holds its contacts. */
    private suspend fun contactsAccount(accountKey: String): Pair<JmapConnection, String> {
        val login = logins.savedLogin(accountKey) ?: throw OwnAvatarException(OwnAvatarFailure.Failed)
        val connection = connections.connection(login)
        val account =
            connection.session().accountFor(JmapCapability.CONTACTS)
                ?: throw OwnAvatarException(OwnAvatarFailure.NoContacts)
        return connection to account
    }

    /**
     * A card with [email] in a writable book, preferring one whose avatar Otus wrote. The card
     * [made] here is read by its id when the search does not find it yet.
     */
    private suspend fun selfCard(
        connection: JmapConnection,
        account: String,
        email: String,
        writable: Set<String>,
        made: String?,
    ): Card? =
        withCardProperties { properties ->
            lateinit var get: JmapCall<GetResult<Card>>
            lateinit var byId: JmapCall<GetResult<Card>>
            val response =
                connection.send {
                    val query =
                        call(QueryMethod(CardType, account, ContactCardFilter(email = email).toFilter().toJson()))
                    get = call(GetMethod(CardType, account, JmapIds.Result(query.resultOf("/ids")), properties))
                    if (made != null) {
                        byId = call(GetMethod(CardType, account, JmapIds.Of(listOf(made)), properties))
                    }
                }
            val found = if (made != null) response[byId].list else emptyList()
            (response[get].list + found).distinctBy { it.id }
        }.filter { it.isSelfCard(email, writable) }
            .sortedWith(compareBy({ !it.isAvatarCard }, { it.id }))
            .firstOrNull()
}

/** Whether this card has [email] and sits in one of the [writable] books. */
private fun Card.isSelfCard(
    email: String,
    writable: Set<String>,
): Boolean =
    addressBookIds.orEmpty().any { it.value && it.key in writable } &&
        emails.orEmpty().values.any { avatarEmailKey(it.address) == email }

/** The book a new self card goes in; without one the user may write to, there is nowhere to save. */
private fun List<AddressBook>.saveTarget(): AddressBook =
    sortedByDescending { it.isDefault }.firstOrNull() ?: throw OwnAvatarException(OwnAvatarFailure.ReadOnly)

/** The `update` argument that sets this card's avatar. */
private fun Card.update(
    avatar: ContactAvatar?,
    photo: Media?,
): Map<String, JsonObject> = mapOf(requireNotNull(id) to patch(avatar, photo))

private fun Card.patch(
    avatar: ContactAvatar?,
    photo: Media?,
): JsonObject {
    val oldPhotos =
        media
            .orEmpty()
            .filter { (key, value) -> value.kind == MediaKind.Photo && !(photo != null && key == AVATAR_PHOTO_KEY) }
            .keys
    val hasMedia = media != null
    val vCard = extra[VCARD_PROPERTY]
    val style = (avatar as? ContactAvatar.Styled)?.style
    return contactCardPatch {
        oldPhotos.forEach { media.remove(it) }
        // A patch path needs its parent on the server, so a card without `media` gets the whole map.
        when {
            photo == null -> Unit
            !hasMedia -> media.set(mapOf(AVATAR_PHOTO_KEY to photo))
            else -> media.put(AVATAR_PHOTO_KEY, photo)
        }
        if (style != null || hasAvatarStyle(vCard)) {
            raw {
                val properties = avatarPropertiesWith(vCard, style)
                // The same goes for `vCard`.
                if (vCard is JsonObject) {
                    set(properties, VCARD_PROPERTY, VCARD_PROPERTIES)
                } else {
                    set(JsonObject(mapOf(VCARD_PROPERTIES to properties)), VCARD_PROPERTY)
                }
            }
        }
    }
}

/** The `create` argument that makes a self card for [email] in the default writable book. */
private fun newCard(
    email: String,
    name: String,
    books: List<AddressBook>,
    avatar: ContactAvatar,
    photo: Media?,
): Map<String, JsonObject> =
    mapOf(
        NEW_CARD to
            Card(
                kind = CardKind.Individual,
                name = name.trim().takeIf { it.isNotEmpty() }?.let { Name(full = it) },
                emails = mapOf(EMAIL_KEY to EmailAddress(email)),
                media = photo?.let { mapOf(AVATAR_PHOTO_KEY to it) },
                addressBookIds = mapOf(books.saveTarget().id to true),
            ).withAvatarStyle((avatar as? ContactAvatar.Styled)?.style)
                .toCreation(),
    )

private fun ContactAvatar.Photo.toMedia() =
    Media(MediaKind.Photo, uri = "data:$mediaType;base64,${Base64.encode(image.bytes)}", mediaType = mediaType)

private const val NEW_CARD = "self"
private const val EMAIL_KEY = "e1"
