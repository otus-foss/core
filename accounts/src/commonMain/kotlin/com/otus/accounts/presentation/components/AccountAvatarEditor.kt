package com.otus.accounts.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.otus.accounts.presentation.AccountAvatarEdit
import com.otus.avatar.ContactAvatar
import com.otus.designsystem.shared.overlays.components.ClosableSheet
import com.otus.designsystem.shared.overlays.components.SheetConfirm
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.shared.selection.components.PickMark
import com.otus.designsystem.shared.selection.components.PickRow
import com.otus.designsystem.theme.tokens.people.AvatarTokens

/**
 * Edits one avatar for every address of [edit]'s account. When some addresses already have an
 * avatar of their own, asks which of them the new one replaces.
 */
@Composable
internal fun AccountAvatarEditor(
    edit: AccountAvatarEdit,
    onDismiss: () -> Unit,
) {
    var asking by remember { mutableStateOf<AccountAvatarChoice?>(null) }
    val emails = edit.emails
    val choice = asking
    if (choice == null) {
        AvatarSheet(
            "All addresses of ${edit.name}",
            emails.first().substringAfterLast('@'),
            edit.shown,
            pickPhoto = edit.pickPhoto,
            onSave = { avatar ->
                val replaced = edit.replaced(avatar)
                if (replaced.isEmpty()) {
                    edit.saveAll(emails, avatar)
                } else {
                    asking = AccountAvatarChoice(avatar, replaced)
                }
            },
            // A save that asks first keeps the editor until the user answers.
            onDismiss = { if (asking == null) onDismiss() },
        )
    } else {
        ReplaceSheet(edit, choice, onDismiss)
    }
}

/**
 * Lists the addresses the new avatar would replace, each beside the avatar it has now, all ticked.
 * The user unticks the ones that keep theirs; the button saves the avatar to the ticked ones and
 * to every address that had none.
 */
@Composable
private fun ReplaceSheet(
    edit: AccountAvatarEdit,
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
    ClosableSheet(
        if (choice.replaced.size == 1) "Replace 1 avatar?" else "Replace ${choice.replaced.size} avatars?",
        onDismiss,
        SheetConfirm(label, {
            edit.saveAll(edit.emails - kept, choice.avatar)
            onDismiss()
        }),
    ) {
        choice.replaced.forEach { email ->
            val avatar = edit.avatarOf(email)
            PickRow(
                email,
                subtitle = null,
                PickMark(email in ticked, multiple = true),
                { ticked = if (email in ticked) ticked - email else ticked + email },
                leading = { PersonAvatar(avatar, AvatarTokens.size) },
            )
        }
    }
}

/** A new account avatar waiting for the user to say whether it replaces [replaced]' own. */
private class AccountAvatarChoice(
    val avatar: ContactAvatar?,
    val replaced: List<String>,
)
