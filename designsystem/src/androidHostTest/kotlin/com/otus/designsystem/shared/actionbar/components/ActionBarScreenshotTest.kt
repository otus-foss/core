package com.otus.designsystem.shared.actionbar.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_delete
import com.otus.designsystem.resources.symbol_send
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.people.components.DemoActions.secondary
import com.otus.designsystem.shared.people.components.DemoActions.write
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActionBarScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun primaryOnly() = compose.screenshot("action_bar_primary") { ActionBar(write) }

    @Test
    fun oneSecondary() =
        compose.screenshot("action_bar_secondary_1") { ActionBar(write, secondary = secondary.take(1)) }

    @Test
    fun twoSecondary() =
        compose.screenshot("action_bar_secondary_2") { ActionBar(write, secondary = secondary.take(2)) }

    @Test
    fun threeSecondary() = compose.screenshot("action_bar_secondary_3") { ActionBar(write, secondary = secondary) }

    @Test
    fun disabledActions() =
        compose.screenshot("action_bar_disabled") {
            ActionBar(
                BarAction(UiAction("Send", Res.drawable.symbol_send, enabled = false) {}),
                secondary = listOf(BarAction(UiAction("Delete", Res.drawable.symbol_delete, enabled = false) {})),
            )
        }

    @Test
    fun minimized() = compose.screenshot("action_bar_minimized") { ActionBar(write, minimized = BarMinimized()) }

    @Test
    fun minimizedHidden() =
        compose.screenshot("action_bar_minimized_hidden") { ActionBar(write, minimized = BarMinimized(hidden = true)) }
}
