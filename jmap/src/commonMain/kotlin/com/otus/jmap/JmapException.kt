package com.otus.jmap

/**
 * Failures reported by the protocol client for server, network, or stored data problems.
 * Custom method decoders must follow [JmapMethod.decode]'s error contract.
 */
public sealed class JmapException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** HTTP 401: the credentials were rejected. */
    public class Unauthorized : JmapException("The server rejected the credentials.")

    /** HTTP 403: the credentials are valid, but this access is not allowed. */
    public class Forbidden : JmapException("The server does not allow this access.")

    /** The address answered, but not with usable JMAP data. */
    public class NotJmap(
        detail: String,
        cause: Throwable? = null,
    ) : JmapException("Not a JMAP response: $detail", cause)

    /** DNS, connection, TLS, or timeout failure. */
    public class Unreachable(
        cause: Throwable,
    ) : JmapException("The server could not be reached.", cause)

    /** A URL that is not https, or not a URL. Plain HTTP is refused unless [JmapPolicy.allowInsecureHttp]. */
    public class InsecureUrl(
        detail: String,
        cause: Throwable? = null,
    ) : JmapException("Refused URL: $detail", cause)

    /**
     * Credentials would go to [host], which the user did not enter. Ask the user, then retry with
     * [origin] in the trusted origins.
     */
    public class RedirectedElsewhere(
        public val host: String,
        public val origin: String,
    ) : JmapException("The server sends sign-in to another host: $host")

    public class TooManyRedirects : JmapException("Too many redirects.")

    public class ResponseTooLarge(
        public val limitBytes: Long,
    ) : JmapException("The response exceeded $limitBytes bytes.")

    /** The blob is larger than the server's advertised `maxSizeUpload`; nothing was sent. */
    public class UploadTooLarge(
        public val limitBytes: Long,
    ) : JmapException("The upload exceeds the server's $limitBytes-byte limit.")

    /** The server or account lacks a capability the request needs. */
    public class Unsupported(
        public val capability: String,
    ) : JmapException("Not supported by this server or account: $capability")

    /**
     * A request-level problem (RFC 8620, section 3.6.1), or another HTTP failure status. [limit] names
     * the limit a `urn:ietf:params:jmap:error:limit` problem reports, such as `maxCallsInRequest`.
     */
    public class RequestFailed(
        public val status: Int,
        public val type: String?,
        public val limit: String? = null,
    ) : JmapException("The request failed with HTTP $status${type?.let { " ($it)" }.orEmpty()}.")

    /**
     * HTTP 404 from a blob download: the server has no blob [blobId] in that account, or no longer
     * has it. The session is still valid.
     */
    public class BlobNotFound(
        public val blobId: String,
    ) : JmapException("The server has no blob $blobId.")

    /** A method-level error (RFC 8620, section 3.6.2), such as `stateMismatch` or `forbidden`. */
    public class MethodError(
        public val type: String,
        public val description: String?,
    ) : JmapException("The server rejected a method call: $type")

    /** The saved secret for a login is missing or cannot be decrypted; the user must sign in again. */
    public class CredentialsUnavailable(
        public val loginId: String,
    ) : JmapException("The saved credentials for this login cannot be read.")
}

/** What a failure means for the user, whatever the call: the coarse reading of a [JmapException]. */
public enum class JmapErrorCategory {
    /** The server rejected the credentials; the user signs in again. */
    Auth,

    /** The credentials work, but the server does not allow this access. */
    Permission,

    /** The saved secret cannot be read; the user signs in again. */
    Credentials,

    /** The server could not be reached; a later try may work. */
    Offline,

    /** The server or account lacks the capability the call needs. */
    Unsupported,

    /** Any other failure of the server or its answer. */
    Server,
}

public val JmapException.category: JmapErrorCategory
    get() =
        when (this) {
            is JmapException.Unauthorized -> JmapErrorCategory.Auth
            is JmapException.Forbidden -> JmapErrorCategory.Permission
            is JmapException.CredentialsUnavailable -> JmapErrorCategory.Credentials
            is JmapException.Unreachable -> JmapErrorCategory.Offline
            is JmapException.Unsupported -> JmapErrorCategory.Unsupported
            else -> JmapErrorCategory.Server
        }

/**
 * Whether the call waits for the next sync instead of failing for good: `serverUnavailable` (RFC
 * 8620, section 3.6.2) is temporary, while `serverFail` may fail again and `serverPartialFail` asks
 * for a resync. All three are the server's problem, not the call's, so a later try may succeed.
 */
public val JmapException.MethodError.isTemporary: Boolean
    get() = type in TEMPORARY_METHOD_ERRORS

private val TEMPORARY_METHOD_ERRORS = setOf("serverUnavailable", "serverFail", "serverPartialFail")
