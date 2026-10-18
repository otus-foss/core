package com.otus.designsystem.elements

import androidx.compose.ui.graphics.Color
import com.otus.designsystem.theme.OtusPalette
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlViewTest {
    @Test
    fun policyComesBeforeTheDocument() {
        val html = "<img src=\"https://tracker.example/p.gif\">"
        val sandboxed = sandboxedHtml(html, loadRemote = false, OtusPalette.Otus)
        assertTrue(sandboxed.startsWith("<meta http-equiv=\"Content-Security-Policy\""))
        assertTrue(sandboxed.endsWith(html))
    }

    @Test
    fun blocksRemoteResourcesUntilAllowed() {
        val blocked = policyOf(sandboxedHtml("", loadRemote = false, OtusPalette.Otus))
        assertEquals(
            "default-src 'none'; img-src data:; style-src 'unsafe-inline'; font-src data:; form-action 'none'",
            blocked,
        )
        val allowed = policyOf(sandboxedHtml("", loadRemote = true, OtusPalette.Otus))
        assertContains(allowed, "img-src data: http: https:")
        assertContains(allowed, "default-src 'none'")
        assertFalse("script-src" in allowed)
    }

    @Test
    fun aPlainDocumentTakesThePickedThemesColors() {
        val dark = sandboxedHtml("<p>Hi</p>", loadRemote = false, OtusPalette.Otus)
        assertContains(dark, ":root{color-scheme:dark}body{color:#ebeced}a{color:#618bb6}")
        val light = OtusPalette.Otus.copy(text1 = Color(0xFF151515), isLight = true)
        assertContains(sandboxedHtml("<p>Hi</p>", loadRemote = false, light), "color-scheme:light}body{color:#151515}")
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
            assertContains(sandboxedHtml(it, loadRemote = false, OtusPalette.Otus), "body{color:#000;background:#fff}")
        }
        assertFalse(hasOwnColors("<p>Pick a color: blue</p><a href=\"https://otus.example/colors\">Colors</a>"))
    }

    @Test
    fun connectionHintsStayInertUntilRemoteContentIsAllowed() {
        val html = "<LINK rel=preconnect href=https://u1.tracker.example><link\trel=\"dns-prefetch\" href=x>"
        val blocked = sandboxedHtml(html, loadRemote = false, OtusPalette.Otus)
        assertContains(blocked, "<meta http-equiv=\"x-dns-prefetch-control\" content=\"off\">")
        assertFalse("<link" in blocked.lowercase())
        assertContains(blocked, "<otus-inert-link rel=preconnect")
        val allowed = sandboxedHtml(html, loadRemote = true, OtusPalette.Otus)
        assertFalse("x-dns-prefetch-control" in allowed)
        assertTrue(allowed.endsWith(html))
    }

    @Test
    fun linkLikeTagsAndTextAreKept() {
        val html = "<linked>a &lt;link b</linked>"
        assertTrue(sandboxedHtml(html, loadRemote = false, OtusPalette.Otus).endsWith(html))
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
        val blocked = sandboxedHtml(html, loadRemote = false, OtusPalette.Otus, placeholders = true)
        assertContains(blocked, "<a href=\"otus-images:show\" class=\"otus-blocked\" style=\"width:320px;height:80px\"")
        assertContains(blocked, "style=\"width:200px;height:50px\" aria-label")
        assertContains(blocked, "style=\"width:100%;height:120px\"")
        assertFalse("t.example" in blocked)
        assertContains(blocked, "<img src=\"cid:logo\">")
        assertContains(blocked, "<img src=\"data:image/png;base64,AA\">")
        assertContains(blocked, ".otus-blocked{")
        assertTrue(isShowImagesLink("otus-images:show"))
        assertFalse(isOpenableLink("otus-images:show"))
    }

    @Test
    fun imagesStayWithoutPlaceholdersOrOnceAllowed() {
        val html = "<img src=\"https://t.example/a.png\">"
        assertTrue(sandboxedHtml(html, loadRemote = false, OtusPalette.Otus).endsWith(html))
        assertTrue(sandboxedHtml(html, loadRemote = true, OtusPalette.Otus, placeholders = true).endsWith(html))
    }

    @Test
    fun aQuoteFoldsAfterTheBodyWithItsEscapedHeader() {
        val quote = HtmlQuote("Ada <Lovelace>", "AL", "10 Oct", "<p>Earlier <img src=\"https://t.example/q.png\"></p>")
        val page =
            sandboxedHtml("<p>Reply</p>", loadRemote = false, OtusPalette.Otus, placeholders = true, quote = quote)
        val body = page.substringAfter("<p>Reply</p>")
        assertContains(body, "<input type=\"checkbox\" id=\"otus-quote\" class=\"otus-quote-toggle\">")
        assertContains(body, "<span class=\"otus-quote-avatar\">AL</span>Ada &lt;Lovelace&gt;</span> · 10 Oct")
        assertContains(body, "<div class=\"otus-quote-text\"><p>Earlier <a href=\"otus-images:show\"")
        assertContains(body, "Show quoted text")
        assertContains(body, "border-left:2px solid #3f4041")
        assertFalse("<script" in page)
    }
}
