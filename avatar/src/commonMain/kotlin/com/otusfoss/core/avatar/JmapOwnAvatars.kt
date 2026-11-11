package com.otusfoss.core.avatar

import com.otusfoss.core.jmap.GetMethod
import com.otusfoss.core.jmap.GetResult
import com.otusfoss.core.jmap.JmapCall
import com.otusfoss.core.jmap.JmapCapability
import com.otusfoss.core.jmap.JmapConnection
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.JmapException
import com.otusfoss.core.jmap.JmapIds
import com.otusfoss.core.jmap.QueryMethod
import com.otusfoss.core.jmap.SetMethod
import com.otusfoss.core.jmap.contacts.AddressBook
import com.otusfoss.core.jmap.contacts.ContactCardFilter
import com.otusfoss.core.jmap.contacts.cardPatch
import com.otusfoss.core.jmap.contacts.toCreation
import com.otusfoss.core.jscontact.Card
import com.otusfoss.core.jscontact.CardKind
import com.otusfoss.core.jscontact.EmailAddress
import com.otusfoss.core.jscontact.Name
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import com.otusfoss.core.jmap.contacts.AddressBookType as BookType
import com.otusfoss.core.jmap.contacts.ContactCardType as CardType

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
        val method =
            when {
                card != null -> SetMethod(CardType, account, update = card.update(avatar))
                avatar == null -> return
                else -> SetMethod(CardType, account, create = newCard(email, name, books, avatar))
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
        val account =
            try {
                connections.account(accountKey, JmapCapability.CONTACTS)
            } catch (error: JmapException.Unsupported) {
                throw OwnAvatarException(OwnAvatarFailure.NoContacts, error)
            } ?: throw OwnAvatarException(OwnAvatarFailure.Failed)
        return account.connection to account.id
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
                        call(QueryMethod(CardType, account, ContactCardFilter(email = email).toFilter()))
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
private fun Card.update(avatar: ContactAvatar?): Map<String, JsonObject> =
    mapOf(requireNotNull(id) to cardPatch(this, withAvatar(avatar)))

/** The `create` argument that makes a self card for [email] in the default writable book. */
private fun newCard(
    email: String,
    name: String,
    books: List<AddressBook>,
    avatar: ContactAvatar,
): Map<String, JsonObject> =
    mapOf(
        NEW_CARD to
            Card(
                kind = CardKind.Individual,
                name = name.trim().takeIf { it.isNotEmpty() }?.let { Name(full = it) },
                emails = mapOf(EMAIL_KEY to EmailAddress(email)),
                addressBookIds = mapOf(books.saveTarget().id to true),
            ).withAvatar(avatar)
                .toCreation(),
    )

private const val NEW_CARD = "self"
private const val EMAIL_KEY = "e1"
