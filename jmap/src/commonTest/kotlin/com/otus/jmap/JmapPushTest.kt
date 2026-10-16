package com.otus.jmap

import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JmapPushTest {
    private val wellKnown = "https://mail.example.org/.well-known/jmap"

    @Test
    fun createsASubscriptionWithoutAnAccount() =
        runTest {
            val (engine, requests) =
                server {
                    """{"methodResponses": [["PushSubscription/set",
                       {"created": {"p": {"id": "ps1", "expires": "2026-10-20T00:00:00Z"}}}, "c0"]]}"""
                }
            val client = JmapClient(engine)
            val method =
                PushSubscriptionSetMethod(
                    create =
                        mapOf(
                            "p" to
                                NewPushSubscription(
                                    deviceClientId = "otus-mail-1",
                                    url = "https://push.example.net/up/abc",
                                    keys = PushKeys("BPk", "c2Vj"),
                                    types = listOf("Email", "Mailbox"),
                                ),
                        ),
                )
            val result = client.request(client.session(wellKnown, ADA), ADA) { call(method) }
            assertEquals("ps1", result.createdId("p"))
            assertEquals("", result.newState)
            val body = requests.last().body
            listOf(
                "\"using\":[\"urn:ietf:params:jmap:core\"]",
                "\"url\":\"https://push.example.net/up/abc\"",
                "\"keys\":{\"p256dh\":\"BPk\",\"auth\":\"c2Vj\"}",
                "\"types\":[\"Email\",\"Mailbox\"]",
            ).forEach { assertTrue(it in body, body) }
            assertFalse("accountId" in body, body)
            assertFalse("expires" in body, body)
        }

    @Test
    fun verifiesAndListsSubscriptions() =
        runTest {
            val (engine, requests) =
                server {
                    """{"methodResponses": [
                       ["PushSubscription/set", {"updated": {"ps1": null}}, "c0"],
                       ["PushSubscription/get", {"list": [{"id": "ps1", "deviceClientId": "otus-mail-1",
                         "verificationCode": "v1", "types": null}], "notFound": []}, "c1"]]}"""
                }
            val client = JmapClient(engine)
            val code = buildJsonObject { put("verificationCode", JsonPrimitive("v1")) }
            val calls = JmapRequestBuilder()
            val verify = calls.call(PushSubscriptionSetMethod(update = mapOf("ps1" to code)))
            val list = calls.call(PushSubscriptionGetMethod())
            val response = client.send(client.session(wellKnown, ADA), ADA, calls.calls)
            assertTrue("ps1" in response[verify].updated)
            assertEquals(listOf(PushSubscription("ps1", "otus-mail-1", verificationCode = "v1")), response[list].list)
            val body = requests.last().body
            assertTrue("\"update\":{\"ps1\":{\"verificationCode\":\"v1\"}}" in body, body)
            assertTrue("\"ids\":null" in body, body)
        }

    @Test
    fun aSetWithStateStillNeedsNewState() =
        runTest {
            val (engine, _) = server { """{"methodResponses": [["Mailbox/set", {"destroyed": ["b"]}, "c0"]]}""" }
            val client = JmapClient(engine)
            assertFailsWith<JmapException.NotJmap> {
                client.request(
                    client.session(wellKnown, ADA),
                    ADA,
                ) { call(SetMethod(MAILBOX, "d1", destroy = listOf("b"))) }
            }
        }

    @Test
    fun decodesStateChangesAndVerifications() {
        assertEquals(
            PushMessage.StateChange(mapOf("d1" to mapOf("Email" to "e9", "Mailbox" to "m3"))),
            PushMessage.decode("""{"@type": "StateChange", "changed": {"d1": {"Email": "e9", "Mailbox": "m3"}}}"""),
        )
        assertEquals(
            PushMessage.Verification("ps1", "v1"),
            PushMessage.decode(
                """{"@type": "PushVerification", "pushSubscriptionId": "ps1", "verificationCode": "v1"}""",
            ),
        )
    }

    @Test
    fun skipsUnknownMessagesAndRejectsBrokenOnes() {
        assertNull(PushMessage.decode("""{"@type": "EmailPush", "accountId": "d1"}"""))
        assertFailsWith<JmapException.NotJmap> { PushMessage.decode("""{"@type": "StateChange"}""") }
        assertFailsWith<JmapException.NotJmap> { PushMessage.decode("not json") }
    }

    @Test
    fun refusesPlainHttpPushUrls() {
        assertFailsWith<IllegalArgumentException> {
            NewPushSubscription(
                "otus-mail-1",
                "http://push.example.net/up/abc",
            )
        }
    }

    @Test
    fun readsTheVapidKeyWhenAdvertised() =
        runTest {
            val vapid = """, "urn:ietf:params:jmap:webpush-vapid": {"applicationServerKey": "BKey"}"""
            val engine =
                MockEngine {
                    json(
                        session().replaceFirst(
                            "\"urn:ietf:params:jmap:core\": {}",
                            "\"urn:ietf:params:jmap:core\": {}$vapid",
                        ),
                    )
                }
            assertEquals(
                "BKey",
                JmapClient(engine).session("https://mail.example.org/jmap/session", ADA).vapidPublicKey,
            )
            val (plain, _) = server()
            assertNull(JmapClient(plain).session(wellKnown, ADA).vapidPublicKey)
        }
}
