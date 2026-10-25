package com.otus.designsystem.elements

import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class SandboxedWebViewTest {
    @Test
    fun remoteImagesSkipTheSharedCacheAndGetNoCookies() {
        val view =
            sandboxedWebView(
                ApplicationProvider.getApplicationContext(),
                loadRemote = true,
                darken = false,
                links = LinkClient(newRenderKey(), onOpenLink = {}, onShowRemote = {}),
            )
        assertEquals(WebSettings.LOAD_NO_CACHE, view.settings.cacheMode)
        assertFalse(CookieManager.getInstance().acceptThirdPartyCookies(view))
    }
}
