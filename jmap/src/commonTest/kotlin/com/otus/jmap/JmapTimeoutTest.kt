package com.otus.jmap

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JmapTimeoutTest {
    private val policy = JmapPolicy(requestTimeoutMillis = LIMIT_MILLIS)

    private val session =
        JmapSession(
            "https://mail.example.org/jmap/session",
            "https://mail.example.org/jmap/",
            "Ada",
            setOf(JmapCapability.CORE, JmapCapability.MAIL),
            mapOf("d1" to JmapAccount("Ada", isPersonal = true, isReadOnly = false, setOf(JmapCapability.MAIL))),
            emptyMap(),
            "s1",
            setOf("https://mail.example.org:443"),
            downloadUrl = "/download/{accountId}/{blobId}/{name}",
            uploadUrl = "/upload/{accountId}/",
        )

    @Test
    fun blobTransfersOutlastTheWholeCallLimit() =
        runTest {
            val client =
                JmapClient(
                    slowServer { request ->
                        if (request.url.encodedPath.startsWith("/upload/")) {
                            json("""{"accountId": "d1", "blobId": "G1", "type": "image/png", "size": 1}""")
                        } else {
                            respond(byteArrayOf(1))
                        }
                    },
                    policy,
                )
            val blob = client.download(session, ADA, "d1", "photo", "image/png", maxBytes = 10)
            assertContentEquals(byteArrayOf(1), blob.bytes)
            assertEquals("G1", client.upload(session, ADA, "d1", byteArrayOf(1), "image/png").blobId)
        }

    @Test
    fun apiCallsStillHaveTheWholeCallLimit() =
        runTest {
            val client = JmapClient(slowServer { json("""{"methodResponses": [], "sessionState": "s1"}""") }, policy)
            assertFailsWith<JmapException.Unreachable> {
                client.request(session, ADA) { call(GetMethod(MAILBOX, "d1")) }
            }
        }

    /** Answers every request after longer than [LIMIT_MILLIS] of virtual time. */
    private fun TestScope.slowServer(answer: MockRequestHandler): MockEngine =
        MockEngine(
            MockEngineConfig().apply {
                dispatcher = StandardTestDispatcher(testScheduler)
                addHandler { request ->
                    delay(SLOW_MILLIS)
                    answer(this, request)
                }
            },
        )

    private companion object {
        const val LIMIT_MILLIS = 1_000L
        const val SLOW_MILLIS = 5_000L
    }
}
