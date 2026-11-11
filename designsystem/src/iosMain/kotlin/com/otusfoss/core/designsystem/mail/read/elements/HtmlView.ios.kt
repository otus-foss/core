package com.otusfoss.core.designsystem.mail.read.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.otusfoss.core.designsystem.mail.read.html.HtmlQuote
import com.otusfoss.core.designsystem.mail.read.html.RenderKey
import com.otusfoss.core.designsystem.mail.read.html.isShowImagesLink
import com.otusfoss.core.designsystem.mail.read.html.newRenderKey
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.WebKit.WKContentRuleList
import platform.WebKit.WKContentRuleListStore
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationActionPolicy.WKNavigationActionPolicyAllow
import platform.WebKit.WKNavigationActionPolicy.WKNavigationActionPolicyCancel
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebsiteDataStore
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
    insets: PaddingValues,
) {
    val openLink by rememberUpdatedState(onOpenLink)
    val showRemote by rememberUpdatedState(onShowRemote)
    val renderKey = remember { newRenderKey() }
    val page = rememberSandboxedPage(html, loadRemote, onShowRemote != null, quote, insets, renderKey)
    // WebKit keeps only a weak reference to its delegate.
    val delegate =
        remember { LinkDelegate(renderKey, onOpenLink = { openLink(it) }, onShowRemote = { showRemote?.invoke() }) }
    if (page == null) {
        // The view keeps its place while the first page is built.
        Box(modifier)
        return
    }
    key(page) {
        UIKitView(
            factory = { htmlWebView(page.document, page.loadRemote, delegate) },
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun htmlWebView(
    document: String,
    loadRemote: Boolean,
    delegate: WKNavigationDelegateProtocol,
): WKWebView {
    val configuration =
        WKWebViewConfiguration().apply {
            defaultWebpagePreferences.allowsContentJavaScript = false
            // Each view gets a fresh store in memory, so cookies and cached responses a
            // message's images leave cannot follow the user to other messages.
            websiteDataStore = WKWebsiteDataStore.nonPersistentDataStore()
        }
    return WKWebView(CGRectZero.readValue(), configuration).apply {
        navigationDelegate = delegate
        setOpaque(false)
        if (loadRemote) {
            loadHTMLString(document, baseURL = null)
        } else {
            withNetworkBlock { rules ->
                if (rules != null) configuration.userContentController.addContentRuleList(rules)
                loadHTMLString(document, baseURL = null)
            }
        }
    }
}

/**
 * Allows only the document's own load; a tapped openable link goes to [onOpenLink], and a tapped
 * image placeholder of this render, by its [key], to [onShowRemote].
 */
private class LinkDelegate(
    private val key: RenderKey,
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
            tapped && isShowImagesLink(url, key) -> onShowRemote()
            tapped && isOpenableLink(url) -> onOpenLink(url)
        }
        val ownLoad = url == "about:blank"
        decisionHandler(if (ownLoad) WKNavigationActionPolicyAllow else WKNavigationActionPolicyCancel)
    }
}

/**
 * Passes [use] a content rule list that blocks every web request, compiled once, as Android's
 * `blockNetworkLoads` does beneath the Content Security Policy. WebKit calls back on the main
 * thread. Null when WebKit cannot compile it; the policy alone still blocks remote content then.
 */
private fun withNetworkBlock(use: (WKContentRuleList?) -> Unit) {
    val cached = networkBlock
    if (cached != null) return use(cached)
    val store = WKContentRuleListStore.defaultStore() ?: return use(null)
    store.compileContentRuleListForIdentifier(
        NETWORK_BLOCK_ID,
        encodedContentRuleList = NETWORK_BLOCK_RULES,
    ) { rules, _ ->
        networkBlock = rules
        use(rules)
    }
}

private var networkBlock: WKContentRuleList? = null

private const val NETWORK_BLOCK_ID = "otus-html-view-network-block"

// Rule filters have no alternation, so each scheme gets its own rule.
private const val NETWORK_BLOCK_RULES =
    """[{"trigger":{"url-filter":"^https?:"},"action":{"type":"block"}},""" +
        """{"trigger":{"url-filter":"^wss?:"},"action":{"type":"block"}}]"""
