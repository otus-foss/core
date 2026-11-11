package com.otusfoss.core.avatar.ui.presentation

import com.otusfoss.core.avatar.ContactAvatar
import com.otusfoss.core.avatar.ResolvedAvatar
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar

/**
 * One avatar for all [emails] of the account named [name], as the editor needs it: the avatar it
 * starts from, the photo picker, which addresses a new avatar would replace, each address's
 * avatar now, and how to save.
 */
internal class AccountAvatarEdit(
    val name: String,
    val emails: List<String>,
    val shown: ResolvedAvatar?,
    val pickPhoto: (suspend () -> ContactAvatar.Photo?)?,
    val replaced: (ContactAvatar?) -> List<String>,
    val avatarOf: (String) -> ChipAvatar,
    val saveAll: (List<String>, ContactAvatar?) -> Unit,
)

/** The editing of one avatar for every address of [account]. */
internal fun OwnAvatarsState.accountEdit(account: OwnAccount): AccountAvatarEdit =
    AccountAvatarEdit(
        account.name,
        account.addresses,
        accountAvatar(account),
        pickPhoto = if (canPickPhoto) ::pickPhoto else null,
        replaced = { replaced(account, it) },
        avatarOf = { email -> avatar(account, email).toChipAvatarOr(account.name, email) },
        saveAll = { emails, avatar -> saveAll(account, emails, avatar) },
    )
