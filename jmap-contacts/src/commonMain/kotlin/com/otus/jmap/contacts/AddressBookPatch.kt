package com.otus.jmap.contacts

import com.otus.jmap.JmapJson
import com.otus.jmap.JmapPatchBuilder
import com.otus.jmap.jmapPatch
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KProperty1

/**
 * Builds the PatchObject of one AddressBook update (RFC 9610, section 2.3; RFC 8620, section 5.3),
 * for the `update` map of an [AddressBookSetMethod]:
 *
 * ```
 * addressBookPatch {
 *     name("Family")
 *     share("P2342fnddd20", AddressBookRights(mayRead = true))
 *     revoke("P674pp24095qo49pr", AddressBookRights::mayWrite)
 * }
 * ```
 */
public fun addressBookPatch(build: AddressBookPatch.() -> Unit): JsonObject =
    jmapPatch { AddressBookPatch(this).build() }

/**
 * The changes one AddressBook update makes (RFC 9610, section 2.3). `isDefault` and `myRights` are
 * server-set, so they are not here.
 *
 * Changing sharing needs the `mayShare` right. A user can grant a Principal only rights the user
 * holds, unless the Principal had them already; the server rejects anything else with `forbidden`.
 * [share], [unshare], [grant], and [revoke] patch one Principal's entry, so the other sharees stay
 * as they are (RFC 9670, section 4). They need a `shareWith` map on the server: when the book's
 * `shareWith` is null, send the whole map with [shareWith]. One patch cannot both replace the map
 * and change an entry in it.
 */
public class AddressBookPatch internal constructor(
    private val patch: JmapPatchBuilder,
) {
    /** Renames the book. The name must not be empty and must fit in 255 octets of UTF-8. */
    public fun name(name: String) {
        requireAddressBookName(name)
        patch.set(JsonPrimitive(name), "name")
    }

    /** Sets the description, or removes it when [description] is null. */
    public fun description(description: String?) {
        if (description == null) {
            patch.remove("description")
        } else {
            patch.set(JsonPrimitive(description), "description")
        }
    }

    /** Moves the book in [AddressBookOrder]. From 0 up to 2^31 - 1. */
    public fun sortOrder(sortOrder: Long) {
        requireSortOrder(sortOrder)
        patch.set(JsonPrimitive(sortOrder), "sortOrder")
    }

    /** Subscribes to the book or unsubscribes from it. The server may refuse with `forbidden`. */
    public fun isSubscribed(isSubscribed: Boolean) {
        patch.set(JsonPrimitive(isSubscribed), "isSubscribed")
    }

    /**
     * Replaces the whole `shareWith` map, which drops every Principal it leaves out. Null stops
     * sharing the book with anyone.
     */
    public fun shareWith(shareWith: Map<String, AddressBookRights>?) {
        if (shareWith == null) {
            patch.remove("shareWith")
        } else {
            patch.set(JmapJson.encodeToJsonElement(SHARE_WITH, shareWith), "shareWith")
        }
    }

    /** Shares the book with [principalId], or replaces that Principal's rights, with all four rights. */
    public fun share(
        principalId: String,
        rights: AddressBookRights,
    ) {
        patch.set(JmapJson.encodeToJsonElement(AddressBookRights.serializer(), rights), "shareWith", principalId)
    }

    /** Stops sharing the book with [principalId]. */
    public fun unshare(principalId: String) {
        patch.remove("shareWith", principalId)
    }

    /** Gives [principalId], which the book is already shared with, one more [right]. */
    public fun grant(
        principalId: String,
        right: KProperty1<AddressBookRights, Boolean>,
    ) {
        patch.set(JsonPrimitive(true), "shareWith", principalId, right.name)
    }

    /** Takes [right] away from [principalId], which keeps its other rights. */
    public fun revoke(
        principalId: String,
        right: KProperty1<AddressBookRights, Boolean>,
    ) {
        patch.set(JsonPrimitive(false), "shareWith", principalId, right.name)
    }
}

private val SHARE_WITH = MapSerializer(String.serializer(), AddressBookRights.serializer())
