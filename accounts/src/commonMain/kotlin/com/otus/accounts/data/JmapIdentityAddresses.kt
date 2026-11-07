package com.otus.accounts.data

import com.otus.accounts.domain.AccountService
import com.otus.jmap.GetMethod
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapConnection
import com.otus.jmap.JmapConnections
import com.otus.jmap.JmapException
import com.otus.jmap.JmapSession
import com.otus.jmap.JmapType
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginStoreException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The sending addresses of [login]'s account for [service], from its identities (RFC 8621, section
 * 6), which describe sending addresses, not every alias that receives mail. None when that account
 * cannot submit mail.
 */
internal suspend fun identityAddresses(
    login: JmapLogin,
    service: AccountService,
    connections: JmapConnections,
): List<String> =
    try {
        val account = connections.account(login, service.capability)
        account
            .takeIf { account.session.canSubmitFrom(account.id) }
            ?.let { identityEmails(account.connection, account.id) }
            .orEmpty()
    } catch (expected: JmapException) {
        // Optional address discovery must not hide a saved account when its server is unavailable.
        emptyList()
    } catch (expected: LoginStoreException) {
        emptyList()
    }

/** Submission may belong to a different account; only show identities for this account. */
private fun JmapSession.canSubmitFrom(accountId: String): Boolean =
    JmapCapability.SUBMISSION in capabilities &&
        JmapCapability.SUBMISSION in accounts[accountId]?.capabilities.orEmpty()

private suspend fun identityEmails(
    connection: JmapConnection,
    accountId: String,
): List<String> =
    connection
        .call(GetMethod(Identity, accountId, properties = listOf("email")))
        .list
        .mapNotNull { (it["email"] as? JsonPrimitive)?.takeIf { value -> value.isString }?.content }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }

private val Identity = JmapType("Identity", JmapCapability.SUBMISSION, JsonObject.serializer())
