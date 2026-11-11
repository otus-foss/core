package com.otusfoss.core.jmap

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
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
                val server = BlobRecoveryServer(upload)
                val connection = JmapConnection(JmapClient(server.engine), "https://example.org/session", ADA)
                assertFailsWith<JmapException.RequestFailed> { transfer(connection, upload) }
                assertEquals(1, server.transfers)
                assertEquals(1, server.sessions)
                transfer(connection, upload)
                assertEquals(2, server.sessions)
                assertEquals(2, server.transfers)
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
                            respond("", HttpStatusCode.ServiceUnavailable)
                        }
                    },
                )
            val connection = JmapConnection(client, "https://example.org/session", ADA)
            val download =
                async { assertFailsWith<JmapException.RequestFailed> { transfer(connection, upload = false) } }
            started.await()
            connection.invalidate()
            val replacement = connection.session()
            finish.complete(Unit)
            download.await()
            assertSame(replacement, connection.session())
        }

    @Test
    fun aMissingBlobKeepsTheSession() =
        runTest {
            var sessions = 0
            val client =
                JmapClient(
                    MockEngine { request ->
                        if (request.url.encodedPath == "/session") {
                            sessions++
                            json(blobSession("old"))
                        } else {
                            respond("", HttpStatusCode.NotFound)
                        }
                    },
                )
            val connection = JmapConnection(client, "https://example.org/session", ADA)
            val error = assertFailsWith<JmapException.BlobNotFound> { transfer(connection, upload = false) }
            assertEquals("photo", error.blobId)
            assertFailsWith<JmapException.BlobNotFound> { transfer(connection, upload = false) }
            assertEquals(1, sessions)
        }

    @Test
    fun anUnapprovedBlobHostKeepsTheSession() =
        runTest {
            var sessions = 0
            val elsewhere = session().dropLast(1) + """, "downloadUrl": "https://files.example/{blobId}"}"""
            val client =
                JmapClient(
                    MockEngine {
                        sessions++
                        json(elsewhere)
                    },
                )
            val connection = JmapConnection(client, "https://example.org/session", ADA)
            val error = assertFailsWith<JmapException.RedirectedElsewhere> { transfer(connection, upload = false) }
            assertEquals("https://files.example:443", error.origin)
            connection.session()
            assertEquals(1, sessions)
        }

    private suspend fun transfer(
        connection: JmapConnection,
        upload: Boolean,
    ) {
        if (upload) {
            connection.upload("d1", byteArrayOf(1), "image/png")
        } else {
            assertContentEquals(byteArrayOf(1), connection.download("d1", "photo", "image/png", maxBytes = 100).bytes)
        }
    }
}

private fun blobSession(endpoint: String): String =
    session().dropLast(1) +
        """, "downloadUrl":"/$endpoint/{accountId}/{blobId}/{name}?type={type}",
        "uploadUrl":"/$endpoint/{accountId}"}"""

private class BlobRecoveryServer(
    private val upload: Boolean,
) {
    var sessions = 0
    var transfers = 0
    val engine =
        MockEngine { request ->
            if (request.url.encodedPath == "/session") sessionResponse() else transferResponse(request.url.encodedPath)
        }

    private fun MockRequestHandleScope.sessionResponse(): HttpResponseData {
        sessions++
        return json(blobSession(if (sessions == 1) "old" else "new"))
    }

    private fun MockRequestHandleScope.transferResponse(path: String): HttpResponseData {
        transfers++
        return when {
            path.startsWith("/old/") -> respond("", HttpStatusCode.ServiceUnavailable)
            upload -> json("""{"accountId":"d1","blobId":"photo","type":"image/png","size":1}""")
            else -> respond(byteArrayOf(1), headers = headersOf(HttpHeaders.ContentType, "image/png"))
        }
    }
}
