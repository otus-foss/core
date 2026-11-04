package com.otus.jmap

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Provider-independent JMAP core client (RFC 8620). One instance per app is enough; it is safe to
 * share. Call [close] when its owner ends. An injected [engine] stays caller-owned and must be
 * closed separately; without one, the client owns the platform engine on the classpath.
 */
public class JmapClient(
    engine: HttpClientEngine? = null,
    private val policy: JmapPolicy = JmapPolicy(),
) : AutoCloseable {
    private val http = if (engine == null) HttpClient { configure() } else HttpClient(engine) { configure() }
    private val urls = JmapUrls(policy)

    /**
     * Stops accepting requests and releases this client's resources when active requests finish.
     * Safe to call repeatedly. An injected engine remains open for its caller to close or reuse.
     */
    public override fun close() {
        http.close()
    }

    /**
     * Authenticated, bounded blob download (RFC 8620, section 6.2). No redirect receives credentials.
     * [name] fills the template's `{name}`, the file name a server gives in `Content-Disposition`;
     * it defaults to `download`. A blob the server does not have throws [JmapException.BlobNotFound].
     */
    public suspend fun download(
        session: JmapSession,
        auth: JmapAuth,
        accountId: String,
        blobId: String,
        mediaType: String,
        maxBytes: Long,
        name: String = DEFAULT_DOWNLOAD_NAME,
    ): JmapBlob =
        JmapDownloads(http, urls, policy).download(session, auth, accountId, blobId, mediaType, maxBytes, name)

    /**
     * Streams a blob as [download] does, but without a size limit: each piece goes to [receive] as
     * it arrives, so a caller can write a large file to disk without holding it in memory. Returns
     * the media type the server sent. Cancelling the caller's coroutine ends the transfer.
     */
    public suspend fun downloadStream(
        session: JmapSession,
        auth: JmapAuth,
        accountId: String,
        blobId: String,
        mediaType: String,
        name: String = DEFAULT_DOWNLOAD_NAME,
        receive: suspend (ByteArray) -> Unit,
    ): String = JmapDownloads(http, urls, policy).stream(session, auth, accountId, blobId, mediaType, name, receive)

    /** Authenticated blob upload (RFC 8620, section 6.1), within the server's `maxSizeUpload`. */
    public suspend fun upload(
        session: JmapSession,
        auth: JmapAuth,
        accountId: String,
        bytes: ByteArray,
        mediaType: String,
    ): JmapUpload = JmapUploads(http, urls, policy).upload(session, auth, accountId, bytes, mediaType)

    /**
     * Fetches the session at [url], following redirects such as `/.well-known/jmap` (section 2.2).
     * Credentials only go to the origin of [url] and to [trustedOrigins]; a redirect elsewhere throws
     * [JmapException.RedirectedElsewhere] before any credentials are sent there, and so does an
     * `apiUrl` on another origin.
     *
     * RFC 8620 allows `downloadUrl` and `uploadUrl` on yet another origin. Each blob request checks
     * its own URL, so such a session still serves API calls. Pass [requireTrustedBlobOrigins] when
     * signing in to also throw [JmapException.RedirectedElsewhere] for an untrusted blob origin, so
     * the user can approve it before the login is saved.
     */
    public suspend fun session(
        url: String,
        auth: JmapAuth,
        trustedOrigins: Set<String> = emptySet(),
        requireTrustedBlobOrigins: Boolean = false,
    ): JmapSession {
        var target = urls.parse(url)
        val trusted = trustedOrigins + target.origin()
        repeat(policy.maxRedirects + 1) {
            val authorized = target.origin() in trusted
            val response = exchange(HttpMethod.Get, target, auth.takeIf { authorized }, body = null)
            val location = response.location
            if (response.status in REDIRECT_STATUSES && location != null) {
                target = urls.resolve(target, location)
            } else {
                val session = parseSession(target.requireTrusted(trusted), response, trusted)
                if (requireTrustedBlobOrigins) urls.requireTrustedBlobOrigins(session)
                return session
            }
        }
        throw JmapException.TooManyRedirects()
    }

    /** Sends the calls built by [build] in one request (section 3.3) and returns the call [build] returns. */
    public suspend fun <T> request(
        session: JmapSession,
        auth: JmapAuth,
        build: JmapRequestBuilder.() -> JmapCall<T>,
    ): T {
        val builder = JmapRequestBuilder()
        val result = builder.build()
        return send(session, auth, builder.calls)[result]
    }

    /** Sends the calls built by [build] in one request; read any call's result from the response. */
    public suspend fun send(
        session: JmapSession,
        auth: JmapAuth,
        build: JmapRequestBuilder.() -> Unit,
    ): JmapResponse = send(session, auth, JmapRequestBuilder().apply(build).calls)

    internal suspend fun send(
        session: JmapSession,
        auth: JmapAuth,
        calls: List<JmapCall<*>>,
    ): JmapResponse {
        val using = linkedSetOf(JmapCapability.CORE) + calls.map { it.method.capability }
        checkSupported(session, calls, using)
        val body =
            buildJsonObject {
                put("using", buildJsonArray { using.forEach { add(JsonPrimitive(it)) } })
                put("methodCalls", buildJsonArray { calls.forEach { add(it.toJson()) } })
            }
        val api = urls.parse(session.apiUrl).requireTrusted(session.trustedOrigins)
        val response = exchange(HttpMethod.Post, api, auth, body.toString())
        return jmapDecoding { parseResponse(decode(response)) }
    }

    private fun parseSession(
        url: Url,
        response: RawResponse,
        trusted: Set<String>,
    ): JmapSession {
        val dto = jmapDecoding { JmapJson.decodeFromJsonElement(SessionDto.serializer(), decode(response)) }
        return JmapSession(
            sessionUrl = url.toString(),
            apiUrl = urls.resolve(url, dto.apiUrl).requireTrusted(trusted).toString(),
            username = dto.username,
            capabilities = dto.capabilities.keys,
            accounts =
                dto.accounts.mapValues { (_, account) ->
                    JmapAccount(
                        account.name,
                        account.isPersonal,
                        account.isReadOnly,
                        account.accountCapabilities.keys,
                        account.accountCapabilities.capabilityObjects(),
                    )
                },
            primaryAccounts = dto.primaryAccounts,
            state = dto.state,
            trustedOrigins = trusted,
            downloadUrl = dto.downloadUrl,
            uploadUrl = dto.uploadUrl,
            capabilityObjects = dto.capabilities.capabilityObjects(),
        )
    }

    private suspend fun exchange(
        method: HttpMethod,
        target: Url,
        auth: JmapAuth?,
        body: String?,
    ): RawResponse =
        try {
            http
                .prepareRequest {
                    this.method = method
                    url(target)
                    if (auth != null) header(HttpHeaders.Authorization, auth.header())
                    header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    if (body != null) {
                        contentType(ContentType.Application.Json)
                        setBody(body)
                    }
                }.execute { response ->
                    RawResponse(
                        response.status.value,
                        response.headers[HttpHeaders.Location],
                        response.limitedText(policy.maxResponseBytes),
                    )
                }
        } catch (error: IOException) {
            throw JmapException.Unreachable(error)
        }

    private fun HttpClientConfig<*>.configure() {
        followRedirects = false
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = policy.connectTimeoutMillis
            requestTimeoutMillis = policy.requestTimeoutMillis
            socketTimeoutMillis = policy.socketTimeoutMillis
        }
    }

    private companion object {
        val REDIRECT_STATUSES = setOf(301, 302, 303, 307, 308)
    }
}

