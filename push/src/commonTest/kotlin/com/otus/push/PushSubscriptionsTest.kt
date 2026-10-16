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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class PushSubscriptionsTest {
    private val login = JmapLogin("ada", "https://mail.example.org/jmap/session", "ada@example.org", "Ada")
    private val endpoint = PushEndpoint("https://ntfy.example.net/upA", PushKeys("BKey", "c2Vj"))
    private val now = Instant.parse("2026-10-14T08:00:00Z")

    private class Store : PushStateStore {
        var records: Map<String, String> = emptyMap()

        override fun read() = records

        override fun write(records: Map<String, String>) {
            this.records = records
        }
    }

    private class Server(
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

    private fun subscriptions(
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

    private fun created(expires: String = "2026-10-21T08:00:00Z") =
        """{"methodResponses": [["PushSubscription/set",
           {"created": {"push": {"id": "ps1", "expires": "$expires"}}}, "c0"]]}"""

    private val noSubscriptions =
        """{"methodResponses": [["PushSubscription/get", {"list": [], "notFound": []}, "c0"]]}"""

    private val updated = """{"methodResponses": [["PushSubscription/set", {"updated": {"ps1": null}}, "c0"]]}"""

    @Test
    fun anEndpointBecomesASubscriptionThatAVerificationActivates() =
        runTest {
            val store = Store()
            val server = Server { if ("PushSubscription/get" in it) noSubscriptions else created() }
            val push = subscriptions(server, store)
            push.setEndpoint("ada", endpoint)
            assertEquals(mapOf("ada" to PushStatus.Waiting), push.status())
            push.sync()
            val create = server.sent.last()
            listOf(
                "\"url\":\"https://ntfy.example.net/upA\"",
                "\"keys\":{\"p256dh\":\"BKey\",\"auth\":\"c2Vj\"}",
                "\"deviceClientId\":\"otus-mail-device\"",
                "\"types\":[\"EmailDelivery\"]",
                "\"expires\":\"2026-10-21T08:00:00Z\"",
            ).forEach { assertTrue(it in create, create) }

            push.receive("ada", PushMessage.Verification("other", "x"))
            push.receive("ada", PushMessage.Verification("ps1", "v1"))
            val verify = Server { updated }
            subscriptions(verify, store).run {
                sync()
                assertEquals(mapOf("ada" to PushStatus.Active), status())
            }
            val sent = verify.sent.single()
            assertTrue("\"update\":{\"ps1\":{\"verificationCode\":\"v1\"}}" in sent, sent)
        }

    @Test
    fun aNewEndpointReplacesTheOldAndEarlierSubscriptionsOfThisDevice() =
        runTest {
            val store = Store()
            val first = Server { if ("PushSubscription/get" in it) noSubscriptions else created() }
            subscriptions(first, store).run {
                setEndpoint("ada", endpoint)
                sync()
            }
            val second =
                Server {
                    if ("PushSubscription/get" in it) {
                        """{"methodResponses": [["PushSubscription/get", {"list": [
                           {"id": "old", "deviceClientId": "otus-mail-device"},
                           {"id": "web", "deviceClientId": "someone-else"}], "notFound": []}, "c0"]]}"""
                    } else {
                        created()
                    }
                }
            subscriptions(second, store).run {
                setEndpoint("ada", endpoint.copy(url = "https://ntfy.example.net/upB"))
                sync()
            }
            val replace = second.sent.last()
            assertTrue("\"destroy\":[\"old\",\"ps1\"]" in replace, replace)
            assertTrue("upB" in replace, replace)
        }

    @Test
    fun anExpiringSubscriptionIsRenewedAndAnUnchangedOneIsLeftAlone() =
        runTest {
            val store = Store()
            subscriptions(Server { if ("PushSubscription/get" in it) noSubscriptions else created() }, store).run {
                setEndpoint("ada", endpoint)
                sync()
            }
            val quiet = Server { error("unexpected call: $it") }
            subscriptions(quiet, store, at = Instant.parse("2026-10-16T08:00:00Z")).sync()
            assertTrue(quiet.sent.isEmpty())

            val renew = Server { updated }
            subscriptions(renew, store, at = Instant.parse("2026-10-19T08:00:00Z")).sync()
            val sent = renew.sent.single()
            assertTrue("\"update\":{\"ps1\":{\"expires\":\"2026-10-26T08:00:00Z\"}}" in sent, sent)
        }

    @Test
    fun aServerWithoutPushSubscriptionsIsMarkedRefused() =
        runTest {
            val store = Store()
            val server = Server { """{"methodResponses": [["error", {"type": "unknownMethod"}, "c0"]]}""" }
            subscriptions(server, store).run {
                setEndpoint("ada", endpoint)
                sync()
                assertEquals(mapOf("ada" to PushStatus.Refused), status())
            }
        }

    @Test
    fun aPlainHttpEndpointIsRefusedWithoutAskingTheServer() =
        runTest {
            val server = Server { error("unexpected call: $it") }
            subscriptions(server, Store()).run {
                setEndpoint("ada", endpoint.copy(url = "http://ntfy.lan/upA"))
                sync()
                assertEquals(mapOf("ada" to PushStatus.Refused), status())
            }
            assertTrue(server.sent.isEmpty())
        }

    @Test
    fun aRefusedVerificationCodeStartsANewSubscription() =
        runTest {
            val store = Store()
            subscriptions(Server { if ("PushSubscription/get" in it) noSubscriptions else created() }, store).run {
                setEndpoint("ada", endpoint)
                sync()
                receive("ada", PushMessage.Verification("ps1", "wrong"))
            }
            val server =
                Server {
                    when {
                        "verificationCode" in it -> {
                            """{"methodResponses": [["PushSubscription/set",
                               {"notUpdated": {"ps1": {"type": "invalidProperties"}}}, "c0"]]}"""
                        }

                        "PushSubscription/get" in it -> {
                            noSubscriptions
                        }

                        else -> {
                            created().replace("ps1", "ps2")
                        }
                    }
                }
            subscriptions(server, store).run {
                sync()
                assertEquals(mapOf("ada" to PushStatus.Waiting), status())
            }
            assertTrue("\"create\"" in server.sent.last(), server.sent.last())
        }

    @Test
    fun clearingTheEndpointDeletesTheSubscription() =
        runTest {
            val store = Store()
            subscriptions(Server { if ("PushSubscription/get" in it) noSubscriptions else created() }, store).run {
                setEndpoint("ada", endpoint)
                sync()
            }
            val server = Server { """{"methodResponses": [["PushSubscription/set", {"destroyed": ["ps1"]}, "c0"]]}""" }
            subscriptions(server, store).run {
                clearEndpoint("ada")
                sync()
                assertEquals(emptyMap(), status())
            }
            assertTrue("\"destroy\":[\"ps1\"]" in server.sent.single(), server.sent.single())
        }

    @Test
    fun anUnreachableServerKeepsTheRecordForTheNextSync() =
        runTest {
            val store = Store()
            val server = Server { """{"methodResponses": [["error", {"type": "serverFail"}, "c0"]]}""" }
            subscriptions(server, store).run {
                setEndpoint("ada", endpoint)
                sync()
                assertEquals(mapOf("ada" to PushStatus.Waiting), status())
            }
            assertFalse(store.records.isEmpty())
        }

    private companion object {
        const val SESSION = """
            {"capabilities": {"urn:ietf:params:jmap:core": {}, "urn:ietf:params:jmap:mail": {}},
             "accounts": {"d1": {"name": "Ada", "isPersonal": true,
               "accountCapabilities": {"urn:ietf:params:jmap:mail": {}}}},
             "primaryAccounts": {"urn:ietf:params:jmap:mail": "d1"},
             "username": "ada@example.org", "apiUrl": "/jmap/", "state": "s1"}
        """
    }
}
