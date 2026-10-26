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
import com.otus.avatar.GeneratedAvatar
import com.otus.avatar.ResolvedAvatar
import com.otus.avatar.generatedAvatar
import com.otus.designsystem.components.ChipAvatar
import com.otus.designsystem.components.PersonChip
import com.otus.designsystem.components.Recipient
import com.otus.designsystem.components.SettingRow
import com.otus.designsystem.elements.RowButton
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_alternate_email
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.PanelTokens

/**
 * One connected account, its sign-in and server, a button that removes it, and a pill for each of
 * its addresses with that address's avatar. Tapping a pill runs [onEditAvatar] with the address.
 * With more than one address, an All addresses pill comes first, showing [shared], the avatar they
 * all have, and runs [onEditAll].
 */
@Composable
public fun ConnectedAccountRow(
    account: ConnectedAccount,
    avatar: (String) -> ResolvedAvatar?,
    shared: ResolvedAvatar?,
    onRemove: () -> Unit,
    onEditAvatar: (String) -> Unit,
    onEditAll: () -> Unit,
) {
    Column {
        SettingRow(
            account.name,
            onClick = null,
            description = connectedAccountDetail(account),
            icon = Res.drawable.symbol_alternate_email,
        ) { RowButton("Remove", onRemove, accessibilityLabel = "Remove ${account.name}") }
        FlowRow(
            Modifier.padding(horizontal = PageTokens.textInset),
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        ) {
            if (account.ownAddresses.size > 1) {
                val chip = shared?.toChipAvatar() ?: generatedAvatar(account.name, account.username).toChip()
                PersonChip(
                    Recipient("All addresses", chip, "Change the avatar of every address of ${account.name}"),
                    onClick = onEditAll,
                )
            }
            account.ownAddresses.forEach { email ->
                val chip = avatar(email)?.toChipAvatar() ?: generatedAvatar(account.name, email).toChip()
                PersonChip(Recipient(email, chip, "Change the avatar of $email"), onClick = { onEditAvatar(email) })
            }
        }
    }
}

private fun GeneratedAvatar.toChip() = ChipAvatar(initials, colorIndex)

/** The sign-in when it is not one of the addresses the pills show, and the server. */
internal fun connectedAccountDetail(account: ConnectedAccount): String {
    val shown = account.ownAddresses.map { it.lowercase() }.toSet() + account.name.lowercase()
    return (listOf(account.username) + account.emailAddresses)
        .filterNot { it.lowercase() in shown }
        .distinctBy { it.lowercase() }
        .plus(account.server)
        .joinToString("\n")
}
