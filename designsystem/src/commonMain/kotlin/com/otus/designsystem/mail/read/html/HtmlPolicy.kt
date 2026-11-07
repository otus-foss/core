package com.otus.designsystem.mail.read.html

/**
 * [html] sealed as the Read view seals a message whose remote content is off, for a view that
 * shows it outside the Read view, such as a print: it loads nothing from the network, DNS
 * prefetching is off, and its `<link>` elements are inert.
 */
public fun offlineHtml(html: String): String = policyHead(loadRemote = false) + inertLinks(html)

/**
 * The Content Security Policy that blocks every outside resource except, with [loadRemote],
 * images, styles, and fonts, and without it the DNS-prefetch switch. It goes before the
 * document's own markup, so nothing loads before the policy applies.
 */
internal fun policyHead(loadRemote: Boolean): String {
    val remote = if (loadRemote) " http: https:" else ""
    val policy =
        "default-src 'none'; img-src data:$remote; style-src 'unsafe-inline'$remote; " +
            "font-src data:$remote; form-action 'none'"
    val prefetch = if (loadRemote) "" else "<meta http-equiv=\"x-dns-prefetch-control\" content=\"off\">"
    return "<meta http-equiv=\"Content-Security-Policy\" content=\"$policy\">$prefetch"
}

/**
 * [html] with its `<link>` elements renamed so they load nothing. The policy does not cover
 * `dns-prefetch` or `preconnect` hints, which would otherwise reach the sender's server.
 */
internal fun inertLinks(html: String): String = html.replace(LINK_TAG, "<otus-inert-link")

/** An HTML `<link>` start tag: the tag name ends at whitespace, `/`, or `>`, in any case. */
private val LINK_TAG = Regex("<link(?=[\\s/>])", RegexOption.IGNORE_CASE)
