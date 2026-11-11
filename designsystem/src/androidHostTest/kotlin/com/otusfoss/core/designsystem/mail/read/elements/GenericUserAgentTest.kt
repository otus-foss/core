package com.otusfoss.core.designsystem.mail.read.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class GenericUserAgentTest {
    @Test
    fun remoteImagesLearnNeitherTheDeviceNorTheWebViewBuild() =
        assertEquals(
            "Mozilla/5.0 (Linux; Android 10; K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                "Chrome/127.0.0.0 Mobile Safari/537.36",
            genericUserAgent(
                "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/AP2A.240805.005; wv) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Version/4.0 Chrome/127.0.6533.103 Mobile Safari/537.36",
            ),
        )
}
