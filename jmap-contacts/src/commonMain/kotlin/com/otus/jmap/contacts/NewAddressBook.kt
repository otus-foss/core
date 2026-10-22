package com.otus.jmap.contacts

import com.otus.jmap.JmapJson
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * An AddressBook to create through [AddressBookSetMethod] (RFC 9610, section 2.3). The server sets
 * `id`, `isDefault`, and `myRights`, so they are not here. To make the new book the default, pass
 * `"#"` and its creation id as `onSuccessSetIsDefault`.
 */
@Serializable
public data class NewAddressBook(
    /** Not empty, and at most 255 octets of UTF-8. */
    val name: String,
    val description: String? = null,
    /** Lower values display first. From 0 up to 2^31 - 1. */
    val sortOrder: Long = 0,
    /**
     * Always sent, because RFC 9610 gives `isSubscribed` no default. Books a user creates should
     * start subscribed.
     */
    @EncodeDefault val isSubscribed: Boolean = true,
    /**
     * The rights to give each Principal, by Principal id (RFC 9670, section 4). The owner cannot be
     * in it, and the user can grant only rights they hold.
     */
    val shareWith: Map<String, AddressBookRights>? = null,
) {
    init {
        requireAddressBookName(name)
        requireSortOrder(sortOrder)
    }

    internal fun toJson(): JsonObject = JmapJson.encodeToJsonElement(NewAddressBook.serializer(), this).jsonObject
}

/** RFC 9610, section 2: the server would reject these names with `invalidProperties` anyway. */
internal fun requireAddressBookName(name: String) {
    require(name.isNotEmpty() && name.encodeToByteArray().size <= MAX_NAME_OCTETS) {
        "An AddressBook name has 1 to $MAX_NAME_OCTETS octets of UTF-8"
    }
}

/** RFC 9610, section 2 limits `sortOrder` to 0 <= sortOrder < 2^31. */
internal fun requireSortOrder(sortOrder: Long) {
    require(sortOrder in 0 until SORT_ORDER_LIMIT) { "An AddressBook sortOrder is from 0 up to 2^31 - 1" }
}

private const val MAX_NAME_OCTETS = 255
private const val SORT_ORDER_LIMIT = 2_147_483_648L
