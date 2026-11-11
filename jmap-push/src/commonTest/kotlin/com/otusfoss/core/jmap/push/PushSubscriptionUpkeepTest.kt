package com.otusfoss.core.jmap.push

import com.otusfoss.core.jmap.PushMessage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Renewal, retries, and cleanup of subscriptions that already exist. */
class PushSubscriptionUpkeepTest {
    @Test
    fun anExpiringSubscriptionIsRenewedAndAnUnchangedOneIsLeftAlone() =
        runTest {
            val store = Store()
            subscribed(store)
            val quiet = Server { error("unexpected call: $it") }
            subscriptions(quiet, store, at = Instant.parse("2026-10-16T08:00:00Z")).sync()
            assertTrue(quiet.sent.isEmpty())

            val renew = Server { UPDATED }
            subscriptions(renew, store, at = Instant.parse("2026-10-19T08:00:00Z")).sync()
            val sent = renew.sent.single()
            assertTrue("\"update\":{\"ps1\":{\"expires\":\"2026-10-26T08:00:00Z\"}}" in sent, sent)
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
            createdSubscription(store).receive("ada", PushMessage.Verification("ps1", "wrong"))
            val server =
                Server {
                    when {
                        "verificationCode" in it -> INVALID_VERIFICATION
                        "PushSubscription/get" in it -> NO_SUBSCRIPTIONS
                        else -> created().replace("ps1", "ps2")
                    }
                }
            subscriptions(server, store).run {
                sync()
                assertEquals(mapOf("ada" to PushStatus.Waiting), status())
            }
            assertTrue("\"create\"" in server.sent.last(), server.sent.last())
        }

    @Test
    fun aSubscriptionThatIsNeverVerifiedIsCreatedAgain() =
        runTest {
            val store = Store()
            val firstServer = subscribingServer()
            createdSubscription(store, firstServer)
            val soon = Server { error("unexpected call: $it") }
            subscriptions(soon, store, at = now + 30.minutes).sync()
            val retry =
                Server {
                    if ("PushSubscription/get" in it) NO_SUBSCRIPTIONS else created().replace("ps1", "ps2")
                }
            subscriptions(retry, store, at = now + 2.hours).sync()
            val create = retry.sent.last()
            assertTrue("\"destroy\":[\"ps1\"]" in create, create)
        }

    @Test
    fun anUnverifiedSubscriptionWithAShortLifetimeIsCreatedAgainRatherThanRenewed() =
        runTest {
            val store = Store()
            createdSubscription(store, subscribingServer("2026-10-14T09:00:00Z"))
            val quiet = Server { error("unexpected call: $it") }
            subscriptions(quiet, store, at = now + 40.minutes).sync()
            val retry =
                Server {
                    if ("PushSubscription/get" in it) NO_SUBSCRIPTIONS else created().replace("ps1", "ps2")
                }
            subscriptions(retry, store, at = now + 70.minutes).sync()
            val create = retry.sent.last()
            assertTrue("\"create\"" in create && "\"destroy\":[\"ps1\"]" in create, create)
        }

    @Test
    fun aShortenedLifetimeIsRenewedHalfwayInsteadOfOnEverySync() =
        runTest {
            val store = Store()
            subscribed(store, "2026-10-15T08:00:00Z")
            val quiet = Server { error("unexpected call: $it") }
            subscriptions(quiet, store, at = now + 11.hours).sync()
            val renew = Server { UPDATED }
            subscriptions(renew, store, at = now + 13.hours).sync()
            assertTrue("\"expires\"" in renew.sent.single(), renew.sent.single())
        }

    @Test
    fun switchingToAPlainHttpEndpointDeletesTheOldSubscription() =
        runTest {
            val store = Store()
            createdSubscription(store)
            val server = Server { DESTROYED }
            subscriptions(server, store).run {
                setEndpoint("ada", endpoint.copy(url = "http://ntfy.lan/upA"))
                sync()
                assertEquals(mapOf("ada" to PushStatus.Refused), status())
            }
            assertTrue("\"destroy\":[\"ps1\"]" in server.sent.single(), server.sent.single())
        }

    @Test
    fun forgettingALoginDeletesItsSubscriptionAndRecord() =
        runTest {
            val store = Store()
            createdSubscription(store)
            val server = Server { DESTROYED }
            subscriptions(server, store).forget("ada")
            assertTrue("\"destroy\":[\"ps1\"]" in server.sent.single(), server.sent.single())
            assertTrue(store.records.isEmpty())
        }
}

private const val INVALID_VERIFICATION =
    """{"methodResponses": [["PushSubscription/set",
       {"notUpdated": {"ps1": {"type": "invalidProperties"}}}, "c0"]]}"""
