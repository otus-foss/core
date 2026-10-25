package com.otus.jmap.contacts

import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapType
import com.otus.jscontact.Card
import com.otus.jscontact.JsContactJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * The `ContactCard` data type (RFC 9610, sections 3 and 7.3): a JSContact [Card] with the JMAP
 * properties `id` and `addressBookIds`. It works with the generic `/get`, `/changes`, `/query`,
 * `/queryChanges`, `/set`, and `/copy` methods. A `/get` with `properties` returns partial cards.
 * Unknown properties survive decoding, but update cards with [contactCardPatch] so that only the
 * changed properties go back to the server.
 */
public val ContactCardType: JmapType<Card> =
    JmapType("ContactCard", JmapCapability.CONTACTS, Card.serializer())

/**
 * The card as one `create` entry of `ContactCard/set` (RFC 9610, section 3.5): its JSContact JSON
 * with `@type`, `version`, and unknown properties, without the server-set `id` (RFC 8620, section
 * 5.3). A card must belong to at least one AddressBook, so [Card.addressBookIds] must name one.
 */
public fun Card.toCreation(): JsonObject {
    val books = addressBookIds.orEmpty()
    require(books.isNotEmpty() && books.values.all { it }) {
        "A new ContactCard names at least one AddressBook in addressBookIds, each with the value true"
    }
    return JsonObject(toJsonObject() - "id")
}

internal fun Card.toJsonObject(): JsonObject = JsContactJson.encodeToJsonElement(Card.serializer(), this).jsonObject
