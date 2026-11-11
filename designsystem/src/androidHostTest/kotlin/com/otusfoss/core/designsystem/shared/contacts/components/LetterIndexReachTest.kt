package com.otusfoss.core.designsystem.shared.contacts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.shared.overlays.components.WithBackDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Letters past the screen stay reachable, and the press zone follows the layout direction. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LetterIndexReachTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aScreenReaderCanScrollToLettersPastTheScreen() {
        val letters = ('A'..'Z').map { IndexLetter(it.toString(), count = 1) }
        show(letters, LayoutDirection.Ltr)
        compose.onNodeWithText("A").performSemanticsAction(SemanticsActions.OnClick)
        compose.waitForIdle()
        val root = compose.onRoot().getBoundsInRoot()
        assertTrue(compose.onNode(hasClickLabel("Jump to Z")).getBoundsInRoot().top > root.bottom)

        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy)).performSemanticsAction(
            SemanticsActions.ScrollBy,
        ) { scroll -> scroll(0f, FAR) }
        compose.waitForIdle()

        assertTrue(compose.onNode(hasClickLabel("Jump to Z")).getBoundsInRoot().bottom <= root.bottom + 1.dp)
    }

    @Test
    fun inRightToLeftAPressNearTheRightEdgeOpensTheIndex() {
        show(SHORT, LayoutDirection.Rtl)

        pressAt(fromLeft = false)

        assertTrue(indexShown())
    }

    @Test
    fun inRightToLeftAShortPressNearTheLeftEdgeDoesNotOpenTheIndex() {
        show(SHORT, LayoutDirection.Rtl)

        pressAt(fromLeft = true)

        assertFalse(indexShown())
    }

    @Test
    fun inLeftToRightAPressNearTheLeftEdgeOpensTheIndex() {
        show(SHORT, LayoutDirection.Ltr)

        pressAt(fromLeft = true)

        assertTrue(indexShown())
    }

    @Test
    fun withLettersOnTheEndAPressNearTheRightEdgeOpensTheIndex() {
        show(SHORT, LayoutDirection.Ltr, LetterIndexSide.End)

        pressAt(fromLeft = false)

        assertTrue(indexShown())
    }

    @Test
    fun withLettersOnTheEndAShortPressNearTheLeftEdgeDoesNotOpenTheIndex() {
        show(SHORT, LayoutDirection.Ltr, LetterIndexSide.End)

        pressAt(fromLeft = true)

        assertFalse(indexShown())
    }

    private fun show(
        letters: List<IndexLetter>,
        direction: LayoutDirection,
        side: LetterIndexSide = LetterIndexSide.Start,
    ) {
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                WithBackDispatcher {
                    val state = rememberLetterIndexState()
                    LetterIndex(state, side, {}, Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize()) {
                            Text("A", Modifier.fillMaxWidth().letterIndexHandle(state, "A", letters) {})
                        }
                    }
                }
            }
        }
    }

    private fun pressAt(fromLeft: Boolean) {
        compose.onNodeWithText("A").performTouchInput {
            val inset = EDGE_INSET.dp.toPx()
            click(Offset(if (fromLeft) inset else width - inset, height / 2f))
        }
        compose.waitForIdle()
    }

    private fun indexShown(): Boolean =
        compose.onAllNodes(hasClickLabel("Jump to B")).fetchSemanticsNodes().isNotEmpty()

    private fun hasClickLabel(label: String): SemanticsMatcher =
        SemanticsMatcher("has click label $label") { it.config.getOrNull(SemanticsActions.OnClick)?.label == label }

    private companion object {
        val SHORT = listOf(IndexLetter("A", count = 1), IndexLetter("B", count = 1))
        const val FAR = 100_000f
        const val EDGE_INSET = 10
    }
}
