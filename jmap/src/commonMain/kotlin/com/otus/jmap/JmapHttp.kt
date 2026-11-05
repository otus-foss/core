package com.otus.jmap

import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLBuilder
import io.ktor.http.URLParserException
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.http.encodeURLParameter
import io.ktor.http.takeFrom
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import kotlinx.serialization.json.jsonObject

/** Parses and resolves URLs, refusing anything the policy does not allow. */
internal class JmapUrls(
    private val policy: JmapPolicy,
) {
    fun parse(text: String): Url = checked(build(text) { takeFrom(text) }, text)

    fun resolve(
        base: Url,
        reference: String,
    ): Url = parse(resolveUriReference(base.toString(), reference))

    /** Resolves an expanded session template, refusing origins that may not receive credentials. */
    fun trustedTarget(
        session: JmapSession,
        expanded: String,
    ): Url = resolve(parse(session.sessionUrl), expanded).requireTrusted(session.trustedOrigins)

    /**
     * Where a session [template] sends requests, with each variable filled by a placeholder, or null
     * when the result is not a URL the policy allows.
     */
    fun templateTarget(
        base: Url,
        template: String,
    ): Url? =
        try {
            resolve(base, TEMPLATE_VARIABLE.replace(template, TEMPLATE_PLACEHOLDER))
        } catch (expected: JmapException.InsecureUrl) {
            null
        }

    private fun build(
        text: String,
        block: URLBuilder.() -> Unit,
    ): Url =
        try {
            URLBuilder().apply(block).build()
        } catch (error: URLParserException) {
            throw JmapException.InsecureUrl(text, error)
        }

    private fun checked(
        url: Url,
        text: String,
    ): Url {
        val secure = url.protocol == URLProtocol.HTTPS || (policy.allowInsecureHttp && url.protocol == URLProtocol.HTTP)
        if (url.host.isBlank() || !secure) throw JmapException.InsecureUrl(text)
        return url
    }
}

/** Credentials are scoped to scheme, host, and port. */
internal fun Url.origin(): String = "${protocol.name}://${host.lowercase()}:$port"

/** This URL, or [JmapException.RedirectedElsewhere] when its origin may not receive credentials. */
internal fun Url.requireTrusted(trustedOrigins: Set<String>): Url {
    if (origin() !in trustedOrigins) throw JmapException.RedirectedElsewhere(host, origin())
    return this
}

internal class RawResponse(
    val status: Int,
    val location: String?,
    val text: String,
) {
    /** Throws for a non-success status; a problem-details body (RFC 7807) adds its type and limit. */
    fun requireSuccess() {
        if (status !in SUCCESS) throw failure()
    }

    /**
     * Whether this answers like a session resource: it asks for credentials, as every JMAP server
     * does without them, or it is a session with the core capability (RFC 8620, section 2).
     */
    fun looksLikeSession(): Boolean = status == UNAUTHORIZED || (status in SUCCESS && hasCoreCapability())

    private fun hasCoreCapability(): Boolean =
        runCatching { JmapJson.parseToJsonElement(text).jsonObject["capabilities"]?.jsonObject }
            .getOrNull()
            ?.containsKey(JmapCapability.CORE) == true

    private fun failure(): JmapException =
        when (status) {
            UNAUTHORIZED -> JmapException.Unauthorized()
            FORBIDDEN -> JmapException.Forbidden()
            NOT_FOUND -> JmapException.NotJmap("nothing found at this address")
            else -> requestFailed()
        }

    /** Keeps a problem's `type` (RFC 7807) and the `limit` a limit problem names (RFC 8620, section 3.6.1). */
    private fun requestFailed(): JmapException.RequestFailed {
        val problem = runCatching { JmapJson.parseToJsonElement(text).jsonObject }.getOrNull()
        return JmapException.RequestFailed(status, problem?.string("type"), problem?.string("limit"))
    }

    private companion object {
        val SUCCESS = 200..299
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
    }
}

/** Reads at most [limit] bytes; a larger body throws instead of exhausting memory. */
internal suspend fun HttpResponse.limitedBytes(limit: Long): ByteArray {
    if ((contentLength() ?: 0) > limit) throw JmapException.ResponseTooLarge(limit)
    val bytes = bodyAsChannel().readBuffer(limit + 1).readByteArray()
    if (bytes.size > limit) throw JmapException.ResponseTooLarge(limit)
    return bytes
}

internal suspend fun HttpResponse.limitedText(limit: Long): String = limitedBytes(limit).decodeToString()

/**
 * A blob transfer takes longer the larger the blob and the slower the link, so it has no total time
 * limit; [JmapPolicy.socketTimeoutMillis] still ends one that stalls.
 */
internal fun HttpRequestBuilder.blobTransfer() {
    timeout { requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS }
}

/**
 * RFC 6570 level 1. Encode each value before substitution, including slashes and URI delimiters.
 * Encoding leaves dots alone, and resolution and HTTP clients remove "." and ".." path segments,
 * so such a value would move the request to another path; it is refused instead.
 */
internal fun expandUriTemplate(
    template: String,
    values: Map<String, String>,
): String {
    val expanded = TEMPLATE_VARIABLE.replace(template) { match -> templateValue(values, match.groupValues[1]) }
    if ('{' in expanded || '}' in expanded) throw JmapException.NotJmap("invalid URI template")
    return expanded
}

/** [name]'s value, encoded for a URI template; refused when unknown or a dot segment. */
private fun templateValue(
    values: Map<String, String>,
    name: String,
): String {
    val value = values[name] ?: throw JmapException.NotJmap("unknown URI template variable")
    if (value.isDotSegment()) throw JmapException.NotJmap("invalid URI template value")
    return value.encodeURLParameter()
}

/** Whether [this] is a "." or ".." path segment; any "%" is encoded, so only plain dots count. */
internal fun String.isDotSegment(): Boolean = this in DOT_SEGMENTS

private val DOT_SEGMENTS = setOf(".", "..")

// Android's ICU regex engine rejects an unescaped "}", which the JVM accepts.
private val TEMPLATE_VARIABLE = Regex("\\{([^{}]+)\\}")

/** Fills every template variable when only the template's origin matters. */
private const val TEMPLATE_PLACEHOLDER = "x"
