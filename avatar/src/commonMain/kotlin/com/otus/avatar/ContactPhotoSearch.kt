package com.otus.avatar

import com.otus.jmap.GetMethod
import com.otus.jmap.GetResult
import com.otus.jmap.JmapCall
import com.otus.jmap.JmapConnection
import com.otus.jmap.JmapIds
import com.otus.jmap.QueryMethod
import com.otus.jmap.QueryResult
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** Search each page without retaining the whole address book or trusting fuzzy email matches. */
internal suspend fun findContactPhoto(
    connection: JmapConnection,
    account: String,
    email: String,
    books: Set<String>,
    load: suspend (ContactCard) -> AvatarImage?,
): AvatarImage? =
    withTimeoutOrNull(CONTACT_SEARCH_TIMEOUT_MILLIS) {
        var position = 0L
        var state: String? = null
        while (true) {
            val (query, cards) = contactPage(connection, account, email, books, position)
            // Stop if the server ignores pagination or the result set changes between pages.
            val queryChanged = state != null && state != query.queryState
            if (query.position != position || queryChanged || query.ids.isEmpty()) break
            state = query.queryState
            val photo =
                cards
                    .filter { card ->
                        card.addressBookIds.any { it.value && it.key in books } &&
                            card.emails.values.any { avatarEmailKey(it.address) == avatarEmailKey(email) }
                    }.sortedBy { it.id }
                    .firstNotNullOfOrNull { load(it) }
            if (photo != null) return@withTimeoutOrNull photo
            position += query.ids.size
        }
        null
    }

private suspend fun contactPage(
    connection: JmapConnection,
    account: String,
    email: String,
    books: Set<String>,
    position: Long,
): Pair<QueryResult, List<ContactCard>> {
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
            get = call(GetMethod(ContactCardType, account, JmapIds.Result(query.resultOf("/ids")), CARD_PROPERTIES))
        }
    return response[query] to response[get].list
}

private fun contactFilter(
    email: String,
    books: Set<String>,
): JsonObject =
    buildJsonObject {
        put("operator", JsonPrimitive("AND"))
        put(
            "conditions",
            JsonArray(
                listOf(
                    buildJsonObject { put("email", JsonPrimitive(email)) },
                    buildJsonObject {
                        put("operator", JsonPrimitive("OR"))
                        put(
                            "conditions",
                            JsonArray(
                                books.sorted().map { book ->
                                    buildJsonObject { put("inAddressBook", JsonPrimitive(book)) }
                                },
                            ),
                        )
                    },
                ),
            ),
        )
    }

private const val CONTACT_PAGE_SIZE = 32
private const val CONTACT_SEARCH_TIMEOUT_MILLIS = 10_000L
private val CARD_PROPERTIES = listOf("id", "addressBookIds", "emails", "media")
