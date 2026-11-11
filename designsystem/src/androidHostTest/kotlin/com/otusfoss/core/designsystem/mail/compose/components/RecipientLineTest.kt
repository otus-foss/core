package com.otusfoss.core.designsystem.mail.compose.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import com.otusfoss.core.designsystem.shared.people.components.Recipient
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecipientLineTest {
    @get:Rule
    val compose = createComposeRule()

    private val input = AddressInput("", {}, {}, {})
    private val people =
        listOf("Grace Hopper", "Ada Lovelace", "Alan Turing").map { Recipient(it, ChipAvatar(it.take(1), 1), it) }

    @Test
    fun aLineThatNeverFoldsKeepsItsField() {
        compose.setContent { RecipientLine("To", people, input, {}) }
        compose.onNodeWithContentDescription("To").assertIsDisplayed()
        compose.onNodeWithText("more", substring = true).assertDoesNotExist()
    }

    @Test
    fun aLongLineFoldsPastItsLimit() {
        compose.setContent { RecipientLine("To", people, input, {}, collapsedAfter = 1) }
        compose.onNodeWithText("+2 more").assertIsDisplayed()
        compose.onNodeWithContentDescription("To").assertDoesNotExist()
    }
}
