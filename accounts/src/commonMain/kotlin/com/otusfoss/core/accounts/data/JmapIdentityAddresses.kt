package com.otusfoss.core.accounts.data

import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.jmap.GetMethod
import com.otusfoss.core.jmap.JmapCapability
import com.otusfoss.core.jmap.JmapConnection
import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.JmapException
import com.otusfoss.core.jmap.JmapSession
import com.otusfoss.core.jmap.JmapType
import com.otusfoss.core.jmap.login.JmapLogin
import com.otusfoss.core.jmap.login.LoginStoreException
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
