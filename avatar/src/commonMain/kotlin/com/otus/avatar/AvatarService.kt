package com.otus.avatar

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

/** Contact photos are account-specific; website icons are shared by every sender at that domain. */
public class AvatarService(
    private val contacts: ContactAvatarSource = ContactAvatarSource { _, _ -> null },
    private val images: AvatarImageLoader = AvatarImageLoader { null },
    cacheConfig: AvatarCacheConfig = AvatarCacheConfig(),
    private val domains: AvatarDomainResolver = AvatarDomainResolver { it },
    private val iconFallback: DomainIconSource = DomainIconSource { null },
) {
    private val cache =
        AvatarLookupCache(
            cacheConfig.store,
            cacheConfig.maxEntries,
            cacheConfig.cacheMillis,
            cacheConfig.nowMillis,
        )
    private val fetches = Semaphore(MAX_CONCURRENT_FETCHES)

    public suspend fun resolve(
        request: AvatarRequest,
        options: AvatarOptions = AvatarOptions(),
    ): ResolvedAvatar {
        val fallback = generatedAvatar(request.name, request.email)
        val normalized = request.normalized() ?: return ResolvedAvatar(fallback)
        val started = cache.begin()
        val contact =
            cache.load(contactKey(normalized, options), started) {
                fetches.withPermit { contactOrNull(normalized, options) }
            }
        return if (contact.style.wantsIcon()) {
            withWebsiteIcon(normalized.email, fallback, contact, options, started)
        } else {
            ResolvedAvatar(fallback, origin = AvatarOrigin.Contact, style = contact.style)
        }
    }

    /** The contact's photo, or else the website icon when the options or its style ask for one. */
    private suspend fun withWebsiteIcon(
        email: String,
        fallback: GeneratedAvatar,
        contact: StoredAvatar,
        options: AvatarOptions,
        started: Long,
    ): ResolvedAvatar {
        val iconOptions = iconOptions(contact.style, options)
        val domain =
            if (contact.image == null) {
                iconDomain(email, iconOptions)?.let { site ->
                    cache
                        .load(domainKey(site, iconOptions), started) {
                            fetches.withPermit { domainImage(site, iconOptions)?.let { StoredAvatar(it, 0) } }
                        }.image
                }
            } else {
                null
            }
        val origin =
            when {
                contact.image != null -> AvatarOrigin.Contact
                domain != null -> AvatarOrigin.Domain
                else -> null
            }
        return ResolvedAvatar(fallback, contact.image ?: domain, origin, contact.style)
    }

    /** Local images only, so refreshed rows keep their avatar before network work. */
    public suspend fun cached(
        request: AvatarRequest,
        options: AvatarOptions = AvatarOptions(),
    ): ResolvedAvatar? {
        val normalized = request.normalized() ?: return null
        val contact = cache.peek(contactKey(normalized, options))
        val fallback = generatedAvatar(request.name, normalized.email)
        val style = contact?.style
        val iconOptions = iconOptions(style, options)
        val site = if (contact?.image == null && style.wantsIcon()) iconDomain(normalized.email, iconOptions) else null
        val domain = site?.let { cache.peek(domainKey(it, iconOptions)) }
        // Keep the style when the icon is not cached yet, so a Website avatar draws its color.
        return when {
            !style.wantsIcon() -> ResolvedAvatar(fallback, origin = AvatarOrigin.Contact, style = style)
            contact?.image != null -> ResolvedAvatar(fallback, contact.image, AvatarOrigin.Contact)
            domain?.image != null -> ResolvedAvatar(fallback, domain.image, AvatarOrigin.Domain, style)
            contact != null || domain != null -> ResolvedAvatar(fallback, style = style)
            else -> null
        }
    }

    /** Explicit contact refresh invalidates stored results, including queued and running lookups. */
    public suspend fun clear(): Unit = cache.clear()

    /**
     * Makes [avatar], just saved on a card for [request], what lookups find, so it shows at once.
     * Reading it back would search the server, whose index may not have the card yet. A null
     * [avatar], such as for a removed or linked photo, makes the next lookup ask the server again
     * instead of keeping the old result for up to a month.
     */
    public suspend fun saved(
        request: AvatarRequest,
        avatar: ContactAvatar?,
    ) {
        val normalized = request.normalized() ?: return
        val entry = avatar?.toStored()
        listOf(false, true).forEach { external ->
            val key = contactKey(normalized, AvatarOptions(externalContactImages = external))
            if (entry == null) cache.expire(key) else cache.put(key, entry)
        }
    }

    /** Logout/reconnect purges private photos while retaining public website icons. */
    public suspend fun removeAccount(account: String): Unit = cache.clear(account)

    private suspend fun contactOrNull(
        request: AvatarRequest,
        options: AvatarOptions,
    ): StoredAvatar? =
        when (val found = nullOnFailure { contacts.avatar(request, options.externalContactImages) }) {
            is ContactAvatar.Photo -> avatarOrNull { found.image }?.let { StoredAvatar(it, 0) }
            is ContactAvatar.Styled -> found.toStored()
            null -> null
        }

    /**
     * Only the registrable domain, never the exact host: a sender could otherwise give each recipient
     * a unique subdomain and learn from the icon request when and where the message was shown.
     */
    private suspend fun iconDomain(
        email: String,
        options: AvatarOptions,
    ): String? {
        val host = avatarIconDomain(email)?.takeIf { options.domainIcons } ?: return null
        return resolvedDomain(host)?.takeIf { isPublicAvatarHost(it) && (host == it || host.endsWith(".$it")) }
    }

    private suspend fun resolvedDomain(host: String): String? = nullOnFailure { domains.resolve(host) }

    private suspend fun domainImage(
        domain: String,
        options: AvatarOptions,
    ): AvatarImage? =
        avatarOrNull {
            val direct =
                withTimeoutOrNull(DOMAIN_ICON_TIMEOUT_MILLIS) {
                    avatarOrNull { images.websiteIcon(domain) }
                        ?: DOMAIN_ICON_PATHS.firstNotNullOfOrNull { path ->
                            avatarOrNull { images.load("https://$domain/$path") }
                        }
                }
            direct ?: if (options.domainIconProxy) iconFallback.icon(domain) else null
        }

    private companion object {
        const val MAX_CONCURRENT_FETCHES = 8
        const val DOMAIN_ICON_TIMEOUT_MILLIS = 8_000L
        val DOMAIN_ICON_PATHS = listOf("apple-touch-icon.png", "favicon.png", "favicon.ico")
    }
}

