package com.otus.designsystem.components

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.theme.PanelTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ValuePickerTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun tapCallsOnClick() {
        var clicks = 0
        compose.setContent {
            ValuePicker(
                "Receipts",
                { clicks++ },
                keyword = "Move to",
                icon = Res.drawable.symbol_folder,
            )
        }
        compose.onNodeWithContentDescription("Move to Receipts, change").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun screenReadersHearOneButton() {
        compose.setContent { ValuePicker("Receipts", {}, keyword = "Move to") }
        compose
            .onNodeWithContentDescription("Move to Receipts, change")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHasClickAction()
            .assertIsEnabled()
            .assertHeightIsAtLeast(PanelTokens.touchTarget)
    }

    @Test
    fun disabledPickerIgnoresTaps() {
        var clicks = 0
        compose.setContent { ValuePicker("Receipts", { clicks++ }, enabled = false) }
        compose
            .onNodeWithContentDescription("Receipts, change")
            .assertIsNotEnabled()
            .performClick()
        assertEquals(0, clicks)
    }
}
