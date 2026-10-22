package com.otus.jmap.contacts

import com.otus.jmap.JmapPatchBuilder
import com.otus.jmap.jmapPatch
import com.otus.jscontact.Address
import com.otus.jscontact.Anniversary
import com.otus.jscontact.Calendar
import com.otus.jscontact.Card
import com.otus.jscontact.CardKind
import com.otus.jscontact.CryptoKey
import com.otus.jscontact.Directory
import com.otus.jscontact.EmailAddress
import com.otus.jscontact.LanguagePref
import com.otus.jscontact.Link
import com.otus.jscontact.Media
import com.otus.jscontact.Name
import com.otus.jscontact.Nickname
import com.otus.jscontact.Note
import com.otus.jscontact.OnlineService
import com.otus.jscontact.Organization
import com.otus.jscontact.PersonalInfo
import com.otus.jscontact.Phone
import com.otus.jscontact.Relation
import com.otus.jscontact.SchedulingAddress
import com.otus.jscontact.SpeakToAs
import com.otus.jscontact.Title
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * Builds the PatchObject of one ContactCard update (RFC 9610, section 3.5; RFC 8620, section 5.3),
 * for the `update` map of a `ContactCard/set`. It sends only what changes, so properties this client
 * does not know stay as they are on the server:
 *
 * ```
 * contactCardPatch {
 *     emails.put("e2", EmailAddress("joe@work.example.com"))
 *     phones.remove("p1")
 *     addressBookIds.add("b2")
 *     addressBookIds.remove("b1")
 *     updated.set(now)
 *     raw { set(JsonPrimitive("Joe Bloggs"), "name", "full") }
 * }
 * ```
 */
public fun contactCardPatch(build: ContactCardPatch.() -> Unit): JsonObject =
    jmapPatch { ContactCardPatch(this).build() }

/**
 * The changes one ContactCard update makes, one handle per Card property of RFC 9553, section 2, and
 * RFC 9610, section 3. The server sets `id`, so it has no handle. Paths may not overlap: setting a
 * whole property and one of its entries in the same patch throws [IllegalArgumentException].
 */
