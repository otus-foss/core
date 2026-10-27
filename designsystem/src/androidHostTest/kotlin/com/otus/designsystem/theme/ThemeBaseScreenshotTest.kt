package com.otus.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.controls.elements.Chip
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

/** One image per built-in theme, so a palette change shows up as a changed image. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeBaseScreenshotTest(
    private val base: ThemeBase,
) {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun textAndChips() {
        val palette = ThemeChoice(base).palette
        compose.screenshot("theme_${base.name.lowercase()}", palette = palette) {
            Column(Modifier.background(palette.bgRegular).padding(8.dp)) {
                Text(base.name, style = panelTextStyle())
                Text("Secondary", style = panelTextStyle().copy(color = palette.text2))
                Text("Muted", style = panelTextStyle().copy(color = palette.text3))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Inbox", {})
                    Chip("Work", {}, selected = true)
                    Chip("Spam", {}, enabled = false)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun bases(): List<Array<Any>> = ThemeBase.entries.map { arrayOf(it) }
    }
}
