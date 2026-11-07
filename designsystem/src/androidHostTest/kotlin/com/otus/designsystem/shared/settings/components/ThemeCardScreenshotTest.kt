package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeCardScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val card = Modifier.width(160.dp)

    @Test
    fun selectedAndUnselected() =
        compose.screenshot("theme_card_selection") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeCard("Otus", OtusPalette.Otus, selected = true, {}, card)
                ThemeCard("Light", palette(ThemeBase.Light), selected = false, {}, card)
            }
        }

    @Test
    fun selectedWithActions() =
        compose.screenshot("theme_card_actions") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val actions = listOf(UiAction("Rename") {}, UiAction("Delete") {})
                ThemeCard("My ink", palette(ThemeBase.Ink), selected = true, {}, card, actions)
                ThemeCard("Black", palette(ThemeBase.Black), selected = false, {}, card, actions)
            }
        }

    private fun palette(base: ThemeBase) = ThemeChoice(base = base).palette
}
