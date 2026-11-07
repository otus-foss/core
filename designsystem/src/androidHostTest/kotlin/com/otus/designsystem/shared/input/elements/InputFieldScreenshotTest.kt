package com.otus.designsystem.shared.input.elements

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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
class InputFieldScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun placeholder() =
        compose.screenshot("input_field_placeholder") {
            InputField("Server", "", {}, WIDTH, placeholder = "mail.example.com", kind = InputKind.Url)
        }

    @Test
    fun filled() = compose.screenshot("input_field_filled") { InputField("Name", "Ada Lovelace", {}, WIDTH) }

    @Test
    fun supportingText() =
        compose.screenshot("input_field_supporting_text") {
            InputField(
                "Email",
                "ada@example.com",
                {},
                WIDTH,
                note = InputNote("Replies go to this address"),
                kind = InputKind.Email,
            )
        }

    @Test
    fun password() =
        compose.screenshot("input_field_password") {
            InputField("App password", "correct-horse", {}, WIDTH, kind = InputKind.Password)
        }

    @Test
    fun longValue() =
        compose.screenshot("input_field_long_value") {
            InputField("Server", "https://jmap.mail.analytical-engine.example.com/.well-known/jmap", {}, WIDTH)
        }

    @Test
    fun valid() =
        compose.screenshot("input_field_valid") {
            InputField(
                "Server",
                "mail.example.org",
                {},
                WIDTH,
                note = InputNote("Connects to https://mail.example.org/.well-known/jmap", valid = true),
                kind = InputKind.Url,
            )
        }

    @Test
    fun invalid() =
        compose.screenshot("input_field_invalid") {
            InputField(
                "Server",
                "asdf",
                {},
                WIDTH,
                note = InputNote("This doesn't look like a server address.", valid = false),
                kind = InputKind.Url,
            )
        }

    private companion object {
        val WIDTH = Modifier.width(320.dp)
    }
}
