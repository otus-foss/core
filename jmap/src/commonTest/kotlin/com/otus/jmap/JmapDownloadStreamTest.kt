package com.otus.jmap

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JmapDownloadStreamTest {
    private val pdfHeaders = headersOf(HttpHeaders.ContentType, "application/pdf; charset=binary")

    private fun session(template: String): JmapSession =
        JmapSession(
            "https://mail.example.org/jmap/session",
            "https://mail.example.org/jmap/",
            "Ada",
            setOf(JmapCapability.CORE),
            mapOf("mail" to JmapAccount("Ada", isPersonal = true, isReadOnly = false, emptySet())),
            emptyMap(),
            "s",
            setOf("https://mail.example.org:443"),
            template,
        )

    @Test
    fun aBlobLargerThanTheResponseLimitArrivesInPieces() =
        runTest {
            val blob = ByteArray(20 * 1024 * 1024) { (it % 251).toByte() }
            val client =
                JmapClient(
                    MockEngine { request ->
                        assertEquals(ADA.header(), request.headers[HttpHeaders.Authorization])
                        respond(blob, headers = pdfHeaders)
                    },
                )
            val pieces = mutableListOf<ByteArray>()
            val type =
                client.downloadStream(session("/download/{blobId}/{name}"), ADA, "mail", "b1", "application/pdf") {
                    pieces += it
                }
            assertEquals("application/pdf", type)
            assertTrue(pieces.size > 1)
            assertContentEquals(blob, pieces.reduce(ByteArray::plus))
        }

    @Test
    fun aMissingBlobThrowsBeforeAnyPiece() =
        runTest {
            val client = JmapClient(MockEngine { respond("", HttpStatusCode.NotFound) })
            var pieces = 0
            assertFailsWith<JmapException.BlobNotFound> {
                client.downloadStream(session("/download/{blobId}"), ADA, "mail", "gone", "application/pdf") {
                    pieces++
                }
            }
            assertEquals(0, pieces)
        }

    @Test
    fun anUntrustedHostReceivesNoRequest() =
        runTest {
            val client = JmapClient(MockEngine { error("Untrusted host must not receive a request") })
            assertFailsWith<JmapException.RedirectedElsewhere> {
                client.downloadStream(session("https://evil.example/{blobId}"), ADA, "mail", "b1", "image/png") {}
            }
        }

    @Test
    fun anErrorInTheReceiverEndsTheTransfer() =
        runTest {
            val client = JmapClient(MockEngine { respond(ByteArray(1024 * 1024), headers = pdfHeaders) })
            var pieces = 0
            assertFailsWith<IllegalStateException> {
                client.downloadStream(session("/download/{blobId}"), ADA, "mail", "b1", "application/pdf") {
                    pieces++
                    error("disk full")
                }
            }
            assertEquals(1, pieces)
        }
}
