package com.otusfoss.core.accounts.data

import com.otusfoss.core.accounts.testing.MailService
import com.otusfoss.core.accounts.testing.STALWART_SESSION
import com.otusfoss.core.jmap.JmapClient
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.login.InMemoryLoginStore
import com.otusfoss.core.jmap.login.JmapLogin
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class SavedAccountLookupTest {
    private val hosts = listOf("mail.example.org", "down.example.net", "mail.example.com")
    private val session =
        STALWART_SESSION.replace(
            "\"urn:ietf:params:jmap:mail\": {}",
            "\"urn:ietf:params:jmap:mail\": {}, \"urn:ietf:params:jmap:submission\": {}",
        )
    private val store = InMemoryLoginStore()

    private fun repository(engine: MockEngine): JmapAccountRepository {
        hosts.forEach { store.save(JmapLogin(it, "https://$it/jmap/session", "ada@example.org", it), "secret") }
        val client = JmapClient(engine)
        return JmapAccountRepository(client, JmapConnections(client, store), store, MailService)
    }

    @Test
    fun everyServerIsAskedBeforeAnyAnswersAndAccountsKeepTheirSavedOrder() =
        runTest {
            val servers =
                GatedServers(hosts.toSet()) { request ->
                    val json = headersOf(HttpHeaders.ContentType, "application/json")
                    when {
                        request.url.host == "down.example.net" -> throw IOException("connection refused")
                        request.url.encodedPath == "/jmap/session" -> respond(session, HttpStatusCode.OK, json)
                        else -> respond(identity("ada@${request.url.host}"), HttpStatusCode.OK, json)
                    }
                }
            val accounts = repository(servers.engine).accounts()
            assertEquals(hosts, accounts.map { it.id })
            // The unreachable server falls back to the saved sign-in and does not drop the others.
            assertEquals(
                listOf(listOf("ada@mail.example.org"), emptyList(), listOf("ada@mail.example.com")),
                accounts.map { it.emailAddresses },
            )
        }

    @Test
    fun cancellingTheLoadStopsEveryLookupInFlight() =
        runTest {
            val waiting = Channel<String>(Channel.UNLIMITED)
            val stopped = Channel<String>(Channel.UNLIMITED)
            val servers =
                GatedServers(hosts.toSet()) { request ->
                    try {
                        waiting.trySend(request.url.host)
                        awaitCancellation()
                    } finally {
                        stopped.trySend(request.url.host)
                    }
                }
            val loading = launch { repository(servers.engine).accounts() }
            assertEquals(hosts.toSet(), List(hosts.size) { waiting.receive() }.toSet())
            loading.cancelAndJoin()
            assertEquals(hosts.toSet(), List(hosts.size) { stopped.receive() }.toSet())
        }
}

/**
 * Servers that hold every answer until each of [hosts] has been asked, so lookups made one at a
 * time fail here instead of passing. [answer] serves each request after that.
 */
private class GatedServers(
    private val hosts: Set<String>,
    private val answer: MockRequestHandler,
) {
    private val lock = Mutex()
    private val asked = mutableSetOf<String>()
    private val everyoneAsked = CompletableDeferred<Unit>()

    val engine =
        MockEngine { request ->
            lock.withLock {
                asked += request.url.host
                if (asked == hosts) everyoneAsked.complete(Unit)
            }
            // The engine answers on its own dispatcher, so this waits in real time, not test time.
            withTimeoutOrNull(10.seconds) { everyoneAsked.await() }
                ?: error("Servers were asked one at a time; ${request.url.host} waited for the others")
            answer(request)
        }
}

private fun identity(email: String) =
    """{"methodResponses": [["Identity/get", {"state": "i", "list": [{"email": "$email"}]}, "c0"]]}"""
