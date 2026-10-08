package com.otus.avatar

/** Account keys are app-private login IDs, never display names or credentials. */
public data class AvatarRequest(
    val accountKey: String,
    val email: String,
    val name: String = "",
)

public data class AvatarOptions(
    val externalContactImages: Boolean = false,
    val domainIcons: Boolean = false,
    val domainIconProxy: Boolean = false,
)

public data class GeneratedAvatar(
    val initials: String,
    /** Maps to named theme tokens in each app; no UI types belong in this service. */
    val colorIndex: Int,
)

public enum class AvatarOrigin { Contact, Domain }

/** Encoded raster data. Callers must treat [bytes] as read-only. */
public class AvatarImage(
    public val bytes: ByteArray,
)

public data class ResolvedAvatar(
    val fallback: GeneratedAvatar,
    val image: AvatarImage? = null,
    val origin: AvatarOrigin? = null,
)

public fun interface ContactPhotoSource {
    /** Return null for unavailable photos. External URIs require [allowExternalImages]. */
    public suspend fun photo(
        request: AvatarRequest,
        allowExternalImages: Boolean,
    ): AvatarImage?
}

public fun interface AvatarImageLoader {
    /** Fetch public images without any account credentials. */
    public suspend fun load(url: String): AvatarImage?

    /** Optional discovery of larger raster icons declared in a public website's HTML. */
    public suspend fun websiteIcon(domain: String): AvatarImage? = null
}

public fun interface AvatarDomainResolver {
    /** Return a website domain for this host, or null for a bare public suffix. */
    public suspend fun resolve(host: String): String?
}

public fun interface DomainIconSource {
    /** Optional public icon cache. Receives a hostname, never an email address or credentials. */
    public suspend fun icon(domain: String): AvatarImage?
}

/**
 * Case-insensitive, because real mail servers treat `John@` and `john@` as one mailbox. Dots and plus
 * tags stay: removing them would merge different people's addresses.
 */
public fun avatarEmailKey(email: String): String = email.trim().lowercase()

/** Stable across Kotlin platforms, independent of contact availability and display-name changes. */
public fun generatedAvatar(
    name: String,
    email: String,
): GeneratedAvatar {
    val key = avatarEmailKey(email).ifBlank { name.trim() }
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val initials =
        if (words.isEmpty() || '@' in name) {
            key.take(1).uppercase().ifBlank { "?" }
        } else {
            (words.first().take(1) + if (words.size > 1) words.last().take(1) else "").uppercase()
        }
    return GeneratedAvatar(initials, (key.hashCode() and Int.MAX_VALUE) % AVATAR_COLOR_COUNT)
}

private const val AVATAR_COLOR_COUNT = 3
