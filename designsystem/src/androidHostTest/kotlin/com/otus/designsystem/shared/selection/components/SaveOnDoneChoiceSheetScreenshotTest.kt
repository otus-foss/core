package com.otus.designsystem.shared.selection.components

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.otus.designsystem.screenshot.screenScreenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SaveOnDoneChoiceSheetScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val details =
        mapOf("Always" to "Every mail", "In your contacts" to "Mail from your contacts", "Never" to "No mail")

    @Test
    fun withDetails() =
        compose.screenScreenshot("radio_choice_sheet") {
            ChoiceSheet(
                "Load images",
                ChoiceOptions(details.keys.toList(), selected = "In your contacts"),
                row = { ChoiceRow(it, details[it]) },
                onSelect = {},
                onDismiss = {},
                saveOnDone = SaveOnDone(),
            )
        }

    @Test
    fun doneSavesThePickedOptionAndNothingBefore() {
        val saved = mutableListOf<String>()
        compose.setContent {
            ChoiceSheet(
                "Theme",
                ChoiceOptions(listOf("Ink", "Slate"), "Ink"),
                { ChoiceRow(it) },
                { saved += it },
                {},
                saveOnDone = SaveOnDone(),
            )
        }
        compose.onNodeWithText("Slate").performClick()
        assertTrue(saved.isEmpty(), "Picking saves nothing: $saved")
        compose.onNodeWithText("Done").performClick()
        compose.waitForIdle()
        assertEquals(listOf("Slate"), saved)
    }

    private data class Theme(
        val name: String,
    )

    @Test
    fun aPickOfAnyOptionTypeSurvivesRestoringState() {
        val saved = mutableListOf<Theme>()
        val themes = listOf(Theme("Ink"), Theme("Slate"))
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            ChoiceSheet(
                "Theme",
                ChoiceOptions(themes, themes[0]),
                { ChoiceRow(it.name) },
                { saved += it },
                {},
                saveOnDone = SaveOnDone(),
            )
        }
        compose.onNodeWithText("Slate").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Done").performClick()
        compose.waitForIdle()
        assertEquals(listOf(Theme("Slate")), saved)
    }
}
