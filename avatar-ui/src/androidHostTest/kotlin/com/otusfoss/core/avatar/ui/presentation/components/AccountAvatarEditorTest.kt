package com.otusfoss.core.avatar.ui.presentation.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.otusfoss.core.avatar.ui.presentation.AccountAvatarEdit
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AccountAvatarEditorTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theEditorShowsNothingForAnAccountWithoutAddresses() {
        val edit =
            AccountAvatarEdit("Jdoe", emptyList(), null, null, { emptyList() }, { ChipAvatar("J", 1) }) { _, _ -> }
        compose.setContent { AccountAvatarEditor(edit) {} }
        compose.onNodeWithText("All addresses of Jdoe").assertDoesNotExist()
    }
}
