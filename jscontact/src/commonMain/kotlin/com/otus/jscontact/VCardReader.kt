package com.otus.jscontact

import com.otus.jscontact.VCardKey.AB_LABEL
import com.otus.jscontact.VCardKey.ADR
import com.otus.jscontact.VCardKey.APPLE_KIND
import com.otus.jscontact.VCardKey.APPLE_MEMBER
import com.otus.jscontact.VCardKey.BASE64
import com.otus.jscontact.VCardKey.BASE64_SHORT
import com.otus.jscontact.VCardKey.EMAIL
import com.otus.jscontact.VCardKey.FN
import com.otus.jscontact.VCardKey.KIND
import com.otus.jscontact.VCardKey.MEDIATYPE
import com.otus.jscontact.VCardKey.MEMBER
import com.otus.jscontact.VCardKey.N
import com.otus.jscontact.VCardKey.NICKNAME
import com.otus.jscontact.VCardKey.NOTE
import com.otus.jscontact.VCardKey.ORG
import com.otus.jscontact.VCardKey.PHOTO
import com.otus.jscontact.VCardKey.ROLE
import com.otus.jscontact.VCardKey.TEL
import com.otus.jscontact.VCardKey.TITLE
import com.otus.jscontact.VCardKey.UID
import com.otus.jscontact.VCardKey.URL

/**
 * The Card these properties of one vCard describe, mapped as RFC 9555 does, or null when it has neither a
 * name nor an email address. A Card without KIND is an individual.
 */
internal fun List<VCardProperty>.toCardOrNull(): Card? =
    CardReader(this).card().takeIf { it.name != null || it.emails != null }

/** Reads one vCard. Properties with an empty value count as absent, and unknown properties are ignored. */
private class CardReader(
    private val properties: List<VCardProperty>,
) {
    /** Apple's labels by group, without the `_$!<...>!$_` wrapper of its built-in labels. */
    private val labels: Map<String, String> =
        named(AB_LABEL)
            .mapNotNull { label ->
                label.group?.let { it to label.text.removeSurrounding(APPLE_LABEL_START, APPLE_LABEL_END) }
            }.toMap()

    fun card(): Card =
        Card(
            kind = (first(KIND) ?: first(APPLE_KIND))?.let { CardKind(it.text.lowercase()) } ?: CardKind.Individual,
            members = named(MEMBER, APPLE_MEMBER).associate { it.text to true }.ifEmpty { null },
            uid = first(UID)?.text,
            name = readName(first(FN)?.text, first(N)),
            nicknames = named(NICKNAME).flatMap { it.textList }.map { Nickname(it) }.keyed("k"),
            organizations = named(ORG).mapNotNull { it.toOrganization() }.keyed("o"),
            titles = named(TITLE, ROLE).map { it.toTitle() }.keyed("t"),
            emails = named(EMAIL).map { it.toEmail(labelOf(it)) }.keyed("e"),
            phones = named(TEL).map { it.toPhone(labelOf(it)) }.keyed("p"),
            addresses = named(ADR).mapNotNull { it.toAddress() }.keyed("a"),
            links = named(URL).map { it.toLink(labelOf(it)) }.keyed("l"),
            media = named(PHOTO).map { it.toPhoto() }.keyed("m"),
            anniversaries = namedIn(ANNIVERSARY_PROPERTIES.keys).mapNotNull { it.toAnniversary() }.keyed("d"),
            notes = named(NOTE).map { Note(it.text) }.keyed("n"),
        )

    private fun named(vararg names: String): List<VCardProperty> = namedIn(names.asList())

    private fun namedIn(names: Collection<String>): List<VCardProperty> =
        properties.filter { it.name in names && it.text.isNotEmpty() }

    private fun first(name: String): VCardProperty? = named(name).firstOrNull()

    private fun labelOf(property: VCardProperty): String? = property.group?.let { labels[it] }
}

private fun VCardProperty.toEmail(label: String?): EmailAddress =
    EmailAddress(address = text, contexts = types.mapTypes(CONTEXT_TYPES), pref = pref, label = label)

/** RFC 9555 sets features only for a TEL-specific TYPE, so a plain TEL has none. */
private fun VCardProperty.toPhone(label: String?): Phone =
    Phone(
        number = text,
        features = types.mapTypes(FEATURE_TYPES),
        contexts = types.mapTypes(CONTEXT_TYPES),
        pref = pref,
        label = label,
    )

private fun VCardProperty.toLink(label: String?): Link =
    Link(uri = text, contexts = types.mapTypes(CONTEXT_TYPES), pref = pref, label = label)

/** The organization name and its units, from the first ORG component on, or null when all are empty. */
private fun VCardProperty.toOrganization(): Organization? {
    val parts = value.splitUnescaped(';').map { it.unescapeText().trim() }
    val units = parts.drop(1).filter { it.isNotEmpty() }.map { OrgUnit(it) }
    return Organization(name = parts.first().ifEmpty { null }, units = units.ifEmpty { null })
        .takeIf { it.name != null || it.units != null }
}

private fun VCardProperty.toTitle(): Title =
    Title(name = text, kind = if (name == ROLE) TitleKind.Role else TitleKind.Title)

/**
 * A photo as a URI. vCard 3.0 writes the image inline as base64 with its format in TYPE, such as
 * `PHOTO;ENCODING=b;TYPE=JPEG:...`, which becomes a `data:` URI.
 */
private fun VCardProperty.toPhoto(): Media =
    Media(
        kind = MediaKind.Photo,
        uri = if (isEncoded(BASE64, BASE64_SHORT)) inlineDataUri() else text,
        mediaType = single(MEDIATYPE),
        contexts = types.mapTypes(CONTEXT_TYPES),
        pref = pref,
    )

private fun VCardProperty.inlineDataUri(): String {
    val format = types.firstOrNull { it !in INLINE_PHOTO_TYPES }
    val mediaType = format?.let { if ('/' in it) it else "image/$it" } ?: UNKNOWN_MEDIA_TYPE
    return "data:$mediaType;base64,${value.filterNot { it.isWhitespace() }}"
}

/** TYPE values of an inline photo that do not name its format. */
private val INLINE_PHOTO_TYPES = setOf("pref", "b", "base64", "work", "home")

private const val UNKNOWN_MEDIA_TYPE = "application/octet-stream"
private const val APPLE_LABEL_START = "_\$!<"
private const val APPLE_LABEL_END = ">!\$_"
