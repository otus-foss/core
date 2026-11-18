package com.otusfoss.core.designsystem.mail.read.elements

import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.OtusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowWebView

/** How often [HtmlView] loads its page, and how often [HtmlViewWarmUp] starts the web engine. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi", shadows = [RecordingWebView::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HtmlViewLoadsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun reset() {
        RecordingWebView.loads.clear()
        RecordingWebView.destroyed = 0
    }

    /** Lets the clock and real time pass, since a page is built off the main thread. */
    private fun settle() {
        repeat(SETTLE_ROUNDS) {
            compose.mainClock.advanceTimeBy(ROUND_MILLIS)
            compose.waitForIdle()
            Thread.sleep(ROUND_SLEEP_MILLIS)
        }
        compose.waitForIdle()
    }

    @Test
    fun insetsMeasuredFrameByFrameLoadThePageOnce() {
        compose.setContent {
            OtusTheme {
                var top by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    repeat(MEASURED_FRAMES) {
                        withFrameNanos {}
                        top += STEP
                    }
                }
                HtmlView(
                    "<p>Hello</p>",
                    loadRemote = false,
                    {},
                    Modifier.fillMaxSize(),
                    insets = PaddingValues(top = top.dp),
                )
            }
        }
        compose.waitUntil(WAIT_MILLIS) { RecordingWebView.loads.isNotEmpty() }
        settle()

        assertEquals("Loads of the page", 1, RecordingWebView.loads.size)
        val room = "height:${MEASURED_FRAMES * STEP}px"
        assertTrue("The page leaves the room that stayed", room in RecordingWebView.loads.single())
    }

    @Test
    fun theWarmUpStartsTheEngineOnceHoweverOftenItIsComposed() {
        compose.setContent {
            HtmlViewWarmUp()
            HtmlViewWarmUp()
        }
        settle()

        assertEquals("Web views made and let go", 1, RecordingWebView.destroyed)
        assertEquals("The warm-up loads no page", emptyList<String>(), RecordingWebView.loads)
    }

    private companion object {
        const val WAIT_MILLIS = 5_000L
        const val MEASURED_FRAMES = 3
        const val STEP = 10
        const val SETTLE_ROUNDS = 12
        const val ROUND_MILLIS = 100L
        const val ROUND_SLEEP_MILLIS = 25L
    }
}

/** A web view that keeps every document it was told to load and counts the views destroyed. */
@Implements(WebView::class)
class RecordingWebView : ShadowWebView() {
    @Implementation
    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        loads += data.orEmpty()
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    @Implementation
    override fun destroy() {
        destroyed++
        super.destroy()
    }

    companion object {
        val loads = mutableListOf<String>()
        var destroyed = 0
    }
}
