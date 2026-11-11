package com.otusfoss.core.designsystem.mail.read.elements

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.mail.read.html.HtmlQuote
import com.otusfoss.core.designsystem.mail.read.html.HtmlScan
import com.otusfoss.core.designsystem.mail.read.html.RenderKey
import com.otusfoss.core.designsystem.mail.read.html.css
import com.otusfoss.core.designsystem.mail.read.html.inertLinks
import com.otusfoss.core.designsystem.mail.read.html.placeholderStyle
import com.otusfoss.core.designsystem.mail.read.html.policyHead
import com.otusfoss.core.designsystem.mail.read.html.quoteFold
import com.otusfoss.core.designsystem.mail.read.html.scanHtml
import com.otusfoss.core.designsystem.mail.read.html.sealedHtml
import com.otusfoss.core.designsystem.mail.read.html.withImagePlaceholders
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.colors.OtusPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

/** A [sandboxedHtml] [document] and whether it may load remote content, so its view can match it. */
internal data class SandboxedPage(
    val document: String,
    val loadRemote: Boolean,
)

/**
 * [HtmlView]'s page, built off the main thread since a message can be large, or null until the
 * first one is ready. While a change of an argument rebuilds it, the last page stays.
 */
@Composable
internal fun rememberSandboxedPage(
    html: String,
    loadRemote: Boolean,
    placeholders: Boolean,
    quote: HtmlQuote?,
    insets: PaddingValues,
    key: RenderKey,
): SandboxedPage? {
    val palette = OtusColors.palette
    val page by produceState<SandboxedPage?>(null, html, loadRemote, palette, placeholders, quote, insets, key) {
        value =
            withContext(Dispatchers.Default) {
                val spacers = insets.spacers()
                SandboxedPage(sandboxedHtml(html, loadRemote, palette, key, placeholders, quote, spacers), loadRemote)
            }
    }
    return page
}

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
    val document = if (quote == null) html else sealedHtml(html) + quoteFold(quote, palette, key)
    val blocked = !loadRemote && placeholders
    val inert = if (loadRemote) document else inertLinks(document)
    val body = if (blocked) withImagePlaceholders(inert, key) else inert
    val margin = if (hasOwnLayout(html)) "0" else "0 ${spacers.right}px 0 ${spacers.left}px"
    val page =
        if (hasOwnColors(html) || quote?.html?.let(::hasOwnColors) == true) {
            "body{color:#000;background:#fff}"
        } else {
            val scheme = if (palette.isLight) "light" else "dark"
            ":root{color-scheme:$scheme}body{color:${palette.text1.css()}}a{color:${palette.accentLight.css()}}"
        }
    return policyHead(loadRemote) +
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
internal fun hasOwnColors(html: String): Boolean = scanHtml(html).sets(COLOR_ATTRIBUTES, CSS_COLOR)

/** Whether [html] lays itself out, in a table or on a background of its own, as designed mail does. */
internal fun hasOwnLayout(html: String): Boolean {
    val scan = scanHtml(html)
    return scan.tags.any { it.name == "table" && !it.closing } || scan.sets(BACKGROUND_ATTRIBUTES, CSS_BACKGROUND)
}

/**
 * Whether a start tag has one of [attributes], or a style attribute or style sheet has a
 * [declaration]. The scan is linear, since a message's markup comes from its sender.
 */
private fun HtmlScan.sets(
    attributes: Set<String>,
    declaration: Regex,
): Boolean =
    tags.any { tag -> attributes.any(tag.attributes::containsKey) } ||
        tags.any { tag -> tag.attributes["style"]?.let(declaration::containsMatchIn) == true } ||
        styleSheets.any(declaration::containsMatchIn)

private val BACKGROUND_ATTRIBUTES = setOf("bgcolor", "background")
private val CSS_BACKGROUND = Regex("""(?i)(?:^|[\s;{])background(?:-color|-image)?\s*:""")

private val COLOR_ATTRIBUTES = setOf("color", "bgcolor")
private val CSS_COLOR = Regex("""(?i)(?:^|[\s;{])(?:color|background(?:-color)?)\s*:""")

/** Whether [HtmlView] passes [url] on: web, mail, and phone links, never app or file links. */
internal fun isOpenableLink(url: String): Boolean = url.substringBefore(':').lowercase() in OPENABLE_SCHEMES

private val OPENABLE_SCHEMES = setOf("http", "https", "mailto", "tel")
