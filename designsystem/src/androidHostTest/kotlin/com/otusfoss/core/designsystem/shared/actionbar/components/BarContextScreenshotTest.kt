package com.otusfoss.core.designsystem.shared.actionbar.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_send
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.people.components.DemoActions.move
import com.otusfoss.core.designsystem.shared.people.components.DemoActions.secondary
import com.otusfoss.core.designsystem.shared.people.components.DemoActions.send
import com.otusfoss.core.designsystem.shared.people.components.DemoPeople
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

/** An [ActionBar] with a [BarContext], one shot per [BarToken] kind. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BarContextScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun person() =
        compose.screenshot("action_bar_context_person") {
            ActionBar(send, context = BarContext("Ada Lovelace", BarToken.Person(DemoPeople.ada)))
        }

    @Test
    fun people() =
        compose.screenshot("action_bar_context_people") {
            ActionBar(
                send,
                secondary = secondary.take(1),
                context = BarContext("Ada and 3 others", BarToken.People(DemoPeople.ada, count = 4)),
            )
        }

    @Test
    fun count() =
        compose.screenshot("action_bar_context_count") {
            ActionBar(
                move,
                secondary = secondary.take(2),
                context = BarContext("messages", BarToken.Count(12), description = "12 messages"),
            )
        }

    @Test
    fun date() =
        compose.screenshot("action_bar_context_date") {
            ActionBar(send, context = BarContext("Tuesday, 9:00", BarToken.Date("Mar", "12")))
        }

    @Test
    fun shellRunsPrimary() {
        var sent = 0
        compose.setContent {
            ActionBar(
                BarAction(UiAction("Send", Res.drawable.symbol_send) { sent++ }),
                context = BarContext("Ada Lovelace", BarToken.Person(DemoPeople.ada)),
            )
        }
        compose.onNodeWithContentDescription("Send, Ada Lovelace").performClick()
        assertEquals(1, sent)
    }
}
