package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ComposeFieldScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun empty() = compose.screenshot("compose_field_empty") { ComposeField("To", "", {}, WIDTH) }

    @Test
    fun filled() =
        compose.screenshot("compose_field_filled") {
            ComposeField("Subject", "Receipts for October", {}, WIDTH)
        }

    @Test
    fun trailing() =
        compose.screenshot("compose_field_trailing") {
            ComposeField("To", "ada@example.com", {
            }, WIDTH, InputKind.Email, trailing = { Symbol(Res.drawable.symbol_expand_more) })
        }

    @Test
    fun valueFields() =
        compose.screenshot("compose_value_field") {
            Column(WIDTH) {
                ComposeValueField("From", "ada@example.com", {})
                ComposeValueField("From", "ada@example.com", null)
                ComposeValueField("From", "Ada Lovelace <ada.lovelace@analytical-engine.example.com>", {})
            }
        }

    @Test
    fun valueFieldClick() {
        var clicks = 0
        compose.setContent { ComposeValueField("From", "ada@example.com", { clicks++ }) }
        compose.onNodeWithContentDescription("From, ada@example.com").performClick()
        assertEquals(1, clicks)
    }

    private companion object {
        val WIDTH = Modifier.width(320.dp)
    }
}
