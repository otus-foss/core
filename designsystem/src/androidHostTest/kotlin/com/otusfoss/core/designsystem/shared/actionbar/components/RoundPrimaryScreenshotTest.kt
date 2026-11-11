package com.otusfoss.core.designsystem.shared.actionbar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_send
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.people.components.DemoActions.move
import com.otusfoss.core.designsystem.shared.people.components.DemoActions.send
import com.otusfoss.core.designsystem.shared.people.components.DemoPeople
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RoundPrimaryScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun states() =
        compose.screenshot("round_primary_states") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                RoundPrimary(send, context = null)
                RoundPrimary(move, BarContext("messages", BarToken.Count(3), "3 messages"))
                RoundPrimary(BarAction(UiAction("Send", Res.drawable.symbol_send, enabled = false) {}), context = null)
            }
        }

    @Test
    fun minimizedBar() =
        compose.screenshot("minimized_bar_visible") {
            Column(Modifier.width(344.dp)) {
                MinimizedBar(
                    send,
                    BarContext("Mira Chen", BarToken.Person(DemoPeople.mira.avatar)),
                    hidden = false,
                    more = emptyList(),
                )
            }
        }
}
