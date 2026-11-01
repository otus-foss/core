package com.otus.designsystem.mail.read.elements

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.ContextThemeWrapper
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.otus.designsystem.theme.colors.OtusColors

@Composable
public actual fun HtmlView(
    html: String,
    loadRemote: Boolean,
    onOpenLink: (String) -> Unit,
    modifier: Modifier,
    onShowRemote: (() -> Unit)?,
    quote: HtmlQuote?,
    insets: PaddingValues,
) {
    val openLink by rememberUpdatedState(onOpenLink)
    val showRemote by rememberUpdatedState(onShowRemote)
    val palette = OtusColors.palette
    val renderKey = remember { newRenderKey() }
    val page = rememberSandboxedPage(html, loadRemote, onShowRemote != null, quote, insets, renderKey)
    if (page == null) {
        // The view keeps its place while the first page is built.
        Box(modifier)
        return
    }
    // A new page gets a new view, so no page or setting of the last one carries over.
    key(page) {
        AndroidView(
            factory = { context ->
                val links =
                    LinkClient(renderKey, onOpenLink = { openLink(it) }, onShowRemote = { showRemote?.invoke() })
                sandboxedWebView(context, page.loadRemote, darken = !palette.isLight, links).apply {
                    loadDataWithBaseURL(null, page.document, "text/html", "utf-8", null)
                }
            },
            modifier = modifier,
            onRelease = WebView::destroy,
        )
    }
}

/**
 * Sets scripts and file access off even where that is the default, so a platform change cannot
 * turn them on. Safe Browsing, which can send the addresses of remote images to Google, is off,
 * and remote images are fetched with a User-Agent that names neither the device model nor its
 * build. The view gets a light or dark platform theme by [darken], the theme the user
 * picked, because WebView darkens pages only under a dark platform theme, whatever the activity's.
 */
private fun sandboxedWebView(
    context: Context,
    loadRemote: Boolean,
    darken: Boolean,
    links: LinkClient,
): WebView =
    FittingWebView(ContextThemeWrapper(context, if (darken) THEME_DARK else THEME_LIGHT)).apply {
        setBackgroundColor(Color.TRANSPARENT)
        with(settings) {
            javaScriptEnabled = false
            allowFileAccess = false
            allowContentAccess = false
            blockNetworkLoads = !loadRemote
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            userAgentString = genericUserAgent(userAgentString)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) safeBrowsingEnabled = false
            // On Android 13 and later, a page with its own light colors is darkened under a dark theme.
            // Elsewhere it keeps its white page, which sandboxedHtml gives it.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) isAlgorithmicDarkeningAllowed = darken
        }
        webViewClient = links
    }

/**
 * [default] reduced the way Chrome reduces its own: the device part, such as
 * `(Linux; Android 14; Pixel 8 Build/AP2A; wv)`, becomes `(Linux; Android 10; K; wv)`, and the
 * WebView version keeps only its major number, so an image's host cannot tell the phone model,
 * Android version, or WebView build.
 */
internal fun genericUserAgent(default: String): String =
    CHROME_VERSION.replace(DEVICE.replace(default, "(Linux; Android 10; K; wv)"), "Chrome/$1.0.0.0")

private val DEVICE = Regex("""\(Linux; Android[^)]*\)""")
private val CHROME_VERSION = Regex("""Chrome/(\d+)[\d.]*""")

/**
 * Keeps every navigation out of the view; a tapped openable link goes to [onOpenLink], and a
 * tapped image placeholder to [onShowRemote].
 */
private class LinkClient(
    private val key: RenderKey,
    private val onOpenLink: (String) -> Unit,
    private val onShowRemote: () -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        val url = request.url.toString()
        val tapped = request.isForMainFrame && request.hasGesture()
        when {
            tapped && isShowImagesLink(url, key) -> onShowRemote()
            tapped && isOpenableLink(url) -> onOpenLink(url)
        }
        return true
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        // The page lays out after the load ends, so its width is known one frame later.
        val fitting = view as? FittingWebView ?: return
        fitting.post { fitting.fitWidth() }
    }
}

/**
 * A view that zooms a page wider than itself out until it fits. Mail laid out at a fixed width,
 * such as a 600 px newsletter, would otherwise need sideways scrolling, since the page's viewport
 * is the view's width.
 */
private class FittingWebView(
    context: Context,
) : WebView(context) {
    fun fitWidth() {
        val content = computeHorizontalScrollRange()
        if (width > 0 && content > width) zoomBy((width.toFloat() / content).coerceAtLeast(MIN_FIT_ZOOM))
    }
}

/** The furthest a page zooms out to fit, so a very wide page stays readable and scrolls instead. */
private const val MIN_FIT_ZOOM = 0.3f

private const val THEME_DARK = android.R.style.Theme_Material
private const val THEME_LIGHT = android.R.style.Theme_Material_Light
