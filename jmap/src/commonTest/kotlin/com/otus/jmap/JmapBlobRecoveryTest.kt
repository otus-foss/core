package com.otus.jmap

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class JmapBlobRecoveryTest {
    @Test
    fun uploadAndDownloadRediscoverAfterEndpointFailuresWithoutAutomaticReplay() =
        runTest {
            for (upload in listOf(false, true)) {
                var sessions = 0
                var transfers = 0
                val client =
                    JmapClient(
                        MockEngine { request ->
                            when (request.url.encodedPath) {
                                "/session" -> {
                                    sessions++
                                    json(blobSession(if (sessions == 1) "old" else "new"))
                                }

                                else -> {
                                    transfers++
                                    if (request.url.encodedPath.startsWith("/old/")) {
                                        respond("", HttpStatusCode.NotFound)
                                    } else if (upload) {
                                        json("""{"accountId":"d1","blobId":"photo","type":"image/png","size":1}""")
                                    } else {
                                        respond(
                                            byteArrayOf(1),
                                            headers = headersOf(HttpHeaders.ContentType, "image/png"),
                                        )
                                    }
                                }
                            }
                        },
                    )
                val connection = JmapConnection(client, "https://example.org/session", ADA)
                assertFailsWith<JmapException.NotJmap> { transfer(connection, upload) }
                assertEquals(1, transfers)
                assertEquals(1, sessions)
                transfer(connection, upload)
                assertEquals(2, sessions)
                assertEquals(2, transfers)
            }
        }

    @Test
    fun cancellationDoesNotDiscardTheSession() =
        runTest {
            var sessions = 0
            val client =
                JmapClient(
                    MockEngine { request ->
                        if (request.url.encodedPath == "/session") {
                            sessions++
                            json(blobSession("old"))
                        } else {
                            throw CancellationException("cancelled")
                        }
                    },
                )
            val connection = JmapConnection(client, "https://example.org/session", ADA)
            for (upload in listOf(false, true)) {
                assertFailsWith<CancellationException> { transfer(connection, upload) }
            }
            connection.session()
            assertEquals(1, sessions)
        }

    @Test
    fun aLateBlobFailureDoesNotDiscardANewerSession() =
        runTest {
            val started = CompletableDeferred<Unit>()
            val finish = CompletableDeferred<Unit>()
            val client =
                JmapClient(
                    MockEngine { request ->
                        if (request.url.encodedPath == "/session") {
                            json(blobSession("old"))
                        } else {
                            started.complete(Unit)
                            finish.await()
                            respond("", HttpStatusCode.NotFound)
                        }
                    },
                )
            val connection = JmapConnection(client, "https://example.org/session", ADA)
            val download = async { assertFailsWith<JmapException.NotJmap> { transfer(connection, false) } }
            started.await()
            connection.invalidate()
            val replacement = connection.session()
            finish.complete(Unit)
            download.await()
            assertSame(replacement, connection.session())
        }

    private suspend fun transfer(
        connection: JmapConnection,
        upload: Boolean,
    ) {
        if (upload) {
            connection.upload("d1", byteArrayOf(1), "image/png")
        } else {
            assertContentEquals(byteArrayOf(1), connection.download("d1", "photo", "image/png", 100).bytes)
        }
    }

    private fun blobSession(endpoint: String): String =
        session().dropLast(1) +
            """, "downloadUrl":"/$endpoint/{accountId}/{blobId}/{name}?type={type}",
        "uploadUrl":"/$endpoint/{accountId}"}"""
}
