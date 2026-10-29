package com.otus.accounts.presentation

import com.otus.accounts.domain.ConnectedAccount
import com.otus.avatar.ContactAvatar
import com.otus.avatar.ResolvedAvatar
import com.otus.designsystem.shared.people.components.ChipAvatar

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
internal fun OwnAvatarsState.accountEdit(account: ConnectedAccount): AccountAvatarEdit =
    AccountAvatarEdit(
        account.name,
        account.ownAddresses,
        accountAvatar(account),
        pickPhoto = if (canPickPhoto) ::pickPhoto else null,
        replaced = { replaced(account, it) },
        avatarOf = { email -> avatar(account, email).toChipAvatarOr(account.name, email) },
        saveAll = { emails, avatar -> saveAll(account, emails, avatar) },
    )
