package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

/** A folding section takes a list's scroll to fold, gives it back to unfold, and toggles from its chevron. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SectionFoldTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var fold: SectionFold

    private fun show() {
        compose.setContent {
            // One pixel per dp, so the content's 100 dp fold in 100 pixels.
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                fold = rememberSectionFold()
                FoldingSection("People", fold) { Box(Modifier.fillMaxWidth().height(FULL.dp)) }
            }
        }
        compose.waitForIdle()
    }

    private fun scroll(by: Float): Float =
        compose.runOnIdle {
            val connection = fold.connection
            val source = NestedScrollSource.UserInput
            val offered = Offset(0f, by)
            if (by < 0f) {
                connection.onPreScroll(offered, source).y
            } else {
                connection.onPostScroll(Offset.Zero, offered, source).y
            }
        }

    @Test
    fun scrollingOnFoldsTheSectionAndPullingBackUnfoldsIt() {
        show()
        assertEquals(-40f, scroll(-40f))
        assertEquals(0.4f, fold.progress)
        // Past the fold, the rest of the scroll is left for the list.
        assertEquals(-60f, scroll(-90f))
        assertEquals(1f, fold.progress)
        assertEquals(0f, abs(scroll(-10f)))
        assertEquals(30f, scroll(30f))
        assertEquals(0.7f, fold.progress)
        assertEquals(70f, scroll(200f))
        assertEquals(0f, fold.progress)
    }

    @Test
    fun theChevronFoldsAMostlyShownSectionAndUnfoldsAMostlyFoldedOne() {
        show()
        scroll(-30f)
        compose.onNodeWithContentDescription("Hide People").performClick()
        compose.waitForIdle()
        assertEquals(1f, fold.progress)
        compose.onNodeWithContentDescription("Show People").performClick()
        compose.waitForIdle()
        assertEquals(0f, fold.progress)
    }

    private companion object {
        const val FULL = 100
    }
}
