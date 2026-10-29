package com.otus.avatar

import com.otus.jmap.FilterOperator
import com.otus.jmap.GetMethod
import com.otus.jmap.GetResult
import com.otus.jmap.JmapCall
import com.otus.jmap.JmapConnection
import com.otus.jmap.JmapException
import com.otus.jmap.JmapFilter
import com.otus.jmap.JmapIds
import com.otus.jmap.QueryMethod
import com.otus.jmap.QueryResult
import com.otus.jmap.allOf
import com.otus.jmap.contacts.ContactCardFilter
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Search each page without retaining the whole address book or trusting fuzzy email matches. On
 * each page, cards in the [own] books the user may write come first, so a shared card never hides
 * the user's own; then the cards Otus wrote, so an avatar the user set wins over older photos.
 */
internal suspend fun <T : Any> findContactAvatar(
    connection: JmapConnection,
    account: String,
    email: String,
    books: Set<String>,
    own: Set<String>,
    load: suspend (ContactCard) -> T?,
): T? =
    withTimeoutOrNull(CONTACT_SEARCH_TIMEOUT_MILLIS) {
        var position = 0L
        var state: String? = null
        while (true) {
            val (query, cards) = contactPage(connection, account, email, books, position)
            // Stop if the server ignores pagination or the result set changes between pages.
            val queryChanged = state != null && state != query.queryState
            if (query.position != position || queryChanged || query.ids.isEmpty()) break
            state = query.queryState
            val found =
                cards
                    .filter { it.isIn(books) && it.hasAddress(email) }
                    .sortedWith(compareBy({ !it.isIn(own) }, { !it.isOtusAvatar }, { it.id }))
                    .firstNotNullOfOrNull { load(it) }
            if (found != null) return@withTimeoutOrNull found
            position += query.ids.size
        }
        null
    }

private fun ContactCard.hasAddress(email: String): Boolean =
    emails.values.any { avatarEmailKey(it.address) == avatarEmailKey(email) }

private suspend fun contactPage(
    connection: JmapConnection,
    account: String,
    email: String,
    books: Set<String>,
    position: Long,
): Pair<QueryResult, List<ContactCard>> =
    withCardProperties { properties ->
        lateinit var query: JmapCall<QueryResult>
        lateinit var get: JmapCall<GetResult<ContactCard>>
        val response =
            connection.send {
                query =
                    call(
                        QueryMethod(
                            ContactCardType,
                            account,
                            contactFilter(email, books),
                            limit = CONTACT_PAGE_SIZE,
                            extra = mapOf("position" to JsonPrimitive(position)),
                        ),
                    )
                get = call(GetMethod(ContactCardType, account, JmapIds.Result(query.resultOf("/ids")), properties))
            }
        response[query] to response[get].list
    }

/**
 * Runs [load] asking for the card properties avatars need, including `vCard`, which holds the
 * style. Stalwart returns a card's extra vCard properties only when `vCard` is asked for by name. A
 * server that does not know `vCard` rejects the call (RFC 8620, section 5.1), so [load] then runs
 * again without it.
 */
internal suspend fun <T> withCardProperties(load: suspend (List<String>) -> T): T =
    try {
        load(CARD_PROPERTIES + VCARD_PROPERTY)
    } catch (error: JmapException.MethodError) {
        if (error.type != "invalidArguments") throw error
        load(CARD_PROPERTIES)
    }

private fun contactFilter(
    email: String,
    books: Set<String>,
): JsonObject =
    allOf(
        ContactCardFilter(email = email).toFilter(),
        JmapFilter.Operator(FilterOperator.OR, books.sorted().map { ContactCardFilter(inAddressBook = it).toFilter() }),
    ).toJson()

private const val CONTACT_PAGE_SIZE = 32
private val CARD_PROPERTIES = listOf("id", "addressBookIds", "emails", "media")
private const val CONTACT_SEARCH_TIMEOUT_MILLIS = 10_000L