/**
 * Throws [JmapException.RedirectedElsewhere] for a blob endpoint on an untrusted origin, so the
 * user can approve it at sign-in instead of every later upload and download failing. A template
 * that is not an allowed URL is refused only when it is used.
 */
private fun JmapUrls.requireTrustedBlobOrigins(session: JmapSession) {
    val base = parse(session.sessionUrl)
    listOfNotNull(session.downloadUrl, session.uploadUrl)
        .mapNotNull { template -> templateTarget(base, template) }
        .forEach { target -> target.requireTrusted(session.trustedOrigins) }
}

/** Checks the session and every account each call touches before anything is sent. */
private fun checkSupported(
    session: JmapSession,
    calls: List<JmapCall<*>>,
    using: Set<String>,
) {
    val missing =
        using.firstOrNull { it !in session.capabilities }
            ?: calls.firstOrNull { it.lacksAccountCapability(session) }?.method?.capability
    if (missing != null) throw JmapException.Unsupported(missing)
}

private fun JmapCall<*>.lacksAccountCapability(session: JmapSession): Boolean =
    method.accountIds.any { method.capability !in session.accounts[it]?.capabilities.orEmpty() }

private fun JmapCall<*>.toJson() =
    buildJsonArray {
        add(JsonPrimitive(method.name))
        add(method.arguments())
        add(JsonPrimitive(id))
    }

private fun decode(response: RawResponse): JsonObject {
    response.requireSuccess()
    return jmapDecoding { JmapJson.parseToJsonElement(response.text).jsonObject }
}
