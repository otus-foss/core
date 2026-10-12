package com.otus.avatar

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

/** Contact photos are account-specific; website icons are shared by every sender at that domain. */
public class AvatarService(
    private val contacts: ContactPhotoSource = ContactPhotoSource { _, _ -> null },
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
        val normalized = request.copy(email = avatarEmailKey(request.email))
        val fallback = generatedAvatar(request.name, normalized.email)
        if (!isAvatarEmail(normalized.email)) return ResolvedAvatar(fallback)
        val started = cache.begin()
        val contact =
            cache
                .load(contactKey(normalized, options), started) {
                    fetches.withPermit { avatarOrNull { contacts.photo(normalized, options.externalContactImages) } }
                }.image
        val domain =
            if (contact == null) {
                iconDomain(normalized.email, options)?.let { website ->
                    cache
                        .load(domainKey(website, options), started) {
                            fetches.withPermit { domainImage(website, options) }
                        }.image
                }
            } else {
                null
            }
        val origin =
            when {
                contact != null -> AvatarOrigin.Contact
                domain != null -> AvatarOrigin.Domain
                else -> null
            }
        return ResolvedAvatar(fallback, contact ?: domain, origin)
    }

    /** Local images only, so refreshed rows keep their avatar before network work. */
    public suspend fun cached(
        request: AvatarRequest,
        options: AvatarOptions = AvatarOptions(),
    ): ResolvedAvatar? {
        val normalized = request.copy(email = avatarEmailKey(request.email))
        if (!isAvatarEmail(normalized.email)) return null
        val contact = cache.peek(contactKey(normalized, options))
        val fallback = generatedAvatar(request.name, normalized.email)
        val website = if (contact?.image == null) iconDomain(normalized.email, options) else null
        val domain = website?.let { cache.peek(domainKey(it, options)) }
        return when {
            contact?.image != null -> ResolvedAvatar(fallback, contact.image, AvatarOrigin.Contact)
            domain?.image != null -> ResolvedAvatar(fallback, domain.image, AvatarOrigin.Domain)
            contact != null || domain != null -> ResolvedAvatar(fallback)
            else -> null
        }
    }

    /** Explicit contact refresh invalidates stored results, including queued and running lookups. */
    public suspend fun clear(): Unit = cache.clear()

    /** Logout/reconnect purges private photos while retaining public website icons. */
    public suspend fun removeAccount(account: String): Unit = cache.clear(account)

    private fun contactKey(
        request: AvatarRequest,
        options: AvatarOptions,
    ) = AvatarCacheKey(request.accountKey, "contact:${options.externalContactImages}:${request.email}")

    private fun domainKey(
        domain: String,
        options: AvatarOptions,
    ) = AvatarCacheKey(null, "domain:${options.domainIconProxy}:$domain")

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

    private suspend fun resolvedDomain(host: String): String? =
        runCatching { domains.resolve(host) }.getOrElse {
            if (it is CancellationException) throw it
            null
        }

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

/** Optional avatar failures never hide mail/events, and cancellation always propagates. */
internal suspend fun avatarOrNull(load: suspend () -> AvatarImage?): AvatarImage? =
    runCatching { load()?.takeIf { it.bytes.isNotEmpty() && it.bytes.size <= MAX_AVATAR_BYTES } }.getOrElse {
        if (it is CancellationException) throw it
        null
    }
