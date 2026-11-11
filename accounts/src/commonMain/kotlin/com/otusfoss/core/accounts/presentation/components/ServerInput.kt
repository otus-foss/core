package com.otusfoss.core.accounts.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.accounts.domain.ConnectFailure
import com.otusfoss.core.accounts.domain.JmapServerAddress
import com.otusfoss.core.accounts.domain.parseJmapServer
import com.otusfoss.core.designsystem.shared.input.elements.InputField
import com.otusfoss.core.designsystem.shared.input.elements.InputKind
import com.otusfoss.core.designsystem.shared.input.elements.InputNote
import kotlinx.coroutines.delay

/**
 * The server field. A moment after the last keystroke it asks the server, through [check], whether
 * JMAP answers there, and marks the field with a check or ✕.
 */
@Composable
internal fun ServerInput(
    value: String,
    onValueChange: (String) -> Unit,
    service: AccountService,
    check: suspend (sessionUrl: String) -> ConnectFailure?,
) {
    val address = parseJmapServer(value)
    // Starts empty, so a restored address is not marked before it has been checked.
    var checked by remember { mutableStateOf(ServerCheck(JmapServerAddress.Empty)) }
    LaunchedEffect(address) {
        delay(SERVER_CHECK_DELAY_MILLIS)
        checked = ServerCheck(address, checking = address is JmapServerAddress.Valid)
        if (address is JmapServerAddress.Valid) {
            checked = ServerCheck(address, failure = check(address.sessionUrl))
        }
    }
    // Until the check catches up with the typing, nothing is marked.
    val current = checked.takeIf { it.address == address }
    InputField(
        "Server",
        value,
        onValueChange,
        placeholder = "${service.name}.example.com",
        // The mark alone says what the check found; a failed sign-in says why.
        note = InputNote("", valid = current?.mark()),
        kind = InputKind.Url,
    )
}

/**
 * What the field last learned about [address]: still [checking], or why no JMAP server answers
 * there; neither means one does.
 */
private data class ServerCheck(
    val address: JmapServerAddress,
    val checking: Boolean = false,
    val failure: ConnectFailure? = null,
)

private fun ServerCheck.mark(): Boolean? =
    when {
        checking || address == JmapServerAddress.Empty -> null
        else -> address is JmapServerAddress.Valid && failure == null
    }

private const val SERVER_CHECK_DELAY_MILLIS = 500L
