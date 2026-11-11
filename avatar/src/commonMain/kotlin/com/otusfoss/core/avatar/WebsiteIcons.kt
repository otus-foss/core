package com.otusfoss.core.avatar

import kotlinx.coroutines.withTimeoutOrNull

/**
 * Where a sender's website icon and brand logo come from: which website domain to ask, and whether
 * to ask the website itself or the options' icon proxy.
 */
internal class WebsiteIcons(
    private val images: AvatarImageLoader,
    private val domains: AvatarDomainResolver,
) {
    /**
     * Only the registrable domain, never the exact host: a sender could otherwise give each recipient
     * a unique subdomain and learn from the icon request when and where the message was shown.
     */
    suspend fun site(
        email: String,
        options: AvatarOptions,
    ): String? = avatarIconDomain(email)?.takeIf { options.domainIcons }?.let { registrable(it) }

    /**
     * The cache key of [site]'s brand logo, or null unless the proxy answers logos and the message
     * passed DMARC for a domain of the same [site]. The logo is looked up for [site] alone, so a
     * sender's unique subdomain cannot tell when its mail was listed.
     */
    suspend fun brandKey(
        request: AvatarRequest,
        site: String,
        options: AvatarOptions,
    ): AvatarCacheKey? {
        val template = options.iconProxy?.brandLogoUrl
        val checked =
            request.brandDomain
                ?.trim()
                ?.lowercase()
                ?.takeIf(::isPublicAvatarHost)
        val sameSite = template != null && checked != null && registrable(checked) == site
        return if (sameSite) AvatarCacheKey(null, "brand:$template:$site") else null
    }

    suspend fun brandLogo(
        site: String,
        options: AvatarOptions,
    ): AvatarImage? = options.iconProxy?.brandLogoUrl?.let { avatarOrNull { images.load(proxyUrl(it, site)) } }

    /** Only the proxy when there is one, so the website never sees the device. */
    suspend fun icon(
        domain: String,
        options: AvatarOptions,
    ): AvatarImage? {
        val proxy = options.iconProxy
        val template = proxy?.iconUrl
        return when {
            proxy == null -> website(domain)
            template == null -> null
            else -> avatarOrNull { images.load(proxyUrl(template, domain)) }
        }
    }

    private suspend fun registrable(host: String): String? {
        val domain = nullOnFailure { domains.resolve(host) }
        return domain?.takeIf { isPublicAvatarHost(it) && (host == it || host.endsWith(".$it")) }
    }

    private suspend fun website(domain: String): AvatarImage? =
        avatarOrNull {
            withTimeoutOrNull(DOMAIN_ICON_TIMEOUT_MILLIS) {
                avatarOrNull { images.websiteIcon(domain) }
                    ?: DOMAIN_ICON_PATHS.firstNotNullOfOrNull { path ->
                        avatarOrNull { images.load("https://$domain/$path") }
                    }
            }
        }

    private companion object {
        const val DOMAIN_ICON_TIMEOUT_MILLIS = 8_000L
        val DOMAIN_ICON_PATHS = listOf("apple-touch-icon.png", "favicon.png", "favicon.ico")
    }
}
