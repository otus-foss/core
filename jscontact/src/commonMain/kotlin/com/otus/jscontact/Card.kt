package com.otus.jscontact

import com.otus.json.types.UtcDateSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * A JSContact Card: the contact information of a person, organization, or other entity (RFC 9553,
 * section 2), with the JMAP ContactCard properties [id] and [addressBookIds] (RFC 9610, section 3).
 *
 * A JMAP `/get` with `properties` returns only some properties, so every property decodes as optional,
 * the mandatory [uid] included. Encoding always writes `"@type": "Card"` and [version]. `Id[X]` maps keep their keys, which must survive every edit
 * (section 1.4.1).
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Card.Serializer::class)
public data class Card(
    /**
     * The JSContact version of the properties and values (RFC 9553, section 2.1.2). The RFC makes it mandatory
     * and gives no default. This library fills in `1.0` when the JSON lacks it, so that partial JMAP `/get`
     * results decode.
     */
    @EncodeDefault val version: String = JSCONTACT_VERSION,
    /** When the Card was created (RFC 9553, section 2.1.3). */
    @Serializable(with = UtcDateSerializer::class)
    val created: Instant? = null,
    /** The kind of entity. Absent means [CardKind.Individual] (RFC 9553, section 2.1.4). */
    val kind: CardKind? = null,
    /** The RFC 5646 language tag of the Card's text, such as `de-AT` (RFC 9553, section 2.1.5). */
    val language: String? = null,
    /** The uids of the member Cards of a group, each mapped to `true` (RFC 9553, section 2.1.6). */
    val members: Map<String, Boolean>? = null,
    /** The product that created the Card (RFC 9553, section 2.1.7). */
    val prodId: String? = null,
    /** How other Cards relate to this one, keyed by their uids (RFC 9553, section 2.1.8). */
    val relatedTo: Map<String, Relation>? = null,
    /**
     * The identifier of the entity across systems, a URN such as `urn:uuid:...` or free text. Mandatory,
     * but null when a JMAP `/get` did not ask for it (RFC 9553, section 2.1.9).
     */
    val uid: String? = null,
    /** When the Card was last modified (RFC 9553, section 2.1.10). */
    @Serializable(with = UtcDateSerializer::class)
    val updated: Instant? = null,
    /** The name of the entity (RFC 9553, section 2.2.1). */
    val name: Name? = null,
    /** Nicknames, keyed by Id (RFC 9553, section 2.2.2). */
    val nicknames: Map<String, Nickname>? = null,
    /** Organizations and their units, keyed by Id (RFC 9553, section 2.2.3). */
    val organizations: Map<String, Organization>? = null,
    /** How to address or refer to the entity (RFC 9553, section 2.2.4). */
    val speakToAs: SpeakToAs? = null,
    /** Job titles and roles, keyed by Id (RFC 9553, section 2.2.5). */
    val titles: Map<String, Title>? = null,
    /** Email addresses, keyed by Id (RFC 9553, section 2.3.1). */
    val emails: Map<String, EmailAddress>? = null,
    /** Messaging services and online profiles, keyed by Id (RFC 9553, section 2.3.2). */
    val onlineServices: Map<String, OnlineService>? = null,
    /** Phone numbers, keyed by Id (RFC 9553, section 2.3.3). */
    val phones: Map<String, Phone>? = null,
    /** Languages to contact the entity in, keyed by Id (RFC 9553, section 2.3.4). */
    val preferredLanguages: Map<String, LanguagePref>? = null,
    /** Calendars, such as free-busy lookups, keyed by Id (RFC 9553, section 2.4.1). */
    val calendars: Map<String, Calendar>? = null,
    /** Addresses for calendar scheduling invitations, keyed by Id (RFC 9553, section 2.4.2). */
    val schedulingAddresses: Map<String, SchedulingAddress>? = null,
    /** Postal addresses and geographic locations, keyed by Id (RFC 9553, section 2.5.1). */
    val addresses: Map<String, Address>? = null,
    /** Public keys and certificates, keyed by Id (RFC 9553, section 2.6.1). */
    val cryptoKeys: Map<String, CryptoKey>? = null,
    /** Directories that hold the entity, keyed by Id (RFC 9553, section 2.6.2). */
    val directories: Map<String, Directory>? = null,
    /** Other links, keyed by Id (RFC 9553, section 2.6.3). */
    val links: Map<String, Link>? = null,
    /** Photos, logos, and sounds, keyed by Id (RFC 9553, section 2.6.4). */
    val media: Map<String, Media>? = null,
    /**
     * PatchObjects that localize this Card, keyed by RFC 5646 language tag (RFC 9553, section 2.7.1).
     * [localized] applies one.
     */
    val localizations: Map<String, JsonObject>? = null,
    /** Birthdays and other memorable dates, keyed by Id (RFC 9553, section 2.8.1). */
    val anniversaries: Map<String, Anniversary>? = null,
    /** Free-text keywords, each mapped to `true` (RFC 9553, section 2.8.2). */
    val keywords: Map<String, Boolean>? = null,
    /** Free-text notes, keyed by Id (RFC 9553, section 2.8.3). */
    val notes: Map<String, Note>? = null,
    /** Hobbies, interests, and expertise, keyed by Id (RFC 9553, section 2.8.4). */
    val personalInfo: Map<String, PersonalInfo>? = null,
    /**
     * The JMAP id: server-set, immutable, and possibly different from [uid]. Null for a Card that is not
     * a JMAP ContactCard (RFC 9610, sections 3 and 7.5).
     */
    val id: String? = null,
    /**
     * The JMAP AddressBooks that hold the Card, each id mapped to `true`. A ContactCard belongs to at least
     * one (RFC 9610, sections 3 and 7.5).
     */
    val addressBookIds: Map<String, Boolean>? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    /**
     * This Card in [language], as RFC 9553, section 2.7.1, describes: a copy without [localizations], with
     * the PatchObject for [language] applied and [language] set as its language. Tags match ignoring case
     * (RFC 5646, section 2.1.1). Returns this Card unchanged when it has no localization for [language].
     *
     * Throws [IllegalArgumentException] and applies nothing when the PatchObject:
     * - breaks a pointer rule of RFC 9553, section 1.4.3, that [JsContactPatch.apply] enforces,
     * - patches `localizations` (section 2.7.1),
     * - removes the mandatory `@type` or `version` with a null (section 1.4.3, rule 4), which decoding would
     *   fill in again unnoticed, or
     * - sets a value of the wrong JSON type, so the result does not decode.
     *
     * Rule 4 also forbids values that decode but break their property, such as a `pref` of 0, an invalid Id,
     * or a null that removes `uid`. This function applies those.
     */
    public fun localized(language: String): Card = localizedTo(language)

    internal object Serializer : JsContactSerializer<Card>(
        generatedSerializer(),
        "Card",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/** The only version RFC 9553 registers (section 1.9.2). */
private const val JSCONTACT_VERSION = "1.0"
