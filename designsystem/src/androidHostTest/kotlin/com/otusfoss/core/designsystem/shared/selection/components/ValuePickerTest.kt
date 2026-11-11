package com.otusfoss.core.designsystem.shared.selection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_folder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            .assertHeightIsAtLeast(Sizes.touchTarget)
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

    @Test
    fun pickerFitsItsValueUnlessTheCallerFillsTheWidth() {
        compose.setContent {
            Column(Modifier.width(320.dp)) {
                ValuePicker("Receipts", {})
                ValuePicker("Archive", {}, Modifier.fillMaxWidth())
            }
        }
        val fitted = compose.onNodeWithContentDescription("Receipts, change").getBoundsInRoot()
        assertTrue(fitted.width < 320.dp)
        compose.onNodeWithContentDescription("Archive, change").assertWidthIsEqualTo(320.dp)
    }
}
