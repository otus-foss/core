package com.otus.jmap.push

import com.otus.jmap.JmapClient
import com.otus.jmap.JmapConnections
import com.otus.jmap.login.InMemoryLoginStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            val server = Server(events::subscriptionReply)
            subscriptions(
                server,
                store,
                at = kotlin.time.Instant.parse("2026-10-16T00:00:00Z"),
                beforeCreate = { events += "pause:$it" },
            ).sync()
            assertEquals(listOf("update", "get", "pause:ada", "create"), events)
        }

    @Test
    fun failedPauseSkipsOnlyThatLoginUntilTheNextSync() =
        runTest {
            val grace = login.copy(id = "grace")
            val server = subscribingServer()
            val logins =
                InMemoryLoginStore().apply {
                    save(login, "secret")
                    save(grace, "secret")
                }
            val store = Store()
            val subscriptions =
                subscriptions(server, store, logins = logins) { if (it == login.id) error("Bridge unavailable") }
            subscriptions.setEndpoint(login.id, endpoint)
            subscriptions.setEndpoint(grace.id, endpoint)
            subscriptions.sync()
            assertEquals(1, server.sent.count { "\"create\"" in it })
            // Grace's subscription is saved; Ada keeps her endpoint without one, to retry later.
            assertTrue("ps1" in store.records.getValue(grace.id))
            assertTrue("ps1" !in store.records.getValue(login.id))
        }
}

private fun MutableList<String>.subscriptionReply(body: String): String {
    val operation =
        when {
            "\"update\"" in body -> "update"
            "PushSubscription/get" in body -> "get"
            else -> "create"
        }
    add(operation)
    return when (operation) {
        "update" -> MISSING_SUBSCRIPTION
        "get" -> NO_SUBSCRIPTIONS
        else -> created()
    }
}

private const val MISSING_SUBSCRIPTION =
    """{"methodResponses":[["PushSubscription/set",
    {"notUpdated":{"ps1":{"type":"notFound"}}},"c0"]]}"""
