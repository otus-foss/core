package com.otus.designsystem.mail.read.elements

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.colors.toHex
import kotlin.math.roundToInt

/**
 * Shows an HTML document, such as a message body, and scrolls it inside its own bounds. Scripts
 * never run, and frames, forms, and plugins stay blocked. A tapped web, mail, or phone link goes
 * to [onOpenLink] instead of opening in the view; other links do nothing. Remote images, styles,
 * and fonts load only while [loadRemote] is true, and no DNS prefetch or preconnect hint is
 * followed without it, so a message cannot report that it was read.
 * Colors follow the theme the user picked, not the platform's dark mode.
 *
 * With [onShowRemote] and without [loadRemote], each remote image becomes a dashed placeholder of
 * its size, and tapping one runs [onShowRemote]. A [quote] folds under the body with its author
 * and date and unfolds on a tap, without scripts. The top and bottom of [insets] add room above and
 * under the page, so it can scroll clear of parts that float over the view's edges, such as a
 * header, strips, and an action bar.
 */
@Composable
public expect fun HtmlView(
    html: String,
    loadRemote: Boolean,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowRemote: (() -> Unit)? = null,
    quote: HtmlQuote? = null,
    insets: PaddingValues = PaddingValues(),
)

/**
 * [html] behind a Content Security Policy that blocks every outside resource except, with
 * [loadRemote], images, styles, and fonts. The policy and base styles come first, so the
 * document's own markup cannot load anything before the policy applies. A policy the document
 * declares itself can only block more.
 *
 * A document without colors of its own takes the text and link colors of [palette], the theme the
 * user picked, on the page behind the view. One that sets its own colors, in its body or its quote,
 * keeps the white page and black text its author designed against, so it stays readable on every
 * platform and theme.
 *
 * Without [loadRemote], DNS prefetching is off and the document's `<link>` elements become inert:
 * the policy does not cover `dns-prefetch` or `preconnect` hints, which would otherwise reach the
 * sender's server, and every other link it could load is blocked anyway. With [placeholders] it
 * also swaps remote images for placeholders. A [quote] follows the sealed body as a fold. [key]
 * names the parts this function adds, so the message cannot fake or restyle them.
 */
internal fun sandboxedHtml(
    html: String,
    loadRemote: Boolean,
    palette: OtusPalette,
    key: RenderKey,
    placeholders: Boolean = false,
    quote: HtmlQuote? = null,
    spacers: HtmlSpacers = HtmlSpacers(),
): String {
    val remote = if (loadRemote) " http: https:" else ""
    val policy =
        "default-src 'none'; img-src data:$remote; style-src 'unsafe-inline'$remote; " +
            "font-src data:$remote; form-action 'none'"
    val prefetch = if (loadRemote) "" else "<meta http-equiv=\"x-dns-prefetch-control\" content=\"off\">"
    val document = if (quote == null) html else sealedHtml(html) + quoteFold(quote, palette, key)
    val blocked = !loadRemote && placeholders
    val inert = if (loadRemote) document else document.replace(LINK_TAG, "<otus-inert-link")
    val body = if (blocked) withImagePlaceholders(inert, key) else inert
    val margin = if (hasOwnLayout(html)) "0" else "0 ${spacers.right}px 0 ${spacers.left}px"
    val page =
        if (hasOwnColors(html) || quote?.html?.let(::hasOwnColors) == true) {
            "body{color:#000;background:#fff}"
        } else {
            val scheme = if (palette.isLight) "light" else "dark"
            ":root{color-scheme:$scheme}body{color:${palette.text1.css()}}a{color:${palette.accentLight.css()}}"
        }
    return "<meta http-equiv=\"Content-Security-Policy\" content=\"$policy\">" +
        prefetch +
        "<meta name=\"viewport\" content=\"width=device-width\">" +
        "<style>body{margin:$margin;overflow-wrap:anywhere}img{max-width:100%;height:auto}$page" +
        (if (blocked) placeholderStyle(palette, key) else "") +
        "</style>" +
        // Spacers, not body padding, since a mail's own styles may reset the body's padding.
        spacer(spacers.top) +
        body +
        spacer(spacers.bottom)
}

/**
 * Empty space above and under a page, and beside one without a layout of its own, in CSS pixels,
 * which are dp at a device-width viewport.
 */
internal data class HtmlSpacers(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0,
)

/** [HtmlView]'s insets as spacers. */
internal fun PaddingValues.spacers(): HtmlSpacers =
    HtmlSpacers(
        calculateTopPadding().value.roundToInt(),
        calculateBottomPadding().value.roundToInt(),
        calculateLeftPadding(LayoutDirection.Ltr).value.roundToInt(),
        calculateRightPadding(LayoutDirection.Ltr).value.roundToInt(),
    )

private fun spacer(height: Int): String = if (height > 0) "<div style=\"height:${height}px\"></div>" else ""

/** Whether [html] sets a text or background color, in a color attribute, a style attribute, or a style sheet. */
internal fun hasOwnColors(html: String): Boolean = COLOR_ATTRIBUTE.containsMatchIn(html) || stylesMatch(html, CSS_COLOR)

/** Whether [html] lays itself out, in a table or on a background of its own, as designed mail does. */
internal fun hasOwnLayout(html: String): Boolean =
    TABLE_TAG.containsMatchIn(html) ||
        BACKGROUND_ATTRIBUTE.containsMatchIn(html) ||
        stylesMatch(html, CSS_BACKGROUND)

/** Whether [declaration] matches inside a style sheet or style attribute of [html]. */
private fun stylesMatch(
    html: String,
    declaration: Regex,
): Boolean = STYLES.findAll(html).any { styles -> styles.groupValues.drop(1).any(declaration::containsMatchIn) }

private val TABLE_TAG = Regex("<table(?=[\\s/>])", RegexOption.IGNORE_CASE)
private val BACKGROUND_ATTRIBUTE = Regex("""(?i)<[^>]*\s(?:bgcolor|background)\s*=""")
private val CSS_BACKGROUND = Regex("""(?i)(?:^|[\s;{])background(?:-color|-image)?\s*:""")

private val COLOR_ATTRIBUTE = Regex("""(?i)<[^>]*\s(?:bg)?color\s*=""")
private val STYLES = Regex("""(?is)<style\b[^>]*>(.*?)</style>|\sstyle\s*=\s*(?:"([^"]*)"|'([^']*)')""")
private val CSS_COLOR = Regex("""(?i)(?:^|[\s;{])(?:color|background(?:-color)?)\s*:""")

internal fun Color.css(): String = toHex().lowercase()

/** An HTML `<link>` start tag: the tag name ends at whitespace, `/`, or `>`, in any case. */
private val LINK_TAG = Regex("<link(?=[\\s/>])", RegexOption.IGNORE_CASE)

/** Whether [HtmlView] passes [url] on: web, mail, and phone links, never app or file links. */
internal fun isOpenableLink(url: String): Boolean = url.substringBefore(':').lowercase() in OPENABLE_SCHEMES

private val OPENABLE_SCHEMES = setOf("http", "https", "mailto", "tel")
