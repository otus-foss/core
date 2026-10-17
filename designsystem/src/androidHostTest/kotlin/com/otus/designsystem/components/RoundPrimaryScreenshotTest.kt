package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_drive_file_move
import com.otus.designsystem.resources.symbol_send
import com.otus.designsystem.screenshot.screenshot
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

    private val send = BarAction("Send", Res.drawable.symbol_send) {}

    @Test
    fun states() =
        compose.screenshot("round_primary_states") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                RoundPrimary(send, context = null)
                RoundPrimary(
                    BarAction("Move", Res.drawable.symbol_drive_file_move) {},
                    BarContext("messages", BarToken.Count(3), "3 messages"),
                )
                RoundPrimary(BarAction("Send", Res.drawable.symbol_send, enabled = false) {}, context = null)
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
