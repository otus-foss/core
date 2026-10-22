package com.otus.jmap.contacts

import com.otus.jmap.JmapAuth
import com.otus.jmap.JmapCall
import com.otus.jmap.JmapClient
import com.otus.jmap.JmapJson
import com.otus.jmap.JmapRequestBuilder
import com.otus.jmap.JmapResponse
import com.otus.jmap.JmapSession
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

const val SESSION_URL = "https://contacts.example.org/jmap/session"

val ADA = JmapAuth.Basic("ada@example.org", "secret")

/**
 * Account `a0x9` of the RFC 9610 examples (section 4) holds Ada's contacts, `team` holds a shared
 * address book, and `mail` has no contacts. Without [contacts] the server does not offer contacts.
 */
fun contactsSession(contacts: Boolean = true): String =
    """
    {"capabilities": {"urn:ietf:params:jmap:core": {}, "urn:ietf:params:jmap:mail": {}
      ${if (contacts) CONTACTS_CAPABILITY else ""}},
     "accounts": {
       "a0x9": {"name": "Ada", "isPersonal": true, "accountCapabilities": {"urn:ietf:params:jmap:contacts":
         {"maxAddressBooksPerCard": null, "mayCreateAddressBook": true}}},
       "team": {"name": "Team", "accountCapabilities": {"urn:ietf:params:jmap:contacts":
         {"maxAddressBooksPerCard": 1, "mayCreateAddressBook": false}}},
       "mail": {"name": "Mail", "accountCapabilities": {"urn:ietf:params:jmap:mail": {}}}},
     "primaryAccounts": {"urn:ietf:params:jmap:contacts": "a0x9"},
     "username": "ada@example.org", "apiUrl": "/jmap/", "state": "s1"}
    """.trimIndent()

/** A JMAP server that serves [session] and answers each API request with [reply]. Records each request body. */
class ContactsServer(
    session: String = contactsSession(),
    reply: (String) -> String = { error("unexpected API call") },
) {
    val bodies = mutableListOf<String>()
    val client =
        JmapClient(
            MockEngine { request ->
                val json = headersOf(HttpHeaders.ContentType, "application/json")
                if (request.url.encodedPath == "/jmap/session") {
                    respond(session, HttpStatusCode.OK, json)
                } else {
                    val body = (request.body as TextContent).text
                    bodies += body
                    respond(reply(body), HttpStatusCode.OK, json)
                }
            },
        )

    suspend fun session(): JmapSession = client.session(SESSION_URL, ADA)

    suspend fun <T> request(build: JmapRequestBuilder.() -> JmapCall<T>): T = client.request(session(), ADA, build)

    suspend fun send(build: JmapRequestBuilder.() -> Unit): JmapResponse = client.send(session(), ADA, build)

    /** The `methodCalls` of the last request. */
    fun methodCalls(): JsonArray = parseObject(bodies.last()).getValue("methodCalls").jsonArray
}

/** A response envelope around [methodResponses], such as an RFC figure. */
fun responseOf(methodResponses: String): String =
    """{"methodResponses": ${numbered(methodResponses)}, "sessionState": "s1"}"""

/**
 * The invocations of an RFC figure as this client numbers its calls: `c0`, `c1`, and so on. Null
 * arguments are left out, because RFC 8620, section 3.5 makes null the default of an omitted one.
 */
fun numbered(invocations: String): JsonArray =
    JsonArray(
        JmapJson.parseToJsonElement(invocations).jsonArray.map { invocation ->
            val (name, arguments, id) = invocation.jsonArray
            JsonArray(listOf(name, arguments.jsonObject.withoutNulls(), JsonPrimitive("c${id.jsonPrimitive.content}")))
        },
    )

/** [calls] as [numbered] writes them, to compare with an RFC figure. */
fun normalized(calls: JsonArray): JsonArray =
    JsonArray(
        calls.map { call ->
            val (name, arguments, id) = call.jsonArray
            JsonArray(listOf(name, arguments.jsonObject.withoutNulls(), id))
        },
    )

fun parseObject(json: String): JsonObject = JmapJson.parseToJsonElement(json).jsonObject

private fun JsonObject.withoutNulls(): JsonObject = JsonObject(filterValues { it != JsonNull })

private const val CONTACTS_CAPABILITY = """, "urn:ietf:params:jmap:contacts": {}"""
