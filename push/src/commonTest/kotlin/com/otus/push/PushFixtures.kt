package com.otus.push

import com.otus.jmap.JmapClient
import com.otus.jmap.JmapConnections
import com.otus.jmap.PushKeys
import com.otus.jmap.PushMessage
import com.otus.jmap.login.InMemoryLoginStore
import com.otus.jmap.login.JmapLogin
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlin.time.Clock
import kotlin.time.Instant

/** A login, a fake JMAP server, and a store for the push tests. */
internal val login = JmapLogin("ada", "https://mail.example.org/jmap/session", "ada@example.org", "Ada")
internal val endpoint = PushEndpoint("https://ntfy.example.net/upA", PushKeys("BKey", "c2Vj"))
internal val now = Instant.parse("2026-10-14T08:00:00Z")

internal class Store : PushStateStore {
    var records: Map<String, String> = emptyMap()

    override fun read() = records

    override fun write(records: Map<String, String>) {
        this.records = records
    }
}

internal class Server(
    val reply: (String) -> String,
) {
    val sent = mutableListOf<String>()
    val engine =
        MockEngine { request ->
            val json = headersOf(HttpHeaders.ContentType, "application/json")
            if (request.url.encodedPath == "/jmap/session") {
                respond(SESSION, HttpStatusCode.OK, json)
            } else {
                val body = (request.body as TextContent).text
                sent += body
                respond(reply(body), HttpStatusCode.OK, json)
            }
        }
}

internal fun subscriptions(
    server: Server,
    store: Store,
    at: Instant = now,
): PushSubscriptions {
    val logins = InMemoryLoginStore().apply { save(login, "secret") }
    val clock =
        object : Clock {
            override fun now() = at
        }
    return PushSubscriptions(
        JmapConnections(JmapClient(server.engine), logins),
        logins,
        store,
        deviceClientId = "otus-mail-device",
        types = listOf("EmailDelivery"),
        clock = clock,
    )
}

internal fun created(expires: String = "2026-10-21T08:00:00Z") =
    """{"methodResponses": [["PushSubscription/set",
       {"created": {"push": {"id": "ps1", "expires": "$expires"}}}, "c0"]]}"""

internal const val NO_SUBSCRIPTIONS =
    """{"methodResponses": [["PushSubscription/get", {"list": [], "notFound": []}, "c0"]]}"""

internal const val UPDATED = """{"methodResponses": [["PushSubscription/set", {"updated": {"ps1": null}}, "c0"]]}"""

private const val SESSION = """
        {"capabilities": {"urn:ietf:params:jmap:core": {}, "urn:ietf:params:jmap:mail": {}},
         "accounts": {"d1": {"name": "Ada", "isPersonal": true,
           "accountCapabilities": {"urn:ietf:params:jmap:mail": {}}}},
         "primaryAccounts": {"urn:ietf:params:jmap:mail": "d1"},
         "username": "ada@example.org", "apiUrl": "/jmap/", "state": "s1"}
    """

/** Leaves the store with a created and verified subscription `ps1`, made at [now]. */
internal suspend fun subscribed(
    store: Store,
    expires: String = "2026-10-21T08:00:00Z",
) {
    subscriptions(Server { if ("PushSubscription/get" in it) NO_SUBSCRIPTIONS else created(expires) }, store).run {
        setEndpoint("ada", endpoint)
        sync()
        receive("ada", PushMessage.Verification("ps1", "v1"))
    }
    subscriptions(Server { UPDATED }, store).sync()
}
