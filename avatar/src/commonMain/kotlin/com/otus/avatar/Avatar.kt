package com.otus.avatar

/**
 * Account keys are app-private login IDs, never display names or credentials. [brandDomain] is the
 * From domain the message passed DMARC for, as the user's server recorded it. Only then may a
 * brand logo show, since the logo says who owns the domain, not that this message came from it.
 */
public data class AvatarRequest(
    val accountKey: String,
    val email: String,
    val name: String = "",
    val brandDomain: String? = null,
)

/** [iconProxy] is asked instead of the sender's website, so the website never sees the device. */
public data class AvatarOptions(
    val externalContactImages: Boolean = false,
    val domainIcons: Boolean = false,
    val iconProxy: IconProxy? = null,
)

/**
 * A service that fetches website icons for the device, such as DuckDuckGo's icon cache or a
 * Bulwark webmail server. Each URL names the website domain as `{domain}`. [brandLogoUrl] answers
 * the domain's BIMI logo as a raster image. Either may answer a 1×1 image for a domain without one.
 * A null [iconUrl], for a service the user chose but cannot reach, loads no icons, and the website
 * is still never asked.
 */
public data class IconProxy(
    val iconUrl: String?,
    val brandLogoUrl: String? = null,
)

/** [template] with `{domain}` replaced by [domain], a checked public hostname. */
internal fun proxyUrl(
    template: String,
    domain: String,
): String = template.replace("{domain}", domain)

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

/**
 * [style] is set instead of [image] when a contact card holds an [AvatarStyle], or beside the
 * website icon when the style is [AvatarMark.Website].
 */
public data class ResolvedAvatar(
    val fallback: GeneratedAvatar,
    val image: AvatarImage? = null,
    val origin: AvatarOrigin? = null,
    val style: AvatarStyle? = null,
)

/** What a contact card shows for an address: its photo, or a style the user built in Otus. */
public sealed interface ContactAvatar {
    public class Photo(
        public val image: AvatarImage,
        public val mediaType: String,
    ) : ContactAvatar

    public data class Styled(
        val style: AvatarStyle,
    ) : ContactAvatar
}

public fun interface ContactAvatarSource {
    /** Return null when no card has an avatar. External URIs require [allowExternalImages]. */
    public suspend fun avatar(
        request: AvatarRequest,
        allowExternalImages: Boolean,
    ): ContactAvatar?
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
