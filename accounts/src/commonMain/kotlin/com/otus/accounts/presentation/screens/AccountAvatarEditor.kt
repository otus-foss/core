package com.otus.accounts.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.otus.accounts.domain.ConnectedAccount
import com.otus.accounts.presentation.OwnAvatarsState
import com.otus.accounts.presentation.components.OwnAvatarSheet
import com.otus.accounts.presentation.ownAddresses
import com.otus.accounts.presentation.toChipAvatar
import com.otus.avatar.ContactAvatar
import com.otus.avatar.generatedAvatar
import com.otus.designsystem.components.ActionDialog
import com.otus.designsystem.components.DialogAction
import com.otus.designsystem.components.FeaturedAvatar
import com.otus.designsystem.components.FeaturedAvatars
import com.otus.designsystem.components.PersonChip
import com.otus.designsystem.components.Recipient
import com.otus.designsystem.theme.PanelTokens

/**
 * Edits one avatar for every address of [account]. When some addresses already have an avatar of
 * their own, asks whether the new one replaces theirs or only fills the addresses without one.
 */
@Composable
internal fun AccountAvatarEditor(
    avatars: OwnAvatarsState,
    account: ConnectedAccount,
    onDismiss: () -> Unit,
) {
    var asking by remember { mutableStateOf<AccountAvatarChoice?>(null) }
    val emails = account.ownAddresses
    val choice = asking
    if (choice == null) {
        OwnAvatarSheet(
            "All addresses of ${account.name}",
            emails.first().substringAfterLast('@'),
            avatars.shared(account),
            pickPhoto = if (avatars.canPickPhoto) avatars::pickPhoto else null,
            onSave = { avatar ->
                val replaced = avatars.replaced(account, avatar)
                if (replaced.isEmpty()) {
                    avatars.saveAll(account, emails, avatar)
                } else {
                    asking = AccountAvatarChoice(avatar, replaced)
                }
            },
            // A save that asks first keeps the editor until the user answers.
            onDismiss = { if (asking == null) onDismiss() },
        )
    } else {
        val one = choice.replaced.size == 1
        ActionDialog(
            title = if (one) "Replace 1 avatar?" else "Replace ${choice.replaced.size} avatars?",
            actions =
                listOf(
                    DialogAction("Replace all", { avatars.saveAll(account, emails, choice.avatar) }, primary = true),
                    DialogAction(
                        "Keep theirs",
                        { avatars.saveAll(account, emails - choice.replaced.toSet(), choice.avatar) },
                    ),
                ),
            onDismiss = onDismiss,
            message = if (one) "This address already has an avatar:" else "These addresses already have one:",
        ) { CurrentAvatars(avatars, account, choice.replaced) }
    }
}

/** [emails] with the avatars they have now: large faces for a few, else pills. */
@Composable
private fun CurrentAvatars(
    avatars: OwnAvatarsState,
    account: ConnectedAccount,
    emails: List<String>,
) {
    val chip = { email: String ->
        avatars.avatar(account, email)?.toChipAvatar() ?: generatedAvatar(account.name, email).toChipAvatar()
    }
    if (emails.size < FACES) {
        FeaturedAvatars(emails.map { FeaturedAvatar(it, chip(it)) }, labelLines = 2)
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
            verticalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        ) { emails.forEach { PersonChip(Recipient(it, chip(it), it)) } }
    }
}

/** Below this many addresses, the dialog shows them as large faces. */
private const val FACES = 5

/** A new account avatar waiting for the user to say whether it replaces [replaced]' own. */
private class AccountAvatarChoice(
    val avatar: ContactAvatar?,
    val replaced: List<String>,
)
