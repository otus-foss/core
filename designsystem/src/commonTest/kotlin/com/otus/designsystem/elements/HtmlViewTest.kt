package com.otus.designsystem.elements

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlViewTest {
    @Test
    fun policyComesBeforeTheDocument() {
        val html = "<img src=\"https://tracker.example/p.gif\">"
        val sandboxed = sandboxedHtml(html, loadRemote = false)
        assertTrue(sandboxed.startsWith("<meta http-equiv=\"Content-Security-Policy\""))
        assertTrue(sandboxed.endsWith(html))
    }

    @Test
    fun blocksRemoteResourcesUntilAllowed() {
        val blocked = policyOf(sandboxedHtml("", loadRemote = false))
        assertEquals(
            "default-src 'none'; img-src data:; style-src 'unsafe-inline'; font-src data:; form-action 'none'",
            blocked,
        )
        val allowed = policyOf(sandboxedHtml("", loadRemote = true))
        assertContains(allowed, "img-src data: http: https:")
        assertContains(allowed, "default-src 'none'")
        assertFalse("script-src" in allowed)
    }

    @Test
    fun connectionHintsStayInertUntilRemoteContentIsAllowed() {
        val html = "<LINK rel=preconnect href=https://u1.tracker.example><link\trel=\"dns-prefetch\" href=x>"
        val blocked = sandboxedHtml(html, loadRemote = false)
        assertContains(blocked, "<meta http-equiv=\"x-dns-prefetch-control\" content=\"off\">")
        assertFalse("<link" in blocked.lowercase())
        assertContains(blocked, "<otus-inert-link rel=preconnect")
        val allowed = sandboxedHtml(html, loadRemote = true)
        assertFalse("x-dns-prefetch-control" in allowed)
        assertTrue(allowed.endsWith(html))
    }

    @Test
    fun linkLikeTagsAndTextAreKept() {
        val html = "<linked>a &lt;link b</linked>"
        assertTrue(sandboxedHtml(html, loadRemote = false).endsWith(html))
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
}
