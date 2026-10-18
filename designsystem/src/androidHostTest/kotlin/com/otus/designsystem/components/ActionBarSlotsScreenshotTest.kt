package com.otus.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.otus.designsystem.components.DemoActions.secondary
import com.otus.designsystem.components.DemoActions.write
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.screenshot.screenScreenshot
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

/** An action bar's empty add slot, and a secondary action that opens a menu. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActionBarSlotsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun addSlot() = compose.screenshot("action_bar_add_slot") { AddSlotBar() }

    @Test
    fun addSlotLight() =
        compose.screenshot("action_bar_add_slot_light", palette = ThemeChoice(ThemeBase.Light).palette) {
            AddSlotBar()
        }

    @Test
    fun addSlotBlack() =
        compose.screenshot("action_bar_add_slot_black", palette = ThemeChoice(ThemeBase.Black).palette) {
            AddSlotBar()
        }

    @Test
    fun addSlotOnly() = compose.screenshot("action_bar_add_slot_only") { ActionBar(write, onAdd = {}) }

    @Test
    fun menuOpensAboveItsButton() =
        compose.screenScreenshot("action_bar_menu", act = {
            onNodeWithContentDescription("All actions").performClick()
        }) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                ActionBar(write, secondary = listOf(secondary.first(), menuAction()))
            }
        }

    @Test
    fun choosingFromTheMenuRunsTheChoiceOnly() {
        val calls = mutableListOf<String>()
        compose.setContent { ActionBar(write, secondary = listOf(menuAction(calls))) }
        compose.onNodeWithContentDescription("All actions").performClick()
        compose.onNodeWithText("Flag").performClick()
        assertEquals(listOf("flag"), calls)
    }

    @Composable
    private fun AddSlotBar() = ActionBar(write, secondary = secondary.take(2), onAdd = {})

    private fun menuAction(calls: MutableList<String> = mutableListOf()) =
        BarAction(
            "All actions",
            Res.drawable.symbol_more_horiz,
            menu = listOf("Archive", "Flag", "Move", "Delete").map { MenuAction(it) { calls += it.lowercase() } },
        ) { calls += "all" }
}
