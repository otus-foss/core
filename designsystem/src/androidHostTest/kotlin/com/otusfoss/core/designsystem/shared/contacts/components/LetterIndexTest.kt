package com.otusfoss.core.designsystem.shared.contacts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.shared.overlays.components.WithBackDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LetterIndexTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aScreenReaderCanOpenTheIndexFromAHeadingAndJump() {
        var picked: String? = null
        val letters = listOf(IndexLetter("A", 2), IndexLetter("B", 1))
        compose.setContent {
            WithBackDispatcher {
                val state = rememberLetterIndexState()
                LetterIndex(state, LetterIndexSide.Start, {}) {
                    Text("A", Modifier.letterIndexHandle(state, "A", letters) { picked = it })
                }
            }
        }

        compose.onNodeWithText("A").performSemanticsAction(SemanticsActions.OnClick)
        compose.onNodeWithText("B").performClick()

        assertEquals("B", picked)
    }

    @Test
    fun thePressedLetterStaysLevelWithItsHeading() {
        assertLevelAfterOpening(letter = "B", bottomGap = 120.dp)
    }

    @Test
    fun aLetterAtTheBottomStaysLevelWithItsHeading() {
        assertLevelAfterOpening(letter = "C", bottomGap = 0.dp)
    }

    private fun assertLevelAfterOpening(
        letter: String,
        bottomGap: Dp,
    ) {
        openFrom(letter, bottomGap)

        val heading = compose.onNode(hasClickLabel("Show letters") and hasText(letter)).getBoundsInRoot()
        val row = compose.onNode(hasClickLabel("Jump to $letter")).getBoundsInRoot()

        assertNear((heading.top + heading.bottom) / 2, (row.top + row.bottom) / 2)
    }

    private fun openFrom(
        letter: String,
        bottomGap: Dp,
    ) {
        val letters = listOf(IndexLetter("A", 2), IndexLetter("B", 1), IndexLetter("C", 3))
        compose.setContent {
            WithBackDispatcher {
                val state = rememberLetterIndexState()
                LetterIndex(state, LetterIndexSide.Start, {}, Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize()) {
                        val heading = Modifier.letterIndexHandle(state, letter, letters) {}
                        Text(letter, Modifier.align(Alignment.BottomStart).padding(bottom = bottomGap).then(heading))
                    }
                }
            }
        }
        compose.onNodeWithText(letter).performSemanticsAction(SemanticsActions.OnClick)
        compose.waitForIdle()
    }

    private fun assertNear(
        expected: Dp,
        actual: Dp,
    ) {
        assertTrue((expected - actual).value in -1f..1f, "Expected $actual to be within 1 dp of $expected")
    }

    private fun hasClickLabel(label: String): SemanticsMatcher =
        SemanticsMatcher("has click label $label") { it.config.getOrNull(SemanticsActions.OnClick)?.label == label }
}
