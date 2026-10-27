package com.otus.designsystem.shared.contacts.components

import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.otus.designsystem.shared.overlays.components.WithBackDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

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
                LetterIndex(state) {
                    Text("A", Modifier.letterIndexHandle(state, "A", letters) { picked = it })
                }
            }
        }

        compose.onNodeWithText("A").performSemanticsAction(SemanticsActions.OnClick)
        compose.onNodeWithText("B").performClick()

        assertEquals("B", picked)
    }
}