public class ContactCardPatch internal constructor(
    private val patch: JmapPatchBuilder,
) {
    /** The JSContact version of the card's data (RFC 9553, section 2.1.2). */
    public val version: CardPatchProperty<String> = property("version") { Card(version = it) }

    /** When the card was created (RFC 9553, section 2.1.3). */
    public val created: CardPatchProperty<Instant> = property("created") { Card(created = it) }

    /** What kind of entity the card is; removing it means an individual (RFC 9553, section 2.1.4). */
    public val kind: CardPatchProperty<CardKind> = property("kind") { Card(kind = it) }

    /** The language tag of the card's text (RFC 9553, section 2.1.5). */
    public val language: CardPatchProperty<String> = property("language") { Card(language = it) }

    /**
     * The uids of a group card's members (RFC 9553, section 2.1.6). Keep uids no account resolves:
     * access to their cards may come back (RFC 9610, section 3).
     */
    public val members: CardPatchSet = set("members") { Card(members = it) }

    /** The product that last wrote the card (RFC 9553, section 2.1.7). */
    public val prodId: CardPatchProperty<String> = property("prodId") { Card(prodId = it) }

    /** Related cards by uid or URI (RFC 9553, section 2.1.8). */
    public val relatedTo: CardPatchMap<Relation> = map("relatedTo") { Card(relatedTo = it) }

    /** The card's uid, unique within an account (RFC 9553, section 2.1.9; RFC 9610, section 3). */
    public val uid: CardPatchProperty<String> = property("uid") { Card(uid = it) }

    /** When the card was last changed (RFC 9553, section 2.1.10). */
    public val updated: CardPatchProperty<Instant> = property("updated") { Card(updated = it) }

    /** The name (RFC 9553, section 2.2.1). Its components are an array, so set the name whole. */
    public val name: CardPatchProperty<Name> = property("name") { Card(name = it) }

    /** Nicknames (RFC 9553, section 2.2.2). */
    public val nicknames: CardPatchMap<Nickname> = map("nicknames") { Card(nicknames = it) }

    /** Organizations (RFC 9553, section 2.2.3). */
    public val organizations: CardPatchMap<Organization> = map("organizations") { Card(organizations = it) }

    /** How to address the entity (RFC 9553, section 2.2.4). */
    public val speakToAs: CardPatchProperty<SpeakToAs> = property("speakToAs") { Card(speakToAs = it) }

    /** Job titles and roles (RFC 9553, section 2.2.5). */
    public val titles: CardPatchMap<Title> = map("titles") { Card(titles = it) }

    /** Email addresses (RFC 9553, section 2.3.1). */
    public val emails: CardPatchMap<EmailAddress> = map("emails") { Card(emails = it) }

    /** Online services, such as social media accounts (RFC 9553, section 2.3.2). */
    public val onlineServices: CardPatchMap<OnlineService> = map("onlineServices") { Card(onlineServices = it) }

    /** Phone numbers (RFC 9553, section 2.3.3). */
    public val phones: CardPatchMap<Phone> = map("phones") { Card(phones = it) }

    /** Preferred languages for contacting the entity (RFC 9553, section 2.3.4). */
    public val preferredLanguages: CardPatchMap<LanguagePref> =
        map("preferredLanguages") { Card(preferredLanguages = it) }

    /** Calendars with the entity's events or free-busy times (RFC 9553, section 2.4.1). */
    public val calendars: CardPatchMap<Calendar> = map("calendars") { Card(calendars = it) }

    /** Addresses to send scheduling messages to (RFC 9553, section 2.4.2). */
    public val schedulingAddresses: CardPatchMap<SchedulingAddress> =
        map("schedulingAddresses") { Card(schedulingAddresses = it) }

    /** Postal addresses and locations (RFC 9553, section 2.5.1). */
    public val addresses: CardPatchMap<Address> = map("addresses") { Card(addresses = it) }

    /** Public keys (RFC 9553, section 2.6.1). */
    public val cryptoKeys: CardPatchMap<CryptoKey> = map("cryptoKeys") { Card(cryptoKeys = it) }

    /** Directories with information about the entity (RFC 9553, section 2.6.2). */
    public val directories: CardPatchMap<Directory> = map("directories") { Card(directories = it) }

    /** Links to other resources (RFC 9553, section 2.6.3). */
    public val links: CardPatchMap<Link> = map("links") { Card(links = it) }

    /**
     * Photos, logos, and sounds (RFC 9553, section 2.6.4). A new photo can name an uploaded blob by
     * `blobId` in place of `uri` (RFC 9610, section 3.5).
     */
    public val media: CardPatchMap<Media> = map("media") { Card(media = it) }

    /** PatchObjects that localize the card, by language tag (RFC 9553, section 2.7.1). */
    public val localizations: CardPatchMap<JsonObject> = map("localizations") { Card(localizations = it) }

    /** Birthdays and other memorable dates (RFC 9553, section 2.8.1). */
    public val anniversaries: CardPatchMap<Anniversary> = map("anniversaries") { Card(anniversaries = it) }

    /** Free-form keywords or tags (RFC 9553, section 2.8.2). */
    public val keywords: CardPatchSet = set("keywords") { Card(keywords = it) }

    /** Notes (RFC 9553, section 2.8.3). */
    public val notes: CardPatchMap<Note> = map("notes") { Card(notes = it) }

    /** Personal interests, hobbies, and skills (RFC 9553, section 2.8.4). */
    public val personalInfo: CardPatchMap<PersonalInfo> = map("personalInfo") { Card(personalInfo = it) }

    /**
     * The AddressBooks the card belongs to (RFC 9610, section 3). The card must stay in at least one,
     * so add the new book and remove the old one in the same patch to move a card. Writing needs
     * `mayWrite` on the books involved.
     */
    public val addressBookIds: CardPatchSet = set("addressBookIds") { Card(addressBookIds = it) }

    /**
     * Adds entries by path for what the handles cannot reach, such as one property inside an entry:
     * `raw { set(JsonPrimitive("joe@example.com"), "emails", "e1", "address") }`. The path's parents
     * must exist on the server.
     */
    public fun raw(build: JmapPatchBuilder.() -> Unit) {
        patch.build()
    }

    private fun <T> property(
        name: String,
        wrap: (T) -> Card,
    ): CardPatchProperty<T> = CardPatchProperty(patch, name, wrap)

    private fun <V> map(
        name: String,
        wrap: (Map<String, V>) -> Card,
    ): CardPatchMap<V> = CardPatchMap(patch, name, wrap)

    private fun set(
        name: String,
        wrap: (Map<String, Boolean>) -> Card,
    ): CardPatchSet = CardPatchSet(patch, name, wrap)
}
