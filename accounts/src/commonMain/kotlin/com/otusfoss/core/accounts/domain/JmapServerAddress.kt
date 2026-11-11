package com.otusfoss.core.accounts.domain

/** What the user typed as their server, resolved to a JMAP session URL when possible. */
public sealed interface JmapServerAddress {
    public data object Empty : JmapServerAddress

    /** JMAP requires TLS; plain HTTP is refused rather than silently upgraded. */
    public data object Insecure : JmapServerAddress

    public data object Invalid : JmapServerAddress

    public data class Valid(
        val sessionUrl: String,
    ) : JmapServerAddress
}

/** The longest DNS name (RFC 1035, section 2.3.4) plus a colon and a five-digit port. */
private const val MAX_AUTHORITY_LENGTH = 253 + 6

private const val LABEL = "[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?"
private const val PORT = "(:[0-9]{1,5})"
private const val OCTET = "(25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])"

// A half-typed word such as `asdf` is not a host, but self-hosted servers often sit at an IP
// address, at `localhost`, or at a LAN name with a port, so those count.
private val hostPatterns =
    listOf(
        Regex("^($LABEL\\.)+[A-Za-z]([A-Za-z0-9-]*[A-Za-z0-9])?$PORT?$"),
        Regex("^$OCTET(\\.$OCTET){3}$PORT?$"),
        Regex("^\\[[0-9A-Fa-f.]*:[0-9A-Fa-f:.]*]$PORT?$"),
        Regex("^localhost$PORT?$", RegexOption.IGNORE_CASE),
        Regex("^$LABEL$PORT$"),
    )

/**
 * A bare host such as `mail.example.com` uses the standard discovery path, `/.well-known/jmap`
 * (RFC 8620, section 2.2). A full `https://` URL with a path is used as the session URL itself.
 */
public fun parseJmapServer(input: String): JmapServerAddress {
    val text = input.trim()
    val scheme = text.substringBefore("://", missingDelimiterValue = "").lowercase()
    val rest = if (scheme.isEmpty()) text else text.substringAfter("://")
    val authority = rest.substringBefore('/')
    val path = rest.removePrefix(authority).trimEnd('/')
    return when {
        text.isEmpty() -> JmapServerAddress.Empty
        scheme == "http" -> JmapServerAddress.Insecure
        scheme !in setOf("", "https") || !authority.isHost() -> JmapServerAddress.Invalid
        else -> JmapServerAddress.Valid("https://${authority.lowercase()}${path.ifEmpty { "/.well-known/jmap" }}")
    }
}

// The length check comes first: the DNS pattern recurses once per label, so a pasted run of
// thousands of labels would overflow the stack before it could fail.
private fun String.isHost(): Boolean = length <= MAX_AUTHORITY_LENGTH && hostPatterns.any { it.matches(this) }

/** Details for connecting one account. The password is passed on, never stored by this model. */
public data class JmapAccountDraft(
    val sessionUrl: String,
    val username: String,
    val password: String,
    /** Other origins the user approved to receive these credentials. */
    val trustedOrigins: Set<String> = emptySet(),
) {
    override fun toString(): String =
        "JmapAccountDraft(sessionUrl=$sessionUrl, username=$username, password=***, trustedOrigins=$trustedOrigins)"
}

/** HTTP Basic usernames cannot contain a colon (RFC 7617, section 2). */
public fun isValidUsername(username: String): Boolean = username.isNotBlank() && ':' !in username
