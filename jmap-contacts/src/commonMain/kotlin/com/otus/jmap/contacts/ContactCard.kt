package com.otus.jmap.contacts

import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapType
import com.otus.jscontact.Card
import com.otus.jscontact.JsContactJson
import com.otus.jscontact.cardPropertyNames
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * The `ContactCard` data type (RFC 9610, sections 3 and 7.3): a JSContact [Card] with the JMAP
 * properties `id` and `addressBookIds`. It works with the generic `/get`, `/changes`, `/query`,
 * `/queryChanges`, `/set`, and `/copy` methods. A `/get` with `properties` returns partial cards.
 * Unknown properties survive decoding; update cards with [cardPatch] so that only the changed
 * properties go back to the server.
 */
public val ContactCardType: JmapType<Card> =
    JmapType("ContactCard", JmapCapability.CONTACTS, Card.serializer())

/**
 * Every [Card] property plus `vCard`, for a `ContactCard/get` that needs a card's extra vCard
 * properties: Stalwart returns them only when `vCard` is asked for by name (RFC 9555, section
 * 2.15). A server that does not know `vCard` rejects the call with `invalidArguments` (RFC 8620,
 * section 5.1), so ask again without properties then.
 */
public val contactCardPropertiesWithVCard: List<String> = cardPropertyNames + "vCard"

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