/** A [AvatarMark.Website] style asks for the website icon even with website icons off. */
private fun iconOptions(
    style: AvatarStyle?,
    options: AvatarOptions,
) = if (style?.mark == AvatarMark.Website) options.copy(domainIcons = true) else options

/** The request with its address as a cache key, or null when it is no address to look up. */
private fun AvatarRequest.normalized(): AvatarRequest? =
    copy(email = avatarEmailKey(email)).takeIf { isAvatarEmail(it.email) }

private fun ContactAvatar.toStored(): StoredAvatar =
    when (this) {
        is ContactAvatar.Photo -> StoredAvatar(image, 0)
        is ContactAvatar.Styled -> StoredAvatar(null, 0, style)
    }

private fun AvatarStyle?.wantsIcon() = this == null || mark == AvatarMark.Website

private fun contactKey(
    request: AvatarRequest,
    options: AvatarOptions,
) = AvatarCacheKey(request.accountKey, "contact:${options.externalContactImages}:${request.email}")

private fun domainKey(
    domain: String,
    options: AvatarOptions,
) = AvatarCacheKey(null, "domain:${options.domainIconProxy}:$domain")

/** Optional avatar failures never hide mail/events, and cancellation always propagates. */
internal suspend fun avatarOrNull(load: suspend () -> AvatarImage?): AvatarImage? =
    nullOnFailure(load)?.takeIf { it.bytes.isNotEmpty() && it.bytes.size <= MAX_AVATAR_BYTES }

internal suspend fun <T> nullOnFailure(load: suspend () -> T?): T? =
    runCatching { load() }.getOrElse {
        if (it is CancellationException) throw it
        null
    }
