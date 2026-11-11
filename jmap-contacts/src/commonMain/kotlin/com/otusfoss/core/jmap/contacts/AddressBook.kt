package com.otusfoss.core.jmap.contacts

import com.otusfoss.core.jmap.JmapCapability
import com.otusfoss.core.jmap.JmapType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * A named collection of ContactCards (RFC 9610, section 2). Every ContactCard belongs to at least
 * one. Show books in [AddressBookOrder], and hide unsubscribed ones and their cards until the user
 * asks for them.
 */
@Serializable
public data class AddressBook(
    val id: String,
    /** The name the user sees: not empty, and at most 255 octets of UTF-8. */
    val name: String = "",
    /** A longer description, for shared books whose name alone does not say enough. */
    val description: String? = null,
    /** Lower values display first. From 0 up to 2^31 - 1. */
    val sortOrder: Long = 0,
    /**
     * Whether clients pick this book when nothing else decides, such as for a new card. At most one
     * book per account is the default, and there may be none. Server-set: `AddressBook/set` changes
     * it with `onSuccessSetIsDefault`.
     */
    val isDefault: Boolean = false,
    /** Whether the user wants to see this book. The server may refuse a subscription with `forbidden`. */
    val isSubscribed: Boolean = false,
    /**
     * The rights of each Principal the book is shared with, by Principal id (RFC 9670, section 4).
     * Those ids live in the account that the book account's `principalOwner` names. Null when the
     * book is not shared or the server does not support sharing; treat an empty map the same way.
     */
    val shareWith: Map<String, AddressBookRights>? = null,
    /** What the user may do with this book. Server-set. */
    val myRights: AddressBookRights = AddressBookRights(),
)

/**
 * What a user or a Principal may do with an AddressBook (RFC 9610, section 2). Encoding always
 * writes all four rights, because RFC 9670 does not say what a missing right means.
 */
@Serializable
public data class AddressBookRights(
    /** May fetch the book's ContactCards. */
    @EncodeDefault val mayRead: Boolean = false,
    /** May create, change, and destroy the book's ContactCards, and move cards into or out of it. */
    @EncodeDefault val mayWrite: Boolean = false,
    /** May change the book's `shareWith`. */
    @EncodeDefault val mayShare: Boolean = false,
    /** May destroy the book itself. */
    @EncodeDefault val mayDelete: Boolean = false,
)

/**
 * The display order of AddressBooks (RFC 9610, section 2): by [AddressBook.sortOrder], then by
 * name. Common Kotlin has no locale-aware collation, so names compare ignoring case first. The id
 * breaks the last ties, so every device shows the same order.
 */
public val AddressBookOrder: Comparator<AddressBook> =
    compareBy<AddressBook> { it.sortOrder }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        .thenBy { it.name }
        .thenBy { it.id }

/**
 * The `AddressBook` data type (RFC 9610, sections 2 and 7.2) for the generic `/get` and `/changes`.
 * `AddressBook/get` accepts null `ids`, so `JmapIds.All` fetches every book (section 2.1).
 */
public val AddressBookType: JmapType<AddressBook> =
    JmapType("AddressBook", JmapCapability.CONTACTS, AddressBook.serializer())
