package com.otusfoss.core.accounts.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.accounts.domain.ConnectedAccount
import com.otusfoss.core.accounts.presentation.AccountAvatars
import com.otusfoss.core.accounts.presentation.AccountsState
import com.otusfoss.core.accounts.presentation.ConnectStatus
import com.otusfoss.core.accounts.presentation.components.ConnectAccountForm
import com.otusfoss.core.accounts.presentation.components.ConnectedAccountRow
import com.otusfoss.core.designsystem.shared.controls.elements.ActionButton
import com.otusfoss.core.designsystem.shared.feedback.components.InfoText
import com.otusfoss.core.designsystem.shared.overlays.components.ConfirmDialog
import com.otusfoss.core.designsystem.shared.overlays.components.SheetFrame
import com.otusfoss.core.designsystem.shared.pages.components.PageNote
import com.otusfoss.core.designsystem.shared.pages.components.Subpage
import com.otusfoss.core.designsystem.shared.settings.components.SettingsSection
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens

/**
 * Connected JMAP accounts with their [avatars], and a form that verifies a new one before saving
 * it. [avatarEditor] edits the avatar of one address of an account, or of all of them when the
 * address is null.
 */
@Composable
public fun AccountSetupScreen(
    state: AccountsState,
    avatars: AccountAvatars,
    avatarEditor: @Composable (account: ConnectedAccount, email: String?, onDismiss: () -> Unit) -> Unit,
    title: String,
    service: AccountService,
    onBack: () -> Unit,
) {
    var removing by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    // The account and address whose avatar is being edited; no address edits all of them.
    var editing by rememberSaveable { mutableStateOf<Pair<String, String?>?>(null) }
    LaunchedEffect(state.status) {
        if (state.status is ConnectStatus.Connected) adding = false
    }
    LaunchedEffect(state.accounts) { avatars.load(state.accounts) }
    Subpage(
        title,
        onBack,
        footer = {
            AccountsFooter {
                state.cancelConnect()
                adding = true
            }
        },
    ) {
        state.storageError?.let {
            PageNote(it)
            ActionButton("Reload accounts", state::reload)
        }
        ConnectedAccounts(
            state,
            avatars,
            onRemove = { removing = it },
            onEditAvatar = { id, email -> editing = id to email },
            onEditAll = { editing = it to null },
        )
    }
    editing?.let { (accountId, email) ->
        state.accounts.firstOrNull { it.id == accountId }?.let { avatarEditor(it, email) { editing = null } }
    }
    if (adding) {
        AddAccountSheet(state, service) {
            state.cancelConnect()
            adding = false
        }
    }
    state.accounts.firstOrNull { it.id == removing }?.let { account ->
        ConfirmDialog(
            title = "Remove ${account.name}?",
            message = "Removes it from this app only. Your account and ${service.name} stay on your server.",
            confirmLabel = "Remove",
            onConfirm = { state.remove(account.id) },
            onDismiss = { removing = null },
        )
    }
}

/** Each account with its address pills, or why there are none yet. */
@Composable
private fun ConnectedAccounts(
    state: AccountsState,
    avatars: AccountAvatars,
    onRemove: (String) -> Unit,
    onEditAvatar: (account: String, email: String) -> Unit,
    onEditAll: (account: String) -> Unit,
) {
    SettingsSection("Connected accounts", info = AvatarsInfo.takeIf { state.accounts.isNotEmpty() }) {
        if (state.accounts.isEmpty() && state.storageError == null) {
            PageNote(if (state.loading) "Loading accounts…" else "No accounts connected yet.")
        }
        state.accounts.forEach { account ->
            ConnectedAccountRow(
                account,
                avatar = { avatars.avatar(account, it) },
                accountAvatar = avatars.accountAvatar(account),
                onRemove = { onRemove(account.id) },
                onEditAvatar = { onEditAvatar(account.id, it) },
                onEditAll = { onEditAll(account.id) },
            )
        }
        avatars.error?.let { PageNote(it) }
    }
}

/** The button that connects an account. */
@Composable
private fun AccountsFooter(onConnect: () -> Unit) {
    ActionButton("Connect an account", onConnect, Modifier.fillMaxWidth(), primary = true)
}

@Composable
private fun AddAccountSheet(
    state: AccountsState,
    service: AccountService,
    onDismiss: () -> Unit,
) {
    SheetFrame("Connect an account", onDismiss = onDismiss) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
        ) { ConnectAccountForm(state, service) }
    }
}

private val AvatarsInfo =
    InfoText(
        "Address avatars",
        "Otus shows it wherever it marks which address a message was sent to.",
    )
