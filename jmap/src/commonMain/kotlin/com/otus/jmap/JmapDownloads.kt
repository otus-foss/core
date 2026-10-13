package com.otus.jmap

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.utils.io.readBuffer
import kotlinx.io.IOException
import kotlinx.io.readByteArray

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
        require(accountId in session.accounts)
        val target = downloadTarget(session, accountId, blobId, mediaType, name)
        return try {
            http
                .prepareGet(target) {
                    header(HttpHeaders.Authorization, auth.header())
                    header(HttpHeaders.Accept, mediaType)
                }.execute { response ->
                    RawResponse(response.status.value, null, "").failure()?.let { throw it }
                    checkBlobSize(response.contentLength() ?: 0, maxBytes)
                    val bytes = response.bodyAsChannel().readBuffer(maxBytes + 1).readByteArray()
                    checkBlobSize(bytes.size.toLong(), maxBytes)
                    JmapBlob(bytes, response.headers[HttpHeaders.ContentType]?.substringBefore(';') ?: mediaType)
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
        val values = mapOf("accountId" to accountId, "blobId" to blobId, "type" to mediaType, "name" to name)
        return urls.trustedTarget(session, expandUriTemplate(template, values))
    }
}

/** The `{name}` for a download whose caller has no file name (RFC 8620, section 6.2). */
internal const val DEFAULT_DOWNLOAD_NAME = "download"

private fun checkBlobSize(
    size: Long,
    limit: Long,
) {
    if (size > limit) throw JmapException.ResponseTooLarge(limit)
}
