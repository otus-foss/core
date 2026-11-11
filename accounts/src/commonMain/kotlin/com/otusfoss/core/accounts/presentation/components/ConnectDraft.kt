package com.otusfoss.core.accounts.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.otusfoss.core.accounts.domain.JmapAccountDraft
import com.otusfoss.core.accounts.domain.JmapServerAddress
import com.otusfoss.core.accounts.domain.isValidUsername
import com.otusfoss.core.accounts.domain.parseJmapServer
import com.otusfoss.core.accounts.presentation.AccountsState

/**
 * What the user has typed to sign in: the [server], the [username], and the [password], which
 * stays in memory only and is never saved with the screen's state.
 */
@Stable
public class ConnectDraft internal constructor(
    server: String = "",
    username: String = "",
) {
    internal var server by mutableStateOf(server)
    internal var username by mutableStateOf(username)
    internal var password by mutableStateOf("")

    private val address: JmapServerAddress get() = parseJmapServer(server)

    /** Whether there is enough to try a sign-in with. */
    internal val ready: Boolean
        get() = address is JmapServerAddress.Valid && isValidUsername(username.trim()) && password.isNotEmpty()

    internal fun connect(state: AccountsState) {
        val address = address
        if (address is JmapServerAddress.Valid) {
            state.connect(JmapAccountDraft(address.sessionUrl, username.trim(), password))
        }
    }

    internal fun clear() {
        server = ""
        username = ""
        password = ""
    }
}

/** A [ConnectDraft] whose server and username survive a recreated screen; the password does not. */
@Composable
public fun rememberConnectDraft(): ConnectDraft =
    rememberSaveable(
        saver =
            listSaver(
                save = { listOf(it.server, it.username) },
                restore = { ConnectDraft(it[0], it[1]) },
            ),
    ) { ConnectDraft() }
