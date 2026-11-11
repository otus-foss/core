package com.otus.designsystem.mail.read.html

import kotlin.test.Test
import kotlin.test.assertEquals

class TextHtmlTest {
    @Test
    fun markdownAndBareLinksBecomeLinks() {
        assertEquals(
            "Sent from <a href=\"https://proton.me/mail/home\">Proton Mail</a> for Android.<br>" +
                "See <a href=\"https://x.example/a?b=1&amp;c=2\">https://x.example/a?b=1&amp;c=2</a>.",
            textHtml(
                "Sent from [Proton Mail](https://proton.me/mail/home) for Android.\nSee https://x.example/a?b=1&c=2.",
            ),
        )
    }

    @Test
    fun textAroundLinksIsEscaped() {
        assertEquals(
            "&lt;<a href=\"https://x.example\">https://x.example</a>&gt; &amp; [not a link](javascript:alert)",
            textHtml("<https://x.example> & [not a link](javascript:alert)"),
        )
    }
}
