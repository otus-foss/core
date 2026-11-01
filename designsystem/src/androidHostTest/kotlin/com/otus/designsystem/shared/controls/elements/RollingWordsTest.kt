package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertTrue

/** Rolling words keep the text's own word order under a right-to-left layout. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RollingWordsTest {
    @get:Rule
    val compose = createComposeRule()

    private fun showRightToLeft(text: String) {
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { RollingWords(text) }
        }
        compose.waitForIdle()
    }

    private fun left(word: String) = compose.onNodeWithText(word).getBoundsInRoot().left

    @Test
    fun englishReadsLeftToRightUnderARightToLeftLayout() {
        showRightToLeft("New mail")
        assertTrue(left("New") < left("mail"))
    }

    @Test
    fun hebrewReadsRightToLeft() {
        showRightToLeft("דואר חדש")
        assertTrue(left("דואר") > left("חדש"))
    }

    @Test
    fun aNumberKeepsItsDigitsInOrderInRightToLeftText() {
        showRightToLeft("הצג 12")
        assertTrue(left("1") < left("2"))
        assertTrue(left("הצג") > left("1"))
    }
}
