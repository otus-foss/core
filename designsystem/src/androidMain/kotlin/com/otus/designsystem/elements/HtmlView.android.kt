package com.otus.designsystem.elements

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.ContextThemeWrapper
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.otus.designsystem.theme.OtusColors
import kotlin.math.roundToInt

@Composable
public actual fun HtmlView(
    html: String,
    loadRemote: Boolean,
    onOpenLink: (String) -> Unit,
    modifier: Modifier,
    onShowRemote: (() -> Unit)?,
    quote: HtmlQuote?,
    bottomInset: Dp,
) {
    val openLink by rememberUpdatedState(onOpenLink)
    val showRemote by rememberUpdatedState(onShowRemote)
    val palette = OtusColors.palette
    val placeholders = onShowRemote != null
    val renderKey = remember { newRenderKey() }
    val document =
        remember(html, loadRemote, palette, placeholders, quote, bottomInset) {
            // At a device-width viewport a CSS pixel is a dp.
            sandboxedHtml(html, loadRemote, palette, renderKey, placeholders, quote, bottomInset.value.roundToInt())
        }
    // A new document gets a new view, so no page or setting of the last one carries over.
    key(document) {
        AndroidView(
            factory = { context ->
                val links =
                    LinkClient(renderKey, onOpenLink = { openLink(it) }, onShowRemote = { showRemote?.invoke() })
                sandboxedWebView(context, loadRemote, darken = !palette.isLight, links).apply {
                    loadDataWithBaseURL(null, document, "text/html", "utf-8", null)
                }
            },
            modifier = modifier,
            onRelease = WebView::destroy,
        )
    }
}

/**
 * Sets scripts and file access off even where that is the default, so a platform change cannot
 * turn them on. The view gets a light or dark platform theme by [darken], the theme the user
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
        settings.javaScriptEnabled = false
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.blockNetworkLoads = !loadRemote
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        // On Android 13 and later, a page with its own light colors is darkened under a dark theme.
        // Elsewhere it keeps its white page, which sandboxedHtml gives it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) settings.isAlgorithmicDarkeningAllowed = darken
        webViewClient = links
    }

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
        (view as? FittingWebView)?.let { fitting -> fitting.post { fitting.fitWidth() } }
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
