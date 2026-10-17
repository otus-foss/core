package com.otus.designsystem.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.otus.designsystem.theme.OtusPalette

/**
 * Shows an HTML document, such as a message body, and scrolls it inside its own bounds. Scripts
 * never run, and frames, forms, and plugins stay blocked. A tapped web, mail, or phone link goes
 * to [onOpenLink] instead of opening in the view; other links do nothing. Remote images, styles,
 * and fonts load only while [loadRemote] is true, so a message cannot report that it was read.
 * Colors follow the theme the user picked, not the platform's dark mode.
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
 * A document without colors of its own takes the text and link colors of [palette], the theme the
 * user picked, on the page behind the view. One that sets its own colors keeps the white page and
 * black text its author designed against, so it stays readable on every platform and theme.
 */
internal fun sandboxedHtml(
    html: String,
    loadRemote: Boolean,
    palette: OtusPalette,
): String {
    val remote = if (loadRemote) " http: https:" else ""
    val policy =
        "default-src 'none'; img-src data:$remote; style-src 'unsafe-inline'$remote; " +
            "font-src data:$remote; form-action 'none'"
    val page =
        if (hasOwnColors(html)) {
            "body{color:#000;background:#fff}"
        } else {
            val scheme = if (palette.isLight) "light" else "dark"
            ":root{color-scheme:$scheme}body{color:${palette.text1.css()}}a{color:${palette.accentLight.css()}}"
        }
    return "<meta http-equiv=\"Content-Security-Policy\" content=\"$policy\">" +
        "<meta name=\"viewport\" content=\"width=device-width\">" +
        "<style>body{margin:0;overflow-wrap:anywhere}img{max-width:100%;height:auto}$page</style>" +
        html
}

/** Whether [html] sets a text or background color, in a color attribute, a style attribute, or a style sheet. */
internal fun hasOwnColors(html: String): Boolean =
    COLOR_ATTRIBUTE.containsMatchIn(html) ||
        STYLES.findAll(html).any { styles -> styles.groupValues.drop(1).any(CSS_COLOR::containsMatchIn) }

private val COLOR_ATTRIBUTE = Regex("""(?i)<[^>]*\s(?:bg)?color\s*=""")
private val STYLES = Regex("""(?is)<style\b[^>]*>(.*?)</style>|\sstyle\s*=\s*(?:"([^"]*)"|'([^']*)')""")
private val CSS_COLOR = Regex("""(?i)(?:^|[\s;{])(?:color|background(?:-color)?)\s*:""")

private fun Color.css(): String = "#" + (toArgb() and RGB).toString(HEX).padStart(RGB_DIGITS, '0')

private const val RGB = 0xFFFFFF
private const val HEX = 16
private const val RGB_DIGITS = 6

/** Whether [HtmlView] passes [url] on: web, mail, and phone links, never app or file links. */
internal fun isOpenableLink(url: String): Boolean = url.substringBefore(':').lowercase() in OPENABLE_SCHEMES

private val OPENABLE_SCHEMES = setOf("http", "https", "mailto", "tel")
