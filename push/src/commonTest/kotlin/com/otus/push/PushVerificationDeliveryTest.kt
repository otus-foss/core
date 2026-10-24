package com.otus.push

import com.otus.jmap.JmapClient
import com.otus.jmap.JmapConnections
import com.otus.jmap.login.InMemoryLoginStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PushVerificationDeliveryTest {
    @Test
    fun pausesDeliveryBeforeCreatingVerificationButNotForAnUnchangedSubscription() =
        runTest {
            val events = mutableListOf<String>()
            val server =
                Server {
                    if ("PushSubscription/get" in it) {
                        events += "get"
                        NO_SUBSCRIPTIONS
                    } else {
                        events += "create"
                        created("2030-01-01T00:00:00Z")
                    }
                }
            val logins = InMemoryLoginStore().apply { save(login, "secret") }
            val subscriptions =
                PushSubscriptions(
                    JmapConnections(JmapClient(server.engine), logins),
                    logins,
                    Store(),
                    "device",
                    listOf("EmailDelivery"),
                    beforeCreate = { events += "pause:$it" },
                )
            subscriptions.setEndpoint(login.id, endpoint)
            subscriptions.sync()
            subscriptions.sync()
            assertEquals(listOf("get", "pause:ada", "create"), events)
        }

    @Test
    fun pausesAgainWhenTheServerDeletedAnExpiredSubscription() =
        runTest {
            val store = Store()
            subscribed(store, expires = "2026-10-15T00:00:00Z")
            val events = mutableListOf<String>()
            val server =
                Server { body ->
                    when {
                        "\"update\"" in body -> {
                            events += "update"
                            """{"methodResponses":[["PushSubscription/set",
                        {"notUpdated":{"ps1":{"type":"notFound"}}},"c0"]]}"""
                        }

                        "PushSubscription/get" in body -> {
                            events += "get"
                            NO_SUBSCRIPTIONS
                        }

                        else -> {
                            events += "create"
                            created()
                        }
                    }
                }
            subscriptions(
                server,
                store,
                at = kotlin.time.Instant.parse("2026-10-16T00:00:00Z"),
                beforeCreate = { events += "pause:$it" },
            ).sync()
            assertEquals(listOf("update", "get", "pause:ada", "create"), events)
        }

    @Test
    fun failedPauseDoesNotSendVerification() =
        runTest {
            val server = Server { NO_SUBSCRIPTIONS }
            val logins = InMemoryLoginStore().apply { save(login, "secret") }
            val subscriptions =
                PushSubscriptions(
                    JmapConnections(JmapClient(server.engine), logins),
                    logins,
                    Store(),
                    "device",
                    listOf("EmailDelivery"),
                    beforeCreate = { error("Bridge unavailable") },
                )
            subscriptions.setEndpoint(login.id, endpoint)
            assertFailsWith<IllegalStateException> { subscriptions.sync() }
            assertEquals(1, server.sent.size)
        }
}
