package com.otus.jscontact

import com.otus.jscontact.VCardKey.BEGIN
import com.otus.jscontact.VCardKey.END
import com.otus.jscontact.VCardKey.VCARD

/**
 * Converts between vCard files and [Card]s, mapping properties as RFC 9555 describes. It reads vCard 3.0
 * (RFC 2426) and 4.0 (RFC 6350), and the quoted-printable values of vCard 2.1, and writes vCard 4.0.
 *
 * It maps FN, N, NICKNAME, EMAIL, TEL, ORG, TITLE, ROLE, BDAY, ANNIVERSARY, DEATHDATE, NOTE, ADR, UID, KIND,
 * MEMBER, PHOTO, and URL, plus Apple's X-ABLabel labels and its vCard 3.0 group properties. Reading ignores
 * other properties, so a file read and written again loses them.
 */
public object VCard {
    /**
     * Reads every `BEGIN:VCARD` to `END:VCARD` entry of [text], which may hold any number of them. Lines may
     * end in CRLF or LF, and folded lines are unfolded. Names and parameter keys match ignoring case, and
     * `group.` prefixes are read. TYPE values may be lists, as in `TYPE=work,pref`, or repeated, as in
     * `TYPE=WORK;TYPE=PREF`.
     *
     * An entry becomes a Card when it has any mapped property besides KIND and UID. Every other entry, and
     * one without `END:VCARD`, becomes a [VCardFailure] instead, so one bad entry never fails the file. An FN
     * marked `DERIVED=TRUE` is not read as a name. Entries in an `Id[X]` map get the keys `e1`, `e2`, and so
     * on, in file order. Without FN, the full name joins the N components, and without KIND, the Card is an
     * individual.
     */
    public fun parse(text: String): VCardImport {
        val entries = text.vCardEntries().map { entry -> entry to entry.properties.toCardOrNull() }
        return VCardImport(
            cards = entries.mapNotNull { (entry, card) -> card.takeIf { entry.isClosed } },
            failures =
                entries.mapIndexedNotNull { index, (entry, card) ->
                    entry.failureReason(card)?.let { VCardFailure(position = index + 1, reason = it) }
                },
        )
    }

    /**
     * Writes [cards] as vCard 4.0 entries, one after another, with CRLF line endings, lines folded at 75
     * octets, and text values escaped. [parse] reads the result back to the same mapped properties.
     *
     * A Card without a name gets its organization, email address, or phone number as FN, which vCard 4.0
     * requires, marked `DERIVED=TRUE` so that [parse] reads no name back. The JSContact properties that
     * [parse] does not read are not written.
     */
    public fun write(cards: List<Card>): String = cards.joinToString("") { it.toVCard() }
}

/** What [VCard.parse] read from a file. */
public data class VCardImport(
    /** The Cards read, in file order. */
    val cards: List<Card>,
    /** The entries that did not become a Card, in file order. */
    val failures: List<VCardFailure>,
)

/** A vCard entry that [VCard.parse] could not turn into a Card. */
public data class VCardFailure(
    /** The 1-based position of the entry among the file's `BEGIN:VCARD` lines. */
    val position: Int,
    /** Why the entry was skipped, as a sentence to show people, such as "It has no contact details." */
    val reason: String,
)

/** The properties between one `BEGIN:VCARD` and its `END:VCARD`, without either. */
private class VCardEntry(
    val properties: List<VCardProperty>,
    /** False when the file ended, or another `BEGIN:VCARD` came, before `END:VCARD`. */
    val isClosed: Boolean,
)

private fun String.vCardEntries(): List<VCardEntry> =
    buildList {
        var open: MutableList<VCardProperty>? = null
        for (property in vCardLines().mapNotNull { it.toVCardProperty() }) {
            val current = open
            when {
                property.marks(BEGIN) -> {
                    if (current != null) add(VCardEntry(current, isClosed = false))
                    open = mutableListOf()
                }

                property.marks(END) -> {
                    if (current != null) add(VCardEntry(current, isClosed = true))
                    open = null
                }

                else -> {
                    current?.add(property)
                }
            }
        }
        val unclosed = open
        if (unclosed != null) add(VCardEntry(unclosed, isClosed = false))
    }

private fun VCardProperty.marks(boundary: String): Boolean =
    name == boundary && value.trim().equals(VCARD, ignoreCase = true)

private fun VCardEntry.failureReason(card: Card?): String? =
    when {
        !isClosed -> "It has no END:VCARD line."
        card == null -> "It has no contact details."
        else -> null
    }
