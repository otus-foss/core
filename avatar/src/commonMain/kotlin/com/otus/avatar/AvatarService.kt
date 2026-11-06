package com.otus.avatar

import com.otus.jmap.login.LoginLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Contact photos are account-specific; website icons are shared by every sender at that domain. */
public class AvatarService(
    private val contacts: ContactAvatarSource = ContactAvatarSource { _, _ -> null },
    images: AvatarImageLoader = AvatarImageLoader { null },
    cacheConfig: AvatarCacheConfig = AvatarCacheConfig(),
    domains: AvatarDomainResolver = AvatarDomainResolver { it },
) : LoginLifecycle {
    private val websites = WebsiteIcons(images, domains)
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
            withWebsiteIcon(normalized, fallback, contact, options, started)
        } else {
            ResolvedAvatar(fallback, origin = AvatarOrigin.Contact, style = contact.style)
        }
    }

    /** The contact's photo, or else the website icon when the options or its style ask for one. */
    private suspend fun withWebsiteIcon(
        request: AvatarRequest,
        fallback: GeneratedAvatar,
        contact: StoredAvatar,
        options: AvatarOptions,
        started: Long,
    ): ResolvedAvatar {
        val image = contact.image ?: websiteIcon(request, iconOptions(contact.style, options), started)
        val origin =
            when {
                contact.image != null -> AvatarOrigin.Contact
                image != null -> AvatarOrigin.Domain
                else -> null
            }
        return ResolvedAvatar(fallback, image, origin, contact.style)
    }

    /** The website's brand logo when [request] may show one, else its icon. */
    private suspend fun websiteIcon(
        request: AvatarRequest,
        options: AvatarOptions,
        started: Long,
    ): AvatarImage? {
        val site = websites.site(request.email, options) ?: return null
        val brand =
            websites.brandKey(request, site, options)?.let { key ->
                load(key, started) { websites.brandLogo(site, options) }
            }
        val missLifetime = if (options.iconProxy == null) WEBSITE_MISS_LIFETIME else MISS_LIFETIME
        return brand ?: load(domainKey(site, options), started, missLifetime) { websites.icon(site, options) }
    }

    private suspend fun load(
        key: AvatarCacheKey,
        started: Long,
        missLifetime: Long = MISS_LIFETIME,
        image: suspend () -> AvatarImage?,
    ): AvatarImage? =
        cache.load(key, started, missLifetime) { fetches.withPermit { image()?.let { StoredAvatar(it, 0) } } }.image

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
        val site =
            if (contact?.image == null &&
                style.wantsIcon()
            ) {
                websites.site(normalized.email, iconOptions)
            } else {
                null
            }
        val brand = site?.let { websites.brandKey(normalized, it, iconOptions) }?.let { cache.peek(it) }
        val domain = brand?.takeIf { it.image != null } ?: site?.let { cache.peek(domainKey(it, iconOptions)) }
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

    /** New credentials may see other contacts, so the login's avatars are read again. */
    override suspend fun connected(loginId: String): Unit = removeAccount(loginId)

    override suspend fun removed(loginId: String): Unit = removeAccount(loginId)

    private suspend fun contactOrNull(
        request: AvatarRequest,
        options: AvatarOptions,
    ): StoredAvatar? =
        when (val found = nullOnFailure { contacts.avatar(request, options.externalContactImages) }) {
            is ContactAvatar.Photo -> avatarOrNull { found.image }?.let { StoredAvatar(it, 0) }
            is ContactAvatar.Styled -> found.toStored()
            null -> null
        }

    private companion object {
        const val MAX_CONCURRENT_FETCHES = 8

        /**
         * Each attempt tells the sender's website the device's IP address and when the sender was
         * shown, so a website without an icon is asked again at most once a day.
         */
        const val WEBSITE_MISS_LIFETIME = 24 * 60 * 60 * 1000L
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
) = AvatarCacheKey(null, "domain:${options.iconProxy?.let { it.iconUrl ?: "none" } ?: "direct"}:$domain")

/** Optional avatar failures never hide mail/events, and cancellation always propagates. */
internal suspend fun avatarOrNull(load: suspend () -> AvatarImage?): AvatarImage? =
    nullOnFailure(load)?.takeIf { it.bytes.isNotEmpty() && it.bytes.size <= MAX_AVATAR_BYTES && !it.isPixel() }

/** A 1×1 PNG, which icon services such as Bulwark answer for a domain without an icon. */
private fun AvatarImage.isPixel(): Boolean {
    if (bytes.size < PNG_IHDR_END || !PNG_SIGNATURE.indices.all { bytes[it] == PNG_SIGNATURE[it] }) return false
    val width = bigEndianInt(PNG_WIDTH_OFFSET)
    val height = bigEndianInt(PNG_WIDTH_OFFSET + Int.SIZE_BYTES)
    return width <= 1 || height <= 1
}

private fun AvatarImage.bigEndianInt(offset: Int): Int =
    (0 until Int.SIZE_BYTES).fold(0) { value, index ->
        (value shl Byte.SIZE_BITS) or (bytes[offset + index].toInt() and BYTE_MASK)
    }

private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte())
private const val PNG_WIDTH_OFFSET = 16
private const val PNG_IHDR_END = 24
private const val BYTE_MASK = 0xFF

internal suspend fun <T> nullOnFailure(load: suspend () -> T?): T? =
    runCatching { load() }.getOrElse {
        if (it is CancellationException) throw it
        null
    }
