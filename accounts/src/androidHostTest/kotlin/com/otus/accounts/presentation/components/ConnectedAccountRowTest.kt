package com.otus.accounts.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import com.otus.accounts.domain.ConnectedAccount
import com.otus.designsystem.shared.people.components.ChipAvatar
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ConnectedAccountRowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun anAccountWithoutAddressesHasNoAvatarButton() {
        var edits = 0
        compose.setContent { AccountRow(ConnectedAccount("jdoe", "Jdoe", "jdoe", "mail.example.org")) { edits++ } }
        compose.onAllNodes(hasClickAction()).assertCountEquals(1)
        assertEquals(0, edits)
    }

    @Test
    fun theAvatarOfAnAccountWithSeveralAddressesEditsThemAll() {
        var edits = 0
        val account =
            ConnectedAccount("ada", "Ada", "ada@example.org", "mail.example.org", listOf("work@example.org"))
        compose.setContent { AccountRow(account) { edits++ } }
        compose.onNode(hasClickLabel("Change the avatar of every address")).performClick()
        assertEquals(1, edits)
    }

    @Composable
    private fun AccountRow(
        account: ConnectedAccount,
        onEditAll: () -> Unit,
    ) = ConnectedAccountRow(account, { ChipAvatar("A", 1) }, ChipAvatar("A", 1), {}, {}, onEditAll)

    private fun hasClickLabel(label: String) =
        SemanticsMatcher("click label $label") { it.config.getOrNull(SemanticsActions.OnClick)?.label == label }
}
