package com.otusfoss.core.avatar

import io.ktor.http.URLProtocol
import io.ktor.http.Url

internal fun isAvatarEmail(email: String): Boolean =
    email.count { it == '@' } == 1 && email.substringBefore('@').isNotBlank() &&
        email.substringAfter('@').isNotBlank() && email.none { it.isWhitespace() || it.isISOControl() }

/** Conservative ASCII hostname handling. IDNs and internal hosts retain their local fallback. */
internal fun avatarIconDomain(email: String): String? {
    if (!isAvatarEmail(email)) return null
    return email.substringAfter('@').takeIf(::isPublicAvatarHost)?.lowercase()
}

/** Whether the public loader may request [url], so callers can check an address before saving it. */
internal fun isPublicAvatarUrl(url: String): Boolean = runCatching { Url(url) }.getOrNull()?.isPublicImageUrl() == true

/** Only the default HTTPS port, so a public name whose DNS answer is private cannot probe other ports. */
internal fun Url.isPublicImageUrl(): Boolean =
    protocol == URLProtocol.HTTPS && port == URLProtocol.HTTPS.defaultPort && user.isNullOrEmpty() &&
        password.isNullOrEmpty() && isPublicAvatarHost(host)

/**
 * The only host check before every public avatar request, including redirects and advertised icon links,
 * so it fails closed: absolute, percent-encoded, and non-ASCII names are refused rather than normalized.
 * It sees names only; a public name whose DNS answer is a private address still passes.
 */
internal fun isPublicAvatarHost(host: String): Boolean {
    val labels = host.split('.')
    val topLevel = labels.last().lowercase()
    return labels.size > 1 && labels.all(::isAsciiLabel) && isTopLevelLabel(topLevel) && topLevel !in PRIVATE_SUFFIXES
}

/** Checked before lowercasing, which turns some non-ASCII letters, such as the Kelvin sign, into ASCII. */
private fun isAsciiLabel(label: String): Boolean =
    label.length in 1..MAX_LABEL_LENGTH && label.first() != '-' && label.last() != '-' &&
        label.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '-' }

/**
 * Real top-level domains are alphabetic or IDN A-labels. Resolvers read a numeric last label as IPv4,
 * so this refuses forms such as `127.1` and `0x7f.0x1`; IPv6 literals already fail the label check.
 */
private fun isTopLevelLabel(label: String): Boolean = label.all { it in 'a'..'z' } || label.startsWith("xn--")

private const val MAX_LABEL_LENGTH = 63

/**
 * Top-level labels of special-use names (RFC 6761, 6762, 7686, 8375, 9476), the infrastructure domain
 * `arpa`, and widely used private top-level names. None hosts a public website; a name under them that
 * resolves at all usually points into the user's network.
 */
private val PRIVATE_SUFFIXES =
    setOf(
        "alt",
        "arpa",
        "corp",
        "example",
        "home",
        "internal",
        "intranet",
        "invalid",
        "lan",
        "local",
        "localdomain",
        "localhost",
        "onion",
        "private",
        "test",
    )
