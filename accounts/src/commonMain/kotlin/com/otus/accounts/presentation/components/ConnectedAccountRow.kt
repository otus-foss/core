package com.otus.accounts.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.accounts.domain.ConnectedAccount
import com.otus.accounts.presentation.ownAddresses
import com.otus.accounts.presentation.toChipAvatar
import com.otus.avatar.ResolvedAvatar
import com.otus.avatar.generatedAvatar
import com.otus.designsystem.components.AvatarButton
import com.otus.designsystem.components.PersonChip
import com.otus.designsystem.components.Recipient
import com.otus.designsystem.components.SettingRow
import com.otus.designsystem.elements.RowButton
import com.otus.designsystem.theme.EntryTokens
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.PanelTokens

/**
 * One connected account, its sign-in and server, a button that removes it, and a pill for each of
 * its addresses with that address's avatar. Tapping a pill runs [onEditAvatar] with the address.
 * [accountAvatar], its sign-in address's, leads the row, else its initials. Tapping it runs
 * [onEditAll], or [onEditAvatar] when the account has one address.
 */
@Composable
public fun ConnectedAccountRow(
    account: ConnectedAccount,
    avatar: (String) -> ResolvedAvatar?,
    accountAvatar: ResolvedAvatar?,
    onRemove: () -> Unit,
    onEditAvatar: (String) -> Unit,
    onEditAll: () -> Unit,
) {
    Column {
        SettingRow(
            account.name,
            onClick = null,
            description = connectedAccountDetail(account),
            leading = {
                val single = account.ownAddresses.singleOrNull()
                AvatarButton(
                    accountAvatar?.toChipAvatar() ?: generatedAvatar(account.name, account.username).toChipAvatar(),
                    EntryTokens.avatarSize,
                    if (single != null) "Change the avatar of $single" else "Change the avatar of every address",
                    onClick = { if (single != null) onEditAvatar(single) else onEditAll() },
                )
            },
        ) { RowButton("Remove", onRemove, accessibilityLabel = "Remove ${account.name}") }
        FlowRow(
            Modifier.padding(horizontal = PageTokens.textInset),
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        ) {
            account.ownAddresses.forEach { email ->
                val chip = avatar(email)?.toChipAvatar() ?: generatedAvatar(account.name, email).toChipAvatar()
                PersonChip(Recipient(email, chip, "Change the avatar of $email"), onClick = { onEditAvatar(email) })
            }
        }
    }
}

/** The sign-in when it is not one of the addresses the pills show, and the server. */
internal fun connectedAccountDetail(account: ConnectedAccount): String {
    val shown = account.ownAddresses.map { it.lowercase() }.toSet() + account.name.lowercase()
    return (listOf(account.username) + account.emailAddresses)
        .filterNot { it.lowercase() in shown }
        .distinctBy { it.lowercase() }
        .plus(account.server)
        .joinToString("\n")
}
