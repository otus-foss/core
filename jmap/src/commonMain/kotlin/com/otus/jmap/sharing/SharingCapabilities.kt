package com.otus.jmap.sharing

import com.otus.jmap.JmapAccount
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

/**
 * The `urn:ietf:params:jmap:principals` account capability (RFC 9670, section 1.5.1). The accounts
 * that hold Principal and ShareNotification objects carry it.
 */
@Serializable
public data class PrincipalsCapability(
    /** The Principal in this account that is the user fetching the session, or null if there is none. */
    val currentUserPrincipalId: String? = null,
)

/**
 * The `urn:ietf:params:jmap:principals:owner` account capability (RFC 9670, section 1.5.2). It names
 * the Principal that owns an account and the account that holds that Principal. The keys of
 * `shareWith` on the account's shared objects are Principal ids in [accountIdForPrincipal]
 * (RFC 9670, section 4).
 */
@Serializable
public data class PrincipalOwner(
    /** An account with the `urn:ietf:params:jmap:principals` capability that holds the owner's Principal. */
    val accountIdForPrincipal: String,
    val principalId: String,
)

/**
 * The account's principals capability (RFC 9670, section 1.5.1), or null when the account lacks it
 * or the server sent a malformed value.
 */
public val JmapAccount.principals: PrincipalsCapability?
    get() = capability(JmapCapability.PRINCIPALS, PrincipalsCapability.serializer())

/**
 * The Principal that owns the account (RFC 9670, section 1.5.2), or null when no Principal owns it,
 * as for the account that holds the Principals, or when the value is malformed. Without an owner a
 * client cannot tell which account the `shareWith` keys refer to, so it should hide sharing controls.
 */
public val JmapAccount.principalOwner: PrincipalOwner?
    get() = capability(JmapCapability.PRINCIPALS_OWNER, PrincipalOwner.serializer())

// A malformed capability object reads as unknown, so one bad server value cannot fail the session.
private fun <T> JmapAccount.capability(
    uri: String,
    serializer: KSerializer<T>,
): T? = capabilityObjects[uri]?.let { runCatching { JmapJson.decodeFromJsonElement(serializer, it) }.getOrNull() }
