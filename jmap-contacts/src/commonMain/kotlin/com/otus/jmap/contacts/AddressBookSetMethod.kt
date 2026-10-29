package com.otus.jmap.contacts

import com.otus.jmap.JmapMethod
import com.otus.jmap.SetMethod
import com.otus.jmap.SetResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * `AddressBook/set` (RFC 9610, section 2.3): a standard `/set` (RFC 8620, section 5.3) with two
 * extra arguments. [update] takes patches from [addressBookPatch].
 *
 * Destroying a book that still holds cards fails with the SetError `addressBookHasContents`, unless
 * [onDestroyRemoveContents] is set. Then the server takes every card out of the book and destroys
 * the cards that are in no other book. This response lists AddressBooks only, so sync ContactCards
 * afterwards.
 *
 * [onSuccessSetIsDefault] names the book to make the default once every create, update, and destroy
 * in this call succeeds. For a book created in the same request, pass `"#"` and its creation id. The
 * server reports the new and the old default in `created` or `updated`. It ignores an unknown id or
 * a change its policy forbids without any error, so read the result before assuming the change.
 */
public class AddressBookSetMethod(
    accountId: String,
    create: Map<String, NewAddressBook> = emptyMap(),
    update: Map<String, JsonObject> = emptyMap(),
    destroy: List<String> = emptyList(),
    /** When set, the server rejects the whole call with `stateMismatch` if its state differs. */
    ifInState: String? = null,
    onDestroyRemoveContents: Boolean = false,
    onSuccessSetIsDefault: String? = null,
) : JmapMethod<SetResult>("${AddressBookType.name}/set", AddressBookType.capability, accountId) {
    private val set =
        SetMethod(
            type = AddressBookType,
            accountId = accountId,
            create = create.mapValues { (_, book) -> book.toJson() },
            update = update,
            destroy = destroy,
            ifInState = ifInState,
            extra =
                buildJsonObject {
                    // Both arguments have defaults the server applies when they are absent.
                    if (onDestroyRemoveContents) put("onDestroyRemoveContents", JsonPrimitive(true))
                    val defaultBook = onSuccessSetIsDefault
                    if (defaultBook != null) put("onSuccessSetIsDefault", JsonPrimitive(defaultBook))
                },
        )

    override fun arguments(): JsonObject = set.arguments()

    override fun decode(arguments: JsonObject): SetResult = set.decode(arguments)
}
