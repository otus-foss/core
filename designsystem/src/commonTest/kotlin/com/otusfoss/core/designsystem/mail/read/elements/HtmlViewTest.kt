package com.otusfoss.core.designsystem.mail.read.elements

import androidx.compose.ui.graphics.Color
import com.otusfoss.core.designsystem.mail.read.html.HtmlQuote
import com.otusfoss.core.designsystem.mail.read.html.RenderKey
import com.otusfoss.core.designsystem.mail.read.html.isShowImagesLink
import com.otusfoss.core.designsystem.mail.read.html.newRenderKey
import com.otusfoss.core.designsystem.mail.read.html.sealedHtml
import com.otusfoss.core.designsystem.theme.colors.OtusPalette
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlViewTest {
    private val key = RenderKey("k1")

    @Test
    fun policyComesBeforeTheDocument() {
        val html = "<img src=\"https://tracker.example/p.gif\">"
        val sandboxed = renderHtml(html, loadRemote = false)
        assertTrue(sandboxed.startsWith("<meta http-equiv=\"Content-Security-Policy\""))
        assertTrue(sandboxed.endsWith(html))
    }

    @Test
    fun blocksRemoteResourcesUntilAllowed() {
        val blocked = policyOf(renderHtml("", loadRemote = false))
        assertEquals(
            "default-src 'none'; img-src data:; style-src 'unsafe-inline'; font-src data:; form-action 'none'",
            blocked,
        )
        val allowed = policyOf(renderHtml("", loadRemote = true))
        assertContains(allowed, "img-src data: http: https:")
        assertContains(allowed, "default-src 'none'")
        assertFalse("script-src" in allowed)
    }

    @Test
    fun aPlainDocumentTakesThePickedThemesColors() {
        val dark = renderHtml("<p>Hi</p>", loadRemote = false)
        assertContains(dark, ":root{color-scheme:dark}body{color:#ebeced}a{color:#7099c4}")
        val light = OtusPalette.Otus.copy(text1 = Color(0xFF151515), isLight = true)
        assertContains(
            renderHtml("<p>Hi</p>", loadRemote = false, palette = light),
            "color-scheme:light}body{color:#151515}",
        )
    }

    @Test
    fun aDocumentWithItsOwnColorsKeepsAWhitePage() {
        listOf(
            "<td bgcolor=\"#eee\">Hi</td>",
            "<font color=red>Hi</font>",
            "<p style=\"color: #333\">Hi</p>",
            "<style>body{background-color:#fafafa}</style>",
        ).forEach {
            assertTrue(hasOwnColors(it), it)
            assertContains(
                renderHtml(it, loadRemote = false),
                "body{color:#000;background:#fff}",
            )
        }
        assertFalse(hasOwnColors("<p>Pick a color: blue</p><a href=\"https://otus.example/colors\">Colors</a>"))
    }

    @Test
    fun connectionHintsStayInertUntilRemoteContentIsAllowed() {
        val html = "<LINK rel=preconnect href=https://u1.tracker.example><link\trel=\"dns-prefetch\" href=x>"
        val blocked = renderHtml(html, loadRemote = false)
        assertContains(blocked, "<meta http-equiv=\"x-dns-prefetch-control\" content=\"off\">")
        assertFalse("<link" in blocked.lowercase())
        assertContains(blocked, "<otus-inert-link rel=preconnect")
        val allowed = renderHtml(html, loadRemote = true)
        assertFalse("x-dns-prefetch-control" in allowed)
        assertTrue(allowed.endsWith(html))
    }

    @Test
    fun linkLikeTagsAndTextAreKept() {
        val html = "<linked>a &lt;link b</linked>"
        assertTrue(renderHtml(html, loadRemote = false).endsWith(html))
    }

    @Test
    fun opensOnlyWebMailAndPhoneLinks() {
        listOf("https://otus.example", "HTTP://otus.example", "mailto:a@otus.example", "tel:+41").forEach {
            assertTrue(isOpenableLink(it), it)
        }
        listOf("intent://scan", "file:///etc/hosts", "javascript:alert(1)", "about:blank", "relative/path").forEach {
            assertFalse(isOpenableLink(it), it)
        }
    }

    private fun policyOf(document: String): String = document.substringAfter("content=\"").substringBefore('"')

    @Test
    fun remoteImagesBecomePlaceholdersOfTheirSize() {
        val html =
            "<img src=\"https://t.example/a.png\" width=\"320\" height=\"80\">" +
                "<img style=\"width:200px;height:50px\" src='http://t.example/b.png'>" +
                "<IMG SRC=//t.example/c.png>" +
                "<img src=\"cid:logo\"><img src=\"data:image/png;base64,AA\">"
        val blocked = renderHtml(html, loadRemote = false, placeholders = true)
        assertContains(
            blocked,
            "<a href=\"otus-images:show?k=k1\" class=\"otus-k1-blocked\" " +
                "style=\"width:320px;max-width:100%;aspect-ratio:320/80;height:auto\"",
        )
        assertContains(blocked, "style=\"width:200px;max-width:100%;aspect-ratio:200/50;height:auto\" aria-label")
        assertContains(blocked, "style=\"width:100%;height:120px\"")
        assertFalse("t.example" in blocked)
        assertContains(blocked, "<img src=\"cid:logo\">")
        assertContains(blocked, "<img src=\"data:image/png;base64,AA\">")
        assertContains(blocked, ".otus-k1-blocked{display:inline-block!important;")
        assertFalse(isOpenableLink(key.showImagesUrl))
    }

    @Test
    fun imagesStayWithoutPlaceholdersOrOnceAllowed() {
        val html = "<img src=\"https://t.example/a.png\">"
        assertTrue(renderHtml(html, loadRemote = false).endsWith(html))
        assertTrue(renderHtml(html, loadRemote = true, placeholders = true).endsWith(html))
    }

    @Test
    fun aQuoteFoldsAfterTheBodyWithItsEscapedHeader() {
        val quote = HtmlQuote("Ada <Lovelace>", "AL", "10 Oct", "<p>Earlier <img src=\"https://t.example/q.png\"></p>")
        val page =
            renderHtml("<p>Reply</p>", loadRemote = false, placeholders = true, quote = quote)
        val body = page.substringAfter("<p>Reply</p>")
        assertContains(body, "<details class=\"otus-k1-toggle\"><summary class=\"otus-k1-label\">")
        assertContains(
            body,
            "<span class=\"otus-k1-avatar\">AL</span>Ada &lt;Lovelace&gt;" +
                "<span class=\"otus-k1-divider\"></span><span class=\"otus-k1-date\">10 Oct</span></span>",
        )
        assertContains(body, "<div class=\"otus-k1-text\"><p>Earlier <a href=\"otus-images:show?k=k1\"")
        assertContains(body, "Show quoted text")
        assertContains(body, "border-left:2px solid #252628")
        assertFalse("<script" in page)
    }

    @Test
    fun aQuoteWithoutAnAuthorHasNoPill() {
        val quote = HtmlQuote(name = "", initials = "", date = "", html = "<p>Earlier</p>")
        val page = renderHtml("<p>Reply</p>", loadRemote = true, quote = quote)
        assertFalse("<span class=\"otus-k1-pill\">" in page)
        assertContains(page, "<div class=\"otus-k1-body\"><div class=\"otus-k1-text\"><p>Earlier</p>")
    }

    @Test
    fun aQuotedGreaterThanSignDoesNotHideARemoteImage() {
        val html = "<img alt=\"a > b\" src=\"https://tracker.example/p.gif\">"
        val blocked = renderHtml(html, loadRemote = false, placeholders = true)
        assertFalse("tracker.example" in blocked, "The image becomes a placeholder")
    }

    @Test
    fun anOpenNoscriptOrObjectIsClosedBeforeTheFold() {
        val quote = HtmlQuote("Ada", "AL", "10 Oct", "<p>Earlier</p>")
        listOf("noscript", "object").forEach { name ->
            val sandboxed = renderHtml("<p>Hi<$name>", loadRemote = false, quote = quote)
            assertTrue("<$name></$name>" in sandboxed, "The $name closes before the fold")
        }
    }

    @Test
    fun placeholderSizesKeepPercentagesAndFillMissingSides() {
        val html =
            "<img src=\"https://t.example/a.png\" width=\"50%\" height=\"40\">" +
                "<img src=\"https://t.example/b.png\" height=\"60\">" +
                "<img src=\"https://t.example/c.png\" width=\"300\">"
        val blocked = renderHtml(html, loadRemote = false, placeholders = true)
        assertContains(blocked, "style=\"width:50%;height:40px\"")
        assertContains(blocked, "style=\"width:100%;height:60px\"")
        assertContains(blocked, "style=\"width:300px;height:120px\"")
    }

    @Test
    fun onlyThisRendersPlaceholderLinkShowsImages() {
        assertTrue(isShowImagesLink("otus-images:show?k=k1", key))
        listOf(
            "otus-images:show",
            "otus-images:show?k=k2",
            "otus-images:show?k=k1&x",
            "OTUS-IMAGES:show?k=k1",
        ).forEach {
            assertFalse(isShowImagesLink(it, key), it)
        }
        val spoofed = renderHtml("<a href=\"otus-images:show\">Show</a>", loadRemote = false)
        assertFalse("otus-images:show?k=k1" in spoofed)
        assertTrue(newRenderKey().showImagesUrl != newRenderKey().showImagesUrl)
    }

    @Test
    fun aQuoteThatSetsColorsKeepsAWhitePage() {
        val quote = HtmlQuote(name = "", initials = "", date = "", html = "<p style=\"color:#333\">Earlier</p>")
        assertContains(
            renderHtml("<p>Reply</p>", loadRemote = false, quote = quote),
            "body{color:#000;background:#fff}",
        )
    }

    @Test
    fun anUnclosedElementInTheBodyNeitherSwallowsNorWrapsTheFold() {
        val quote = HtmlQuote("Ada", "A", "10 Oct", "<p>Earlier</p>")
        mapOf(
            "<p>Hi<style>p{}" to "<p>Hi<style>p{}</style>",
            "<p>Hi<a href=\"https://evil.example\">tap" to "tap</a>",
            "<p>Hi<!-- note" to "<!-- note-->",
            "<textarea>Hi" to "<textarea>Hi</textarea>",
            "<p>Hi</p><p title=\"a>b" to "<p>Hi</p>",
        ).forEach { (html, sealed) ->
            val page = renderHtml(html, loadRemote = false, quote = quote)
            val fold = page.indexOf("<style>.otus-k1-quote{")
            assertTrue(fold > 0, html)
            assertTrue(page.substring(0, fold).endsWith(sealed), html)
        }
    }

    @Test
    fun anUnclosedElementInTheQuoteKeepsTheFoldWhole() {
        val quote = HtmlQuote(name = "", initials = "", date = "", html = "<a href=\"https://evil.example\"><style>")
        val page = renderHtml("<p>Reply</p>", loadRemote = false, quote = quote)
        assertTrue(page.endsWith("<style></style></a></div></div></div>"))
    }

    @Test
    fun sealingKeepsBalancedMarkupAndDropsACutOffTag() {
        listOf("<p>Hi</p>", "<a href=x>a</a><!-- c --><style>a{}</style>", "<svg/><table><tr><td>x</td></tr></table>")
            .forEach { assertEquals(it, sealedHtml(it)) }
        assertEquals("<p>Hi</p>", sealedHtml("<p>Hi</p><img src=\"x"))
        assertEquals("<a><b>x</b></a>", sealedHtml("<a><b>x</b>"))
        assertEquals("<STYLE>a{}</style>", sealedHtml("<STYLE>a{}"))
    }

    private fun renderHtml(
        html: String,
        loadRemote: Boolean,
        palette: OtusPalette = OtusPalette.Otus,
        placeholders: Boolean = false,
        quote: HtmlQuote? = null,
    ): String = sandboxedHtml(html, loadRemote, palette, key, placeholders, quote)
}
