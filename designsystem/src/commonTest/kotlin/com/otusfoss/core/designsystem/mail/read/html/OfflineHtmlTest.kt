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
    fun otherTagsStartingWithLinkStay() = assertTrue(offlineHtml("<linker>").endsWith("<linker>"))
}
