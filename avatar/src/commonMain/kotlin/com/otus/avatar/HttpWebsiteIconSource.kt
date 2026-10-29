package com.otus.avatar

import io.ktor.http.Url

/** Prefer large raster icons advertised by the website over small legacy favicons. */
public class HttpWebsiteIconSource(
    private val images: HttpAvatarImageLoader,
) : DomainIconSource {
    override suspend fun icon(domain: String): AvatarImage? =
        avatarOrNull {
            val page = images.websitePage("https://$domain/") ?: return@avatarOrNull null
            websiteIconUrls(page).firstNotNullOfOrNull { avatarOrNull { images.load(it) } }
        }
}

internal fun websiteIconUrls(page: AvatarWebsitePage): List<String> {
    val head = Regex("</head\\s*>", RegexOption.IGNORE_CASE).split(page.html, limit = 2).first()
    return Regex("<link\\b[^>]*>", RegexOption.IGNORE_CASE)
        .findAll(head.replace(Regex("<!--[\\s\\S]*?-->"), ""))
        .mapNotNull { websiteIcon(it.value, page.url) }
        .sortedByDescending { it.size }
        .map { it.url }
        .distinct()
        .take(MAX_WEBSITE_ICONS)
        .toList()
}

/** The raster icon a `<link>` [tag] on the page at [pageUrl] advertises, or null when it names none. */
private fun websiteIcon(
    tag: String,
    pageUrl: String,
): WebsiteIcon? {
    val attributes = linkAttributes(tag)
    val rel = attributes["rel"]?.lowercase()?.split(Regex("\\s+")).orEmpty()
    val touch = "apple-touch-icon" in rel || "apple-touch-icon-precomposed" in rel
    val icon = touch || "icon" in rel
    val svg = attributes["type"]?.contains("svg", ignoreCase = true) == true
    val href = attributes["href"]?.replace("&amp;", "&")
    if (!icon || svg || href.isNullOrBlank()) return null
    return rasterUrl(pageUrl, href)?.let { WebsiteIcon(it, iconSize(attributes["sizes"].orEmpty(), touch)) }
}

/** [href] resolved against [pageUrl], or null when it does not resolve or names an SVG file. */
private fun rasterUrl(
    pageUrl: String,
    href: String,
): String? =
    runCatching { Url(pageUrl).resolving(href).buildString() }
        .getOrNull()
        ?.takeUnless { Url(it).encodedPath.endsWith(".svg", ignoreCase = true) }

/** The attributes of an HTML tag by lowercase name, with quoted or bare values. */
private fun linkAttributes(tag: String): Map<String, String> =
    LINK_ATTRIBUTE.findAll(tag).associate { match ->
        match.groupValues[1].lowercase() to (match.groupValues.drop(2).firstOrNull { it.isNotEmpty() } ?: "")
    }

private fun iconSize(
    sizes: String,
    touch: Boolean,
): Int =
    Regex("(\\d+)x(\\d+)", RegexOption.IGNORE_CASE)
        .findAll(sizes)
        .map { minOf(it.groupValues[1].toIntOrNull() ?: 0, it.groupValues[2].toIntOrNull() ?: 0) }
        .maxOrNull() ?: if (touch) DEFAULT_TOUCH_ICON_SIZE else 0

private class WebsiteIcon(
    val url: String,
    val size: Int,
)

private val LINK_ATTRIBUTE = Regex("([\\w-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))")

private const val MAX_WEBSITE_ICONS = 8
private const val DEFAULT_TOUCH_ICON_SIZE = 180
