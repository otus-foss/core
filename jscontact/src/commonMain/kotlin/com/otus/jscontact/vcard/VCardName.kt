package com.otus.jscontact.vcard

import com.otus.jscontact.Card
import com.otus.jscontact.Name
import com.otus.jscontact.NameComponent
import com.otus.jscontact.NameComponentKind
import com.otus.jscontact.VCard
import com.otus.jscontact.vcard.VCardKey.DERIVED
import com.otus.jscontact.vcard.VCardKey.DERIVED_TRUE
import com.otus.jscontact.vcard.VCardKey.FN
import com.otus.jscontact.vcard.VCardKey.N

/**
 * The [Name] of an FN value and an N property (RFC 9555, Table 1), or null when both are empty. Without
 * FN, the full name joins the N components in reading order. The components are unordered.
 */
internal fun readName(
    full: String?,
    n: VCardProperty?,
): Name? {
    val components = n?.toNameComponents().orEmpty()
    val text = full ?: components.fullName().ifEmpty { null } ?: return null
    return Name(
        components = components.ifEmpty { null },
        isOrdered = false.takeIf { components.isNotEmpty() },
        full = text,
    )
}

/**
 * The N components: surname, given, given2, title, and credential, then surname2 and generation from
 * RFC 9554. A credential that repeats a generation is dropped, because writing adds each generation to the
 * credentials (RFC 9555, Table 1).
 */
private fun VCardProperty.toNameComponents(): List<NameComponent> {
    val slots = value.splitUnescaped(';').map { it.textValues() }
    val generations = slots.getOrNull(N_KINDS.indexOf(GENERATION)).orEmpty()
    return N_KINDS.zip(slots).flatMap { (kind, values) ->
        values.filterNot { kind == NameComponentKind.Credential && it in generations }.map { NameComponent(it, kind) }
    }
}

/** The values joined with spaces in the order people read a name, such as "Dr. Jane Doe". */
internal fun List<NameComponent>.fullName(): String =
    READING_ORDER.flatMap { kind -> filter { it.kind == kind }.map { it.value } }.joinToString(" ")

/**
 * FN, which vCard 4.0 requires. A Card without a name gets its organization, email address, or phone number
 * as FN, marked `DERIVED=TRUE` (RFC 9554, section 4.4) so that [VCard.parse] does not read it as a name.
 */
internal fun Card.fullNameProperty(): VCardProperty =
    name?.writtenFull()?.ifEmpty { null }?.let { VCardProperty(FN, it.escapeText()) }
        ?: VCardProperty(FN, derivedFullName().escapeText(), mapOf(DERIVED to listOf(DERIVED_TRUE)))

private fun Card.derivedFullName(): String =
    listOfNotNull(
        organizations?.values?.firstNotNullOfOrNull { it.name },
        emails?.values?.firstOrNull()?.address,
        phones?.values?.firstOrNull()?.number,
    ).firstOrNull().orEmpty()

/** The full name to write as FN, which vCard 4.0 requires: [Name.full], or else the joined components. */
internal fun Name.writtenFull(): String = full ?: components.orEmpty().fullName()

/**
 * The N property of this name, or null when it has no component N can hold. It writes five components, or
 * seven when the name has a surname2 or a generation.
 */
internal fun Name.toNProperty(): VCardProperty? {
    val parts = components.orEmpty()
    val slots =
        N_KINDS.map { kind ->
            parts
                .filter { it.kind == kind || (kind == NameComponentKind.Credential && it.kind == GENERATION) }
                .joinToString(",") { it.value.escapeText() }
        }
    val written = if (slots.drop(CLASSIC_N_SLOTS).all { it.isEmpty() }) slots.take(CLASSIC_N_SLOTS) else slots
    return VCardProperty(N, written.joinToString(";")).takeIf { slots.any { it.isNotEmpty() } }
}

private val GENERATION = NameComponentKind.Generation

private val N_KINDS =
    listOf(
        NameComponentKind.Surname,
        NameComponentKind.Given,
        NameComponentKind.Given2,
        NameComponentKind.Title,
        NameComponentKind.Credential,
        NameComponentKind.Surname2,
        GENERATION,
    )

private val READING_ORDER =
    listOf(
        NameComponentKind.Title,
        NameComponentKind.Given,
        NameComponentKind.Given2,
        NameComponentKind.Surname,
        NameComponentKind.Surname2,
        NameComponentKind.Credential,
        GENERATION,
    )

/** The components of N in RFC 6350, before RFC 9554 added surname2 and generation. */
private const val CLASSIC_N_SLOTS = 5
