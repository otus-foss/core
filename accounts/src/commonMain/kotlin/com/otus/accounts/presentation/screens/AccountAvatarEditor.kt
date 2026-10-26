package com.otus.accounts.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.otus.accounts.domain.ConnectedAccount
import com.otus.accounts.presentation.OwnAvatarsState
import com.otus.accounts.presentation.components.AvatarSheet
import com.otus.accounts.presentation.ownAddresses
import com.otus.accounts.presentation.toChipAvatar
import com.otus.avatar.ContactAvatar
import com.otus.avatar.generatedAvatar
import com.otus.designsystem.components.ActionDialog
import com.otus.designsystem.components.DialogAction
import com.otus.designsystem.components.PersonAvatar
import com.otus.designsystem.components.PickMark
import com.otus.designsystem.components.PickRow
import com.otus.designsystem.theme.AvatarTokens

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
        AvatarSheet(
            "All addresses of ${account.name}",
            emails.first().substringAfterLast('@'),
            avatars.accountAvatar(account),
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
        ReplaceDialog(avatars, account, choice, onDismiss)
    }
}

/**
 * Lists the addresses the new avatar would replace, each beside the avatar it has now, all ticked.
 * The user unticks the ones that keep theirs; the button saves the avatar to the ticked ones and
 * to every address that had none.
 */
@Composable
private fun ReplaceDialog(
    avatars: OwnAvatarsState,
    account: ConnectedAccount,
    choice: AccountAvatarChoice,
    onDismiss: () -> Unit,
) {
    var ticked by remember(choice) { mutableStateOf(choice.replaced.toSet()) }
    val kept = choice.replaced.toSet() - ticked
    val label =
        when {
            kept.isEmpty() -> "Replace all"
            ticked.isEmpty() -> "Keep all"
            else -> "Replace ${ticked.size}"
        }
    ActionDialog(
        title = if (choice.replaced.size == 1) "Replace 1 avatar?" else "Replace ${choice.replaced.size} avatars?",
        actions =
            listOf(
                DialogAction(label, { avatars.saveAll(account, account.ownAddresses - kept, choice.avatar) }, true),
            ),
        onDismiss = onDismiss,
        message = "These addresses have their own. Untick the ones to keep:",
    ) {
        Column {
            choice.replaced.forEach { email ->
                val avatar =
                    avatars.avatar(account, email)?.toChipAvatar()
                        ?: generatedAvatar(account.name, email).toChipAvatar()
                PickRow(
                    email,
                    null,
                    PickMark(email in ticked, multiple = true),
                    { ticked = if (email in ticked) ticked - email else ticked + email },
                    leading = { PersonAvatar(avatar, AvatarTokens.size) },
                )
            }
        }
    }
}

/** A new account avatar waiting for the user to say whether it replaces [replaced]' own. */
private class AccountAvatarChoice(
    val avatar: ContactAvatar?,
    val replaced: List<String>,
)
