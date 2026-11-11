package com.otusfoss.core.accounts.presentation

import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.accounts.domain.ConnectFailure

private const val WRONG_CREDENTIALS_MESSAGE =
    "The server rejected this username or password. " +
        "If your account uses two-factor sign-in, use an app password."

private const val UNREACHABLE_MESSAGE =
    "Couldn't reach the server. Check the address and your connection. " +
        "Its certificate must be trusted by this device."

private const val NOT_JMAP_MESSAGE =
    "No JMAP service was found at this address. Check it, or enter the full JMAP session URL."

public fun ConnectFailure.message(service: AccountService): String =
    when (this) {
        ConnectFailure.WrongCredentials -> WRONG_CREDENTIALS_MESSAGE
        ConnectFailure.NotPermitted -> notPermittedMessage(service)
        ConnectFailure.InvalidUsername -> "Usernames can't contain “:”."
        ConnectFailure.NotJmap -> NOT_JMAP_MESSAGE
        ConnectFailure.MissingService -> "This account doesn't offer ${service.name} over JMAP."
        ConnectFailure.Unreachable -> UNREACHABLE_MESSAGE
        ConnectFailure.Insecure -> "The server tried to use an insecure connection, so it was stopped."
        ConnectFailure.ServerError -> "The server returned an error. Try again later."
        ConnectFailure.StorageFailed -> "The account couldn't be saved on this device. Try again."
    }

private fun notPermittedMessage(service: AccountService): String =
    "The server accepted the sign-in but doesn't allow this account to use JMAP ${service.name}."

public fun confirmHostMessage(host: String): String =
    "This server hands sign-in to $host. Your password will be sent there. Continue only if you trust it."
