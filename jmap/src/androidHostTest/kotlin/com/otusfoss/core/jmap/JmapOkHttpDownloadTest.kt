package com.otusfoss.core.jmap

import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.test.runTest
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

/** Downloads through the real OkHttp engine, which checks header values more strictly than the mock engine. */
class JmapOkHttpDownloadTest {
    @Test
    fun aNonAsciiMediaTypeDownloadsAsOctetStream() =
        runTest {
            ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { listener ->
                var request = ""
                val answering =
                    thread(isDaemon = true) {
                        listener.accept().use { socket ->
                            val buffer = ByteArray(REQUEST_BYTES)
                            request = String(buffer, 0, socket.getInputStream().read(buffer))
                            socket.getOutputStream().write(RESPONSE.toByteArray())
                        }
                    }
                val origin = "http://127.0.0.1:${listener.localPort}"
                val client = JmapClient(OkHttp.create(), JmapPolicy(allowInsecureHttp = true))
                val blob = client.download(session(origin), ADA, "contacts", "b1", "image/pñg", 10)
                answering.join()
                assertContentEquals("x".toByteArray(), blob.bytes)
                assertTrue("Accept: application/octet-stream" in request, request)
            }
        }

    private fun session(origin: String): JmapSession =
        JmapSession(
            "$origin/jmap/session",
            "$origin/jmap/",
            "Ada",
            setOf(JmapCapability.CORE),
            mapOf("contacts" to JmapAccount("Ada", isPersonal = true, isReadOnly = false, emptySet())),
            emptyMap(),
            "s",
            setOf(origin),
            "/download/{accountId}/{blobId}/{name}?type={type}",
        )
}

private const val REQUEST_BYTES = 4096

private const val RESPONSE =
    "HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nContent-Length: 1\r\nConnection: close\r\n\r\nx"
