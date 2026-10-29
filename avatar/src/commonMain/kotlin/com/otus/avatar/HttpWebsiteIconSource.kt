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
    val rel = attributes["rel"]?.lowercase()?.split(Regex("\\s+")) ?: return null
    val touch = "apple-touch-icon" in rel || "apple-touch-icon-precomposed" in rel
    if (!touch && "icon" !in rel) return null
    if (attributes["type"]?.contains("svg", ignoreCase = true) == true) return null
    val href = attributes["href"]?.replace("&amp;", "&") ?: return null
    if (href.isBlank()) return null
    val url = runCatching { Url(pageUrl).resolving(href).buildString() }.getOrNull() ?: return null
    if (Url(url).encodedPath.endsWith(".svg", ignoreCase = true)) return null
    return WebsiteIcon(url, iconSize(attributes["sizes"].orEmpty(), touch))
}

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
