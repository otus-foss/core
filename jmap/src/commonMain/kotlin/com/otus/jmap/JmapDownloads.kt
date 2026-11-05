package com.otus.jmap

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.utils.io.readAvailable
import kotlinx.io.IOException

public class JmapBlob(
    public val bytes: ByteArray,
    public val mediaType: String,
)

internal class JmapDownloads(
    private val http: HttpClient,
    private val urls: JmapUrls,
    private val policy: JmapPolicy,
) {
    suspend fun download(
        session: JmapSession,
        auth: JmapAuth,
        accountId: String,
        blobId: String,
        mediaType: String,
        maxBytes: Long,
        name: String,
    ): JmapBlob {
        require(maxBytes > 0 && maxBytes <= policy.maxResponseBytes)
        return fetch(session, auth, accountId, blobId, mediaType, name) { response ->
            JmapBlob(response.limitedBytes(maxBytes), response.mediaType(mediaType))
        }
    }

    suspend fun stream(
        session: JmapSession,
        auth: JmapAuth,
        accountId: String,
        blobId: String,
        mediaType: String,
        name: String,
        receive: suspend (ByteArray) -> Unit,
    ): String =
        fetch(session, auth, accountId, blobId, mediaType, name) { response ->
            val body = response.bodyAsChannel()
            val buffer = ByteArray(STREAM_CHUNK_BYTES)
            while (true) {
                val read = body.readAvailable(buffer)
                if (read < 0) break
                if (read > 0) receive(buffer.copyOf(read))
            }
            response.mediaType(mediaType)
        }

    private suspend fun <T> fetch(
        session: JmapSession,
        auth: JmapAuth,
        accountId: String,
        blobId: String,
        mediaType: String,
        name: String,
        read: suspend (HttpResponse) -> T,
    ): T {
        require(accountId in session.accounts)
        val target = downloadTarget(session, accountId, blobId, mediaType, name)
        return try {
            http
                .prepareGet(target) {
                    header(HttpHeaders.Authorization, auth.header())
                    header(HttpHeaders.Accept, mediaType)
                    blobTransfer()
                }.execute { response ->
                    if (response.status == HttpStatusCode.NotFound) throw JmapException.BlobNotFound(blobId)
                    RawResponse(response.status.value, location = null, text = "").requireSuccess()
                    read(response)
                }
        } catch (error: IOException) {
            throw JmapException.Unreachable(error)
        }
    }

    private fun downloadTarget(
        session: JmapSession,
        accountId: String,
        blobId: String,
        mediaType: String,
        name: String,
    ): Url {
        val template = session.downloadUrl ?: throw JmapException.NotJmap("no blob download endpoint")
        // A file name comes from the message's sender, so "." or ".." falls back to the default.
        val fileName = name.takeUnless(String::isDotSegment) ?: DEFAULT_DOWNLOAD_NAME
        val values = mapOf("accountId" to accountId, "blobId" to blobId, "type" to mediaType, "name" to fileName)
        return urls.trustedTarget(session, expandUriTemplate(template, values))
    }
}

/** The media type the server sent, without parameters; [requested] when it sent none. */
private fun HttpResponse.mediaType(requested: String): String =
    headers[HttpHeaders.ContentType]?.substringBefore(';') ?: requested

/** The `{name}` for a download whose caller has no file name (RFC 8620, section 6.2). */
internal const val DEFAULT_DOWNLOAD_NAME = "download"

/** The most bytes one streamed piece holds. */
private const val STREAM_CHUNK_BYTES = 64 * 1024
