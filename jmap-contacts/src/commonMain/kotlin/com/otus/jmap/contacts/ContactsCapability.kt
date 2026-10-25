package com.otus.jmap.contacts

import com.otus.jmap.JmapAccount
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The `urn:ietf:params:jmap:contacts` account capability (RFC 9610, section 1.4.1). The session
 * capability is an empty object, so the key's presence alone signals support.
 */
@Serializable
public data class ContactsCapability(
    /**
     * The most AddressBooks one ContactCard may belong to, at least 1. Null means the only limit is
     * the number of AddressBooks in the account.
     */
    val maxAddressBooksPerCard: Long? = null,
    /** Whether the user may create an AddressBook in this account. Offer creation only when true. */
    val mayCreateAddressBook: Boolean = false,
) {
    init {
        require(maxAddressBooksPerCard == null || maxAddressBooksPerCard >= 1) { "Invalid maxAddressBooksPerCard" }
    }
}

/**
 * The account's contacts capability (RFC 9610, section 1.4.1), or null when the account has no
 * contacts. A malformed capability object reads as [ContactsCapability]'s defaults: no known limit,
 * and no creating AddressBooks.
 */
public val JmapAccount.contacts: ContactsCapability?
    get() =
        if (JmapCapability.CONTACTS in capabilities) {
            capabilityObjects[JmapCapability.CONTACTS]?.let(::decodeCapability) ?: ContactsCapability()
        } else {
            null
        }

private fun decodeCapability(json: JsonObject): ContactsCapability? =
    runCatching { JmapJson.decodeFromJsonElement(ContactsCapability.serializer(), json) }.getOrNull()
