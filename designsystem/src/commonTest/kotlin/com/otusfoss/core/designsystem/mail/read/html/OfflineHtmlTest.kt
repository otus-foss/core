package com.otusfoss.core.designsystem.mail.read.html

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineHtmlTest {
    @Test
    fun anOfflineMessageLoadsNothingAndHintsNothing() {
        val sealed =
            offlineHtml(
                "<LINK rel=dns-prefetch href=//tracker.example><link/rel=preconnect href=https://a.example>" +
                    "<p>Hi</p><img src=\"data:image/png;base64,AA\">",
            )
        assertTrue(sealed.startsWith("<meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'none';"))
        assertFalse("http:" in sealed.substringBefore("<otus-inert-link"))
        assertTrue("<meta http-equiv=\"x-dns-prefetch-control\" content=\"off\">" in sealed)
        assertFalse(Regex("<link(?=[\\s/>])", RegexOption.IGNORE_CASE).containsMatchIn(sealed))
        assertTrue(sealed.endsWith("<p>Hi</p><img src=\"data:image/png;base64,AA\">"))
    }

    @Test
    fun anOfflineMessageHasNoFramesOrForms() {
        val frame = "srcdoc=\"&lt;link rel=preconnect href=https://example.org/&gt;\">"
        assertTrue(
            offlineHtml("<IFRAME $frame</IFRAME><form><input>")
                .endsWith("<otus-inert-iframe $frame</otus-inert-iframe><otus-inert-form><input disabled>"),
        )
    }

    @Test
    fun otherTagsStartingWithLinkStay() = assertTrue(offlineHtml("<linker>").endsWith("<linker>"))
}
