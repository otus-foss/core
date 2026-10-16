package com.otus.designsystem.elements

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
public actual fun HtmlView(
    html: String,
    loadRemote: Boolean,
    onOpenLink: (String) -> Unit,
    modifier: Modifier,
) {
    val openLink by rememberUpdatedState(onOpenLink)
    val document = remember(html, loadRemote) { sandboxedHtml(html, loadRemote) }
    // A new document gets a new view, so no page or setting of the last one carries over.
    key(document) {
        AndroidView(
            factory = { context ->
                sandboxedWebView(context, loadRemote) { openLink(it) }.apply {
                    loadDataWithBaseURL(null, document, "text/html", "utf-8", null)
                }
            },
            modifier = modifier,
            onRelease = WebView::destroy,
        )
    }
}

/** Sets scripts and file access off even where that is the default, so a platform change cannot turn them on. */
private fun sandboxedWebView(
    context: Context,
    loadRemote: Boolean,
    onOpenLink: (String) -> Unit,
): WebView =
    WebView(context).apply {
        setBackgroundColor(Color.TRANSPARENT)
        settings.javaScriptEnabled = false
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.blockNetworkLoads = !loadRemote
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        // Otus is dark, so pages without their own dark colors are darkened to match.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) settings.isAlgorithmicDarkeningAllowed = true
        webViewClient = LinkClient(onOpenLink)
    }

/** Keeps every navigation out of the view; a tapped openable link goes to [onOpenLink]. */
private class LinkClient(
    private val onOpenLink: (String) -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        val url = request.url.toString()
        if (request.isForMainFrame && request.hasGesture() && isOpenableLink(url)) onOpenLink(url)
        return true
    }
}
