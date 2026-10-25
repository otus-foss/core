package com.otus.jscontact

import kotlinx.serialization.json.JsonObject

/**
 * Every problem of [card], see [Card.validate]: the Card's own, then those each localization adds when it
 * is applied (RFC 9553, section 2.7.1).
 */
internal fun cardProblems(card: Card): List<JsContactProblem> {
    val own = validation { checkCard(card) }
    return own + validation { checkLocalizations(card, own) }
}

// Walks every property but localizations, which only make sense applied to the rest.
private fun ValidationScope.checkCard(card: Card) {
    propertyNames(card.extra, Card.serializer())
    checkMetadata(card)
    checkJmapProperties(card)
    member("name", card.name, ValidationScope::checkName)
    idMap("nicknames", card.nicknames, ValidationScope::checkNickname)
    idMap("organizations", card.organizations, ValidationScope::checkOrganization)
    member("speakToAs", card.speakToAs, ValidationScope::checkSpeakToAs)
    idMap("titles", card.titles, ValidationScope::checkTitle)
    idMap("emails", card.emails, ValidationScope::checkEmail)
    idMap("onlineServices", card.onlineServices, ValidationScope::checkOnlineService)
    idMap("phones", card.phones, ValidationScope::checkPhone)
    idMap("preferredLanguages", card.preferredLanguages, ValidationScope::checkLanguagePref)
    idMap("calendars", card.calendars, ValidationScope::checkCalendar)
    idMap("schedulingAddresses", card.schedulingAddresses, ValidationScope::checkSchedulingAddress)
    idMap("addresses", card.addresses, ValidationScope::checkAddress)
    idMap("cryptoKeys", card.cryptoKeys, ValidationScope::checkCryptoKey)
    idMap("directories", card.directories, ValidationScope::checkDirectory)
    idMap("links", card.links, ValidationScope::checkLink)
    idMap("media", card.media, ValidationScope::checkMedia)
    idMap("anniversaries", card.anniversaries, ValidationScope::checkAnniversary)
    trueSet("keywords", card.keywords, "RFC 9553, section 2.8.2")
    idMap("notes", card.notes, ValidationScope::checkNote)
    idMap("personalInfo", card.personalInfo, ValidationScope::checkPersonalInfo)
}

/**
 * Validates the metadata (RFC 9553, section 2.1). The version must be a registered one (section 2.1.2), and
 * RFC 9553 registers only major version 1. Any 1.x passes, because a later minor version must not change how
 * 1.0 data is processed, while another major version may change it incompatibly (section 1.9).
 */
private fun ValidationScope.checkMetadata(card: Card) {
    ensure(
        VERSION.matches(card.version),
        "Not a registered JSContact 1.x version (RFC 9553, sections 1.9 and 2.1.2)",
        "version",
    )
    ensure(card.uid != null, "A Card needs a uid (RFC 9553, section 2.1.9)", "uid")
    enumValue(card.kind?.value, RegisteredValues.cardKinds, "kind")
    languageTag(card.language, "language")
    trueSet("members", card.members, MEMBERS_SECTION)
    ensure(
        card.members == null || card.kind == CardKind.Group,
        "A Card with members needs the kind \"group\" ($MEMBERS_SECTION)",
        "members",
    )
    ensure(card.prodId?.isEmpty() != true, "prodId must not be empty (RFC 9553, section 2.1.7)", "prodId")
    entries("relatedTo", card.relatedTo, ValidationScope::checkRelation)
}

/** Validates the JMAP ContactCard properties (RFC 9610, section 3). */
private fun ValidationScope.checkJmapProperties(card: Card) {
    ensure(card.id == null || ID.matches(card.id), "Not an Id ($JMAP_ID_SECTION)", "id")
    idKeys(ADDRESS_BOOK_IDS, card.addressBookIds, JMAP_ID_SECTION)
    trueSet(ADDRESS_BOOK_IDS, card.addressBookIds, CONTACT_CARD_SECTION)
    ensure(
        card.addressBookIds?.isEmpty() != true,
        "A ContactCard belongs to at least one AddressBook ($CONTACT_CARD_SECTION)",
        ADDRESS_BOOK_IDS,
    )
}

/**
 * Applies each localization and reports, at its language tag, a tag that is not one, a patch that fails
 * (RFC 9553, section 1.4.3), and each problem of the localized Card that [own] lacks, because patched values
 * must stay valid (section 2.7.1).
 */
private fun ValidationScope.checkLocalizations(
    card: Card,
    own: List<JsContactProblem>,
) {
    member("localizations", card.localizations) { localizations ->
        localizations.forEach { (tag, patch) ->
            languageTag(tag, tag)
            localizationProblems(card, tag, patch, own).forEach { report(it, tag) }
        }
    }
}

/** The problems [patch] adds to [card] beyond [own], as `path: message` text, or why it does not apply. */
private fun localizationProblems(
    card: Card,
    tag: String,
    patch: JsonObject,
    own: List<JsContactProblem>,
): List<String> =
    try {
        // Keeping the Card's language reports an invalid tag once, at the tag itself.
        val localized = card.localizedWith(tag, patch).copy(language = card.language)
        (validation { checkCard(localized) } - own.toSet()).map { "${it.path}: ${it.message}" }
    } catch (invalid: IllegalArgumentException) {
        listOf(invalid.message ?: "The PatchObject does not apply (RFC 9553, section 1.4.3)")
    }

private const val MEMBERS_SECTION = "RFC 9553, section 2.1.6"

private const val CONTACT_CARD_SECTION = "RFC 9610, section 3"

private const val JMAP_ID_SECTION = "RFC 8620, section 1.2"

private const val ADDRESS_BOOK_IDS = "addressBookIds"

// Major version 1 with any minor version (RFC 9553, sections 1.9.1 and 1.9.2).
private val VERSION = Regex("1\\.[0-9]+")
