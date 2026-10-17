package com.otus.designsystem.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shows an HTML document, such as a message body, and scrolls it inside its own bounds. Scripts
 * never run, and frames, forms, and plugins stay blocked. A tapped web, mail, or phone link goes
 * to [onOpenLink] instead of opening in the view; other links do nothing. Remote images, styles,
 * and fonts load only while [loadRemote] is true, and no DNS prefetch or preconnect hint is
 * followed without it, so a message cannot report that it was read.
 */
@Composable
public expect fun HtmlView(
    html: String,
    loadRemote: Boolean,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
)

/**
 * [html] behind a Content Security Policy that blocks every outside resource except, with
 * [loadRemote], images, styles, and fonts. The policy and base styles come first, so the
 * document's own markup cannot load anything before the policy applies. A policy the document
 * declares itself can only block more.
 *
 * Without [loadRemote], DNS prefetching is off and the document's `<link>` elements become inert:
 * the policy does not cover `dns-prefetch` or `preconnect` hints, which would otherwise reach the
 * sender's server, and every other link it could load is blocked anyway.
 */
internal fun sandboxedHtml(
    html: String,
    loadRemote: Boolean,
): String {
    val remote = if (loadRemote) " http: https:" else ""
    val policy =
        "default-src 'none'; img-src data:$remote; style-src 'unsafe-inline'$remote; " +
            "font-src data:$remote; form-action 'none'"
    val prefetch = if (loadRemote) "" else "<meta http-equiv=\"x-dns-prefetch-control\" content=\"off\">"
    val body = if (loadRemote) html else html.replace(LINK_TAG, "<otus-inert-link")
    return "<meta http-equiv=\"Content-Security-Policy\" content=\"$policy\">" +
        prefetch +
        "<meta name=\"viewport\" content=\"width=device-width\">" +
        "<style>body{margin:0;overflow-wrap:anywhere}img{max-width:100%;height:auto}</style>" +
        body
}

/** An HTML `<link>` start tag: the tag name ends at whitespace, `/`, or `>`, in any case. */
private val LINK_TAG = Regex("<link(?=[\\s/>])", RegexOption.IGNORE_CASE)

/** Whether [HtmlView] passes [url] on: web, mail, and phone links, never app or file links. */
internal fun isOpenableLink(url: String): Boolean = url.substringBefore(':').lowercase() in OPENABLE_SCHEMES

private val OPENABLE_SCHEMES = setOf("http", "https", "mailto", "tel")
