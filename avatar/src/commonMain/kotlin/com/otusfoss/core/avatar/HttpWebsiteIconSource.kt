package com.otusfoss.core.avatar

import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Prefer large raster icons advertised by the website over small legacy favicons. */
public class HttpWebsiteIconSource(
    private val images: HttpAvatarImageLoader,
) : DomainIconSource {
    /** Parses off the caller's thread, which may be the UI thread, since the page is untrusted. */
    override suspend fun icon(domain: String): AvatarImage? =
        avatarOrNull {
            val page = images.websitePage("https://$domain/") ?: return@avatarOrNull null
            val urls = withContext(Dispatchers.Default) { websiteIconUrls(page) }
            urls.firstNotNullOfOrNull { avatarOrNull { images.load(it) } }
        }
}

internal fun websiteIconUrls(page: AvatarWebsitePage): List<String> =
    headLinkTags(page.html)
        .mapNotNull { websiteIcon(it, page.url) }
        .sortedByDescending { it.size }
        .map { it.url }
        .distinct()
        .take(MAX_WEBSITE_ICONS)
        .toList()

/**
 * The `<link>` tags of [html] before `</head>` and outside comments, found in one pass. Each step
 * continues after what it read, so hostile markup, such as thousands of unclosed `<!--` or `<link`,
 * takes linear time. An unclosed comment or tag hides the rest of the page, as in a browser.
 */
private fun headLinkTags(html: String): Sequence<String> =
    sequence {
        var start = html.indexOf('<')
        while (start >= 0 && !html.isHeadEnd(start)) {
            val link = html.isLinkTag(start)
            val end =
                when {
                    link -> html.indexOf('>', start)
                    html.startsWith(COMMENT_START, start) -> html.indexOf(COMMENT_END, start + COMMENT_START.length)
                    else -> start
                }
            if (end < 0) break
            if (link && end - start < MAX_LINK_TAG_LENGTH) yield(html.substring(start, end + 1))
            start = html.indexOf('<', end + 1)
        }
    }

/** Whether `</head>`, with optional spaces before `>`, starts at [index]. */
private fun String.isHeadEnd(index: Int): Boolean {
    if (!startsWith(HEAD_END, index, ignoreCase = true)) return false
    val close = (index + HEAD_END.length until length).firstOrNull { !this[it].isWhitespace() }
    return close != null && this[close] == '>'
}

/** Whether a `<link` tag, and not a longer tag name, starts at [index]. */
private fun String.isLinkTag(index: Int): Boolean {
    val after = getOrNull(index + LINK_START.length)
    return startsWith(LINK_START, index, ignoreCase = true) && after?.isLetterOrDigit() != true && after != '_'
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
    ICON_SIZE
        .findAll(sizes)
        .map { minOf(it.groupValues[1].toIntOrNull() ?: 0, it.groupValues[2].toIntOrNull() ?: 0) }
        .maxOrNull() ?: if (touch) DEFAULT_TOUCH_ICON_SIZE else 0

private class WebsiteIcon(
    val url: String,
    val size: Int,
)

// Each match starts at the beginning of a name or number, so a long one is read once rather than
// once per character.
private val LINK_ATTRIBUTE = Regex("(?<![\\w-])([\\w-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))")

private val ICON_SIZE = Regex("(?<!\\d)(\\d+)x(\\d+)", RegexOption.IGNORE_CASE)

private const val COMMENT_START = "<!--"
private const val COMMENT_END = "-->"
private const val HEAD_END = "</head"
private const val LINK_START = "<link"
private const val MAX_LINK_TAG_LENGTH = 2048
private const val MAX_WEBSITE_ICONS = 8
private const val DEFAULT_TOUCH_ICON_SIZE = 180
