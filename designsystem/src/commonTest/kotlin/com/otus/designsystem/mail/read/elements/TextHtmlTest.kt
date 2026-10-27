package com.otus.designsystem.mail.read.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class TextHtmlTest {
    @Test
    fun markdownAndBareLinksBecomeLinks() {
        assertEquals(
            "Sent from <a href=\"https://proton.me/mail/home\">Proton Mail</a> for Android.<br>" +
                "See <a href=\"https://x.org/a?b=1&amp;c=2\">https://x.org/a?b=1&amp;c=2</a>.",
            textHtml("Sent from [Proton Mail](https://proton.me/mail/home) for Android.\nSee https://x.org/a?b=1&c=2."),
        )
    }

    @Test
    fun textAroundLinksIsEscaped() {
        assertEquals(
            "&lt;<a href=\"https://x.org\">https://x.org</a>&gt; &amp; [not a link](javascript:alert)",
            textHtml("<https://x.org> & [not a link](javascript:alert)"),
        )
    }
}
