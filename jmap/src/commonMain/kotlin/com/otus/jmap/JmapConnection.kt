package com.otus.jmap

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * One signed-in server. Caches the session, refetches it when a response reports a new session
 * state (RFC 8620, section 3.4), and drops it after a failure so the next request starts fresh.
 * A missing blob or an unapproved origin keeps the session, because a new one would not help.
 */
public class JmapConnection(
    private val client: JmapClient,
    public val sessionUrl: String,
    private val auth: JmapAuth,
    private val trustedOrigins: Set<String> = emptySet(),
) {
    private val lock = Mutex()
    private var cached: JmapSession? = null

    public suspend fun session(): JmapSession =
        lock.withLock {
            val current = cached
            if (current != null) return@withLock current
            val fetched = client.session(sessionUrl, auth, trustedOrigins)
            cached = fetched
            fetched
        }

    /** One request; returns the result of the call [build] returns. */
    public suspend fun <T> request(build: JmapRequestBuilder.() -> JmapCall<T>): T {
        val builder = JmapRequestBuilder()
        val result = builder.build()
        return exchange(builder.calls)[result]
    }

    /** One request with a single call. */
    public suspend fun <T> call(method: JmapMethod<T>): T = request { call(method) }

    /** Downloads through [JmapClient.download]; [name] is the file name the server gives the blob. */
    public suspend fun download(
        accountId: String,
        blobId: String,
        mediaType: String,
        maxBytes: Long,
        name: String = DEFAULT_DOWNLOAD_NAME,
    ): JmapBlob = withSession { client.download(it, auth, accountId, blobId, mediaType, maxBytes, name) }

    /** Streams through [JmapClient.downloadStream]; [receive] gets each piece as it arrives. */
    public suspend fun downloadStream(
        accountId: String,
        blobId: String,
        mediaType: String,
        name: String = DEFAULT_DOWNLOAD_NAME,
        receive: suspend (ByteArray) -> Unit,
    ): String = withSession { client.downloadStream(it, auth, accountId, blobId, mediaType, name, receive) }

    public suspend fun upload(
        accountId: String,
        bytes: ByteArray,
        mediaType: String,
    ): JmapUpload = withSession { client.upload(it, auth, accountId, bytes, mediaType) }

    /** One request; read any call's result from the response. */
    public suspend fun send(build: JmapRequestBuilder.() -> Unit): JmapResponse =
        exchange(JmapRequestBuilder().apply(build).calls)

    private suspend fun exchange(calls: List<JmapCall<*>>): JmapResponse =
        withSession { session ->
            val response = client.send(session, auth, calls)
            if (response.isOutdated(session)) invalidate(session)
            response
        }

    private suspend fun <T> withSession(block: suspend (JmapSession) -> T): T {
        val session = session()
        return try {
            block(session)
        } catch (error: JmapException) {
            // Never replay a failed mutation; refresh discovery on the caller's next attempt.
            if (error.mayMeanStaleSession()) invalidate(session)
            throw error
        }
    }

    private suspend fun invalidate(session: JmapSession) {
        lock.withLock { if (cached === session) cached = null }
    }

    public suspend fun invalidate() {
        lock.withLock { cached = null }
    }
}

/**
 * Whether a fresh session could change this outcome. A missing blob does not, and an origin the user
 * has not approved stays unapproved, so neither costs another session request.
 */
private fun JmapException.mayMeanStaleSession(): Boolean =
    this !is JmapException.BlobNotFound && this !is JmapException.RedirectedElsewhere
