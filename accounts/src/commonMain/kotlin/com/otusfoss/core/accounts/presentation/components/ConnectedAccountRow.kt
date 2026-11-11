package com.otusfoss.core.accounts.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.accounts.domain.ConnectedAccount
import com.otusfoss.core.accounts.domain.ownAddresses
import com.otusfoss.core.designsystem.shared.people.components.AvatarButton
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import com.otusfoss.core.designsystem.shared.people.components.PersonChip
import com.otusfoss.core.designsystem.shared.people.components.Recipient
import com.otusfoss.core.designsystem.shared.rows.elements.RowButton
import com.otusfoss.core.designsystem.shared.settings.components.SettingRow
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.EntryTokens

/**
 * One connected account, its sign-in and server, a button that removes it, and a pill for each of
 * its addresses with that address's avatar. Tapping a pill runs [onEditAvatar] with the address.
 * [accountAvatar], its sign-in address's, leads the row. Tapping it runs
 * [onEditAll], or [onEditAvatar] when the account has one address. An account with no addresses,
 * such as one signed in with a plain username while its server is unreachable, has nothing to edit.
 */
@Composable
public fun ConnectedAccountRow(
    account: ConnectedAccount,
    avatar: (String) -> ChipAvatar,
    accountAvatar: ChipAvatar,
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
                val addresses = account.ownAddresses
                val single = addresses.singleOrNull()
                AvatarButton(
                    accountAvatar,
                    EntryTokens.avatarSize,
                    if (single != null) "Change the avatar of $single" else "Change the avatar of every address",
                    onClick =
                        when {
                            single != null -> ({ onEditAvatar(single) })
                            addresses.isEmpty() -> null
                            else -> onEditAll
                        },
                )
            },
        ) { RowButton("Remove", onRemove, accessibilityLabel = "Remove ${account.name}") }
        FlowRow(
            Modifier.padding(horizontal = PageTokens.textInset),
            horizontalArrangement = Arrangement.spacedBy(Spacing.inlineGap),
        ) {
            account.ownAddresses.forEach { email ->
                PersonChip(
                    Recipient(email, avatar(email), "Change the avatar of $email"),
                    onClick = { onEditAvatar(email) },
                )
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
