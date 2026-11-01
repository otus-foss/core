package com.otus.designsystem.shared.input.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class InputFieldAutofillTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun fieldsTellAutofillWhatTheyHold() {
        compose.setContent {
            Column {
                InputField("Username", "", {}, kind = InputKind.Username)
                InputField("Password", "", {}, kind = InputKind.Password)
                InputField("Email", "", {}, kind = InputKind.Email)
            }
        }
        assertEquals(ContentType.Username, contentTypeOf("Username"))
        assertEquals(ContentType.Password, contentTypeOf("Password"))
        // Compose offers an email-keyboard field as an email address on its own; it is not a login field.
        assertEquals(ContentType.EmailAddress, contentTypeOf("Email"))
    }

    private fun contentTypeOf(description: String): ContentType? =
        compose
            .onNodeWithContentDescription(description)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.ContentType)
}
