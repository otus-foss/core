package com.otus.settings.presentation.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.otus.designsystem.theme.OtusTheme
import com.otus.designsystem.theme.colors.CustomTheme
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.colors.ThemeColor
import com.otus.settings.presentation.ThemeSettingsState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeSettingsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val state = ThemeSettingsState()

    @Test
    fun doneAppliesTheColorAndClosesTheSheet() {
        openPageColor()
        compose.onNode(hasSetTextAction()).performTextReplacement("#101828")
        clickButton("Done")
        compose.onNodeWithContentDescription("Hue").assertDoesNotExist()
        assertEquals(Color(0xFF101828), state.choice.customColors[ThemeColor.Page])
        compose.onNodeWithText("Made from Otus").assertExists()
    }

    @Test
    fun resetPutsBackTheColorAndKeepsTheCustomTheme() {
        state.select(ThemeChoice().withColor(ThemeColor.Page, Color(0xFF101828)))
        openPageColor(listShown = true)
        compose.onNodeWithText("Reset").performClick()
        compose.onNodeWithContentDescription("Hue").assertDoesNotExist()
        assertEquals(listOf(CustomTheme(1, ThemeBase.Otus)), state.choice.customThemes)
        assertEquals(1, state.choice.customId)
    }

    @Test
    fun deletingTheCustomThemeGoesBackToTheThemeItWasMadeFrom() {
        state.select(ThemeChoice(ThemeBase.Ink).withColor(ThemeColor.Page, Color(0xFF101828)))
        showScreen()
        openCustomActions()
        compose.onNodeWithText("Delete").performClick()
        clickButton("Delete")
        assertEquals(ThemeChoice(ThemeBase.Ink), state.choice)
    }

    @Test
    fun renamingACustomThemeShowsItsNewName() {
        state.select(ThemeChoice().withColor(ThemeColor.Page, Color(0xFF101828)))
        showScreen()
        openCustomActions()
        compose.onNodeWithText("Rename").performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("Night")
        clickButton("Rename")
        // The sheet slides away before it applies the name.
        compose.waitUntil { state.choice.customTheme?.name == "Night" }
        compose.onNodeWithText("Night").assertExists()
    }

    @Test
    fun aLongPressDuplicatesABuiltInTheme() {
        showScreen()
        compose.onNodeWithText("Black").performScrollTo().performTouchInput { longClick() }
        compose.onNodeWithText("Duplicate").performClick()
        assertEquals(CustomTheme(1, ThemeBase.Black, name = "Black copy"), state.choice.customTheme)
        compose.onNodeWithText("Black copy").assertExists()
    }

    @Test
    fun doneClosesTheCustomAccentSheet() {
        showScreen()
        compose.onNodeWithContentDescription("Custom color").performScrollTo().performClick()
        clickButton("Done")
        compose.onNodeWithContentDescription("Hue").assertDoesNotExist()
    }

    /** Opens the page color; the list of colors starts unfolded when the theme has changed colors. */
    private fun openPageColor(listShown: Boolean = false) {
        showScreen()
        if (!listShown) compose.onNodeWithText("Customize colors").performScrollTo().performClick()
        compose.onNodeWithText("Page").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Hue").assertExists()
    }

    private fun showScreen() {
        val back =
            object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = NavigationEventDispatcher()
            }
        compose.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides back) {
                OtusTheme(state.palette) { ThemeSettingsScreen(state, onBack = {}) {} }
            }
        }
    }

    private fun clickButton(label: String) {
        compose.onNode(hasText(label) and hasClickAction()).performClick()
    }

    private fun openCustomActions() {
        compose.onNodeWithContentDescription("Actions for Custom").performScrollTo().performClick()
    }
}
