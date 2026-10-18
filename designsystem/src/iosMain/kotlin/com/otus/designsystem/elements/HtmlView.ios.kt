package com.otus.designsystem.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.otus.designsystem.theme.OtusColors
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationActionPolicy.WKNavigationActionPolicyAllow
import platform.WebKit.WKNavigationActionPolicy.WKNavigationActionPolicyCancel
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
public actual fun HtmlView(
    html: String,
    loadRemote: Boolean,
    onOpenLink: (String) -> Unit,
    modifier: Modifier,
    onShowRemote: (() -> Unit)?,
    quote: HtmlQuote?,
) {
    val openLink by rememberUpdatedState(onOpenLink)
    val showRemote by rememberUpdatedState(onShowRemote)
    val palette = OtusColors.palette
    val placeholders = onShowRemote != null
    val document =
        remember(html, loadRemote, palette, placeholders, quote) {
            sandboxedHtml(html, loadRemote, palette, placeholders, quote)
        }
    // WebKit keeps only a weak reference to its delegate.
    val delegate = remember { LinkDelegate(onOpenLink = { openLink(it) }, onShowRemote = { showRemote?.invoke() }) }
    key(document) {
        UIKitView(
            factory = {
                val configuration =
                    WKWebViewConfiguration().apply { defaultWebpagePreferences.allowsContentJavaScript = false }
                WKWebView(CGRectZero.readValue(), configuration).apply {
                    navigationDelegate = delegate
                    setOpaque(false)
                    loadHTMLString(document, baseURL = null)
                }
            },
            modifier = modifier,
        )
    }
}

/**
 * Allows only the document's own load; a tapped openable link goes to [onOpenLink], and a tapped
 * image placeholder to [onShowRemote].
 */
private class LinkDelegate(
    private val onOpenLink: (String) -> Unit,
    private val onShowRemote: () -> Unit,
) : NSObject(),
    WKNavigationDelegateProtocol {
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val url =
            decidePolicyForNavigationAction.request.URL
                ?.absoluteString
                .orEmpty()
        val tapped = decidePolicyForNavigationAction.navigationType == WKNavigationTypeLinkActivated
        when {
            tapped && isShowImagesLink(url) -> onShowRemote()
            tapped && isOpenableLink(url) -> onOpenLink(url)
        }
        val ownLoad = url == "about:blank"
        decisionHandler(if (ownLoad) WKNavigationActionPolicyAllow else WKNavigationActionPolicyCancel)
    }
}
