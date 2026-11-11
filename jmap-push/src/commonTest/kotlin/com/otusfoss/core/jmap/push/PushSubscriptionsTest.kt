package com.otusfoss.core.jmap.push

import com.otusfoss.core.jmap.PushMessage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PushSubscriptionsTest {
    @Test
    fun anEndpointBecomesASubscriptionThatAVerificationActivates() =
        runTest {
            val store = Store()
            val server = subscribingServer()
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
            val verify = Server { UPDATED }
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
            val first = subscribingServer()
            createdSubscription(store, first)
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
    fun pushingMoreTypesReplacesTheSubscription() =
        runTest {
            val store = Store()
            subscribed(store)
            val server = subscribingServer()
            subscriptions(server, store, types = listOf("EmailDelivery", "Email")).sync()
            val replace = server.sent.last()
            assertTrue("\"types\":[\"EmailDelivery\",\"Email\"]" in replace, replace)
            assertTrue("\"destroy\":[\"ps1\"]" in replace, replace)
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
    fun clearingTheEndpointDeletesTheSubscription() =
        runTest {
            val store = Store()
            createdSubscription(store)
            val server = Server { DESTROYED }
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
}
