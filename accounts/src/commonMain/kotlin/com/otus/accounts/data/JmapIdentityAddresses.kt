package com.otus.accounts.data

import com.otus.accounts.domain.AccountService
import com.otus.jmap.GetMethod
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapConnections
import com.otus.jmap.JmapException
import com.otus.jmap.JmapType
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginStoreException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Identities describe sending addresses, not every alias that receives mail. RFC 8621, section 6. */
internal suspend fun identityAddresses(
    login: JmapLogin,
    service: AccountService,
    connections: JmapConnections,
): List<String> {
    if (service.capability != JmapCapability.MAIL) return emptyList()
    return try {
        val connection = connections.connection(login)
        val session = connection.session()
        val accountId = session.accountFor(service.capability)
        // Submission may belong to a different account; only show identities for this mail account.
        if (accountId == null || JmapCapability.SUBMISSION !in session.capabilities ||
            JmapCapability.SUBMISSION !in session.accounts[accountId]?.capabilities.orEmpty()
        ) {
            emptyList()
        } else {
            connection
                .call(GetMethod(Identity, accountId, properties = listOf("email")))
                .list
                .mapNotNull { (it["email"] as? JsonPrimitive)?.takeIf { value -> value.isString }?.content }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }
        }
    } catch (expected: JmapException) {
        // Optional address discovery must not hide a saved account when its server is unavailable.
        emptyList()
    } catch (expected: LoginStoreException) {
        emptyList()
    }
}

private val Identity = JmapType("Identity", JmapCapability.SUBMISSION, JsonObject.serializer())
