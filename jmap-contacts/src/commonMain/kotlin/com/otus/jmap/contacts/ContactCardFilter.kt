package com.otus.jmap.contacts

import com.otus.jmap.JmapFilter
import com.otus.jmap.JmapJson
import com.otus.jmap.UtcDateSerializer
import com.otus.jscontact.CardKind
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Instant

/**
 * A `ContactCard/query` FilterCondition (RFC 9610, section 3.3.1). Null properties are left out. A
 * card matches only when every given property matches, and a filter with none matches every card.
 *
 * [uid] and [kind] match exactly. How the text properties match is up to the server: it should
 * ignore case, treat text in quotes as a phrase, and require every word outside quotes. Inside a
 * phrase, write `\"`, `\'`, and `\\` for a literal quote or backslash. A server that cannot run a
 * filter answers `unsupportedFilter`.
 */
public data class ContactCardFilter(
    /** Matches cards in this AddressBook. */
    val inAddressBook: String? = null,
    /** Matches the card whose JSContact `uid` is exactly this, which may differ from its JMAP id. */
    val uid: String? = null,
    /** Matches group cards whose `members` include this uid. */
    val hasMember: String? = null,
    /** Matches cards whose `kind` is exactly this one. */
    val kind: CardKind? = null,
    /** Matches cards created before this instant. */
    val createdBefore: Instant? = null,
    /** Matches cards created at or after this instant. */
    val createdAfter: Instant? = null,
    /** Matches cards updated before this instant. */
    val updatedBefore: Instant? = null,
    /** Matches cards updated at or after this instant. */
    val updatedAfter: Instant? = null,
    /** Matches text anywhere in the card; the server decides which properties count. */
    val text: String? = null,
    /** Matches any name component or the full name. */
    val name: String? = null,
    /** Matches a `given` name component, sent as `name/given`. */
    val nameGiven: String? = null,
    /** Matches a `surname` name component, sent as `name/surname`. */
    val nameSurname: String? = null,
    /** Matches a `surname2` name component, sent as `name/surname2`. */
    val nameSurname2: String? = null,
    /** Matches the name of any nickname. */
    val nickname: String? = null,
    /** Matches the name of any organization. */
    val organization: String? = null,
    /** Matches the address or label of any email address. */
    val email: String? = null,
    /** Matches the number or label of any phone. */
    val phone: String? = null,
    /** Matches the service, URI, user name, or label of any online service. */
    val onlineService: String? = null,
    /** Matches any address component or the full address of any address. */
    val address: String? = null,
    /** Matches the text of any note. */
    val note: String? = null,
) {
    /** The condition as a filter, to send alone or to combine with other filters (RFC 8620, section 5.5). */
    public fun toFilter(): JmapFilter {
        val values = conditions().mapNotNull { (key, value) -> value?.let { key to it } }.toMap()
        return JmapFilter.Condition(JsonObject(values))
    }

    // The name keys contain a literal slash: they are flat condition names, not nested objects.
    private fun conditions(): List<Pair<String, JsonElement?>> =
        listOf(
            "inAddressBook" to string(inAddressBook),
            "uid" to string(uid),
            "hasMember" to string(hasMember),
            "kind" to string(kind?.value),
            "createdBefore" to instant(createdBefore),
            "createdAfter" to instant(createdAfter),
            "updatedBefore" to instant(updatedBefore),
            "updatedAfter" to instant(updatedAfter),
            "text" to string(text),
            "name" to string(name),
            "name/given" to string(nameGiven),
            "name/surname" to string(nameSurname),
            "name/surname2" to string(nameSurname2),
            "nickname" to string(nickname),
            "organization" to string(organization),
            "email" to string(email),
            "phone" to string(phone),
            "onlineService" to string(onlineService),
            "address" to string(address),
            "note" to string(note),
        )
}

private fun string(value: String?): JsonElement? = value?.let { JsonPrimitive(it) }

private fun instant(value: Instant?): JsonElement? = value?.let { JmapJson.encodeToJsonElement(UtcDateSerializer, it) }
