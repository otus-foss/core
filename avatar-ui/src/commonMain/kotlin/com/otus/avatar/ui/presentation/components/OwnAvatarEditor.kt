package com.otus.avatar.ui.presentation.components

import androidx.compose.runtime.Composable
import com.otus.avatar.ui.presentation.OwnAccount
import com.otus.avatar.ui.presentation.OwnAvatarsState
import com.otus.avatar.ui.presentation.accountEdit

/** The editor of [email]'s avatar, or of every address's of [account] when it is null. */
@Composable
public fun OwnAvatarEditor(
    avatars: OwnAvatarsState,
    account: OwnAccount,
    email: String?,
    onDismiss: () -> Unit,
) {
    if (email == null) {
        AccountAvatarEditor(avatars.accountEdit(account), onDismiss)
    } else {
        AvatarSheet(
            email,
            email.substringAfterLast('@'),
            avatars.avatar(account, email),
            pickPhoto = if (avatars.canPickPhoto) avatars::pickPhoto else null,
            onSave = { avatars.save(account, email, it) },
            onDismiss = onDismiss,
        )
    }
}
