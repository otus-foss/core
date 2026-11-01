package com.otus.jscontact.vcard

import com.otus.jscontact.Card
import com.otus.jscontact.MediaKind
import com.otus.jscontact.Organization
import com.otus.jscontact.Title
import com.otus.jscontact.TitleKind
import com.otus.jscontact.VCard
import com.otus.jscontact.vcard.VCardKey.AB_LABEL
import com.otus.jscontact.vcard.VCardKey.BEGIN
import com.otus.jscontact.vcard.VCardKey.EMAIL
import com.otus.jscontact.vcard.VCardKey.END
import com.otus.jscontact.vcard.VCardKey.KIND
import com.otus.jscontact.vcard.VCardKey.MEDIATYPE
import com.otus.jscontact.vcard.VCardKey.MEMBER
import com.otus.jscontact.vcard.VCardKey.NICKNAME
import com.otus.jscontact.vcard.VCardKey.NOTE
import com.otus.jscontact.vcard.VCardKey.ORG
import com.otus.jscontact.vcard.VCardKey.PHOTO
import com.otus.jscontact.vcard.VCardKey.ROLE
import com.otus.jscontact.vcard.VCardKey.TEL
import com.otus.jscontact.vcard.VCardKey.TITLE
import com.otus.jscontact.vcard.VCardKey.UID
import com.otus.jscontact.vcard.VCardKey.URL
import com.otus.jscontact.vcard.VCardKey.VCARD
import com.otus.jscontact.vcard.VCardKey.VERSION
import com.otus.jscontact.vcard.VCardKey.WRITTEN_VERSION

/** This Card as one vCard 4.0 entry, each content line folded and ended with CRLF. */
internal fun Card.toVCard(): String = vCardProperties().joinToString("") { it.toContentLine() + CRLF }

/**
 * The properties [VCard.write] writes, in the order RFC 6350 lists them. Text values are escaped. URIs, such
 * as MEMBER, PHOTO, and URL values, are not escaped, because vCard 4.0 does not escape URIs, but their control
 * characters are percent-encoded so that a line break cannot start a new content line (RFC 6350, section 3.2).
 * A KIND that is not a vCard token is left out.
 */
private fun Card.vCardProperties(): List<VCardProperty> =
    buildList {
        add(VCardProperty(BEGIN, VCARD))
        add(VCardProperty(VERSION, WRITTEN_VERSION))
        if (uid != null) add(VCardProperty(UID, uid.escapeText()))
        if (kind != null && KIND_TOKEN.matches(kind.value)) add(VCardProperty(KIND, kind.value))
        add(fullNameProperty())
        addAll(listOfNotNull(name?.toNProperty()))
        addAll(nicknames.orEmpty().values.map { VCardProperty(NICKNAME, it.name.escapeText()) })
        addAll(organizations.orEmpty().values.map { it.toOrgProperty() })
        addAll(titles.orEmpty().values.map { it.toTitleProperty() })
        addAll(emailProperties())
        addAll(phoneProperties())
        addAll(addresses.orEmpty().values.map { it.toAdrProperty() })
        addAll(anniversaries.orEmpty().values.mapNotNull { it.toDateProperty() })
        addAll(notes.orEmpty().values.map { VCardProperty(NOTE, it.note.escapeText()) })
        addAll(
            members
                .orEmpty()
                .filterValues { it }
                .keys
                .map { VCardProperty(MEMBER, it.uriValue()) },
        )
        addAll(photoProperties())
        addAll(linkProperties())
        add(VCardProperty(END, VCARD))
    }

private fun Card.emailProperties(): List<VCardProperty> =
    emails.orEmpty().values.flatMapIndexed { index, email ->
        val parameters = vCardParameters(email.contexts.typesIn(CONTEXT_TYPES), email.pref)
        VCardProperty(EMAIL, email.address.escapeText(), parameters)
            .labelled("email${index + 1}", email.label)
    }

private fun Card.phoneProperties(): List<VCardProperty> =
    phones.orEmpty().values.flatMapIndexed { index, phone ->
        val types = phone.features.typesIn(FEATURE_TYPES) + phone.contexts.typesIn(CONTEXT_TYPES)
        VCardProperty(TEL, phone.number.escapeText(), vCardParameters(types, phone.pref))
            .labelled("tel${index + 1}", phone.label)
    }

private fun Card.linkProperties(): List<VCardProperty> =
    links.orEmpty().values.flatMapIndexed { index, link ->
        VCardProperty(URL, link.uri.uriValue(), vCardParameters(link.contexts.typesIn(CONTEXT_TYPES), link.pref))
            .labelled("url${index + 1}", link.label)
    }

/** Photos with a URI. A photo that only has a JMAP blobId has no value vCard can hold. */
private fun Card.photoProperties(): List<VCardProperty> =
    media
        .orEmpty()
        .values
        .filter { it.kind == MediaKind.Photo && it.uri.isNotEmpty() }
        .map { photo ->
            val types = photo.contexts.typesIn(CONTEXT_TYPES)
            VCardProperty(PHOTO, photo.uri.uriValue(), vCardParameters(types, photo.pref, MEDIATYPE to photo.mediaType))
        }

private fun Organization.toOrgProperty(): VCardProperty =
    VCardProperty(ORG, (listOf(name.orEmpty()) + units.orEmpty().map { it.name }).joinToString(";") { it.escapeText() })

private fun Title.toTitleProperty(): VCardProperty =
    VCardProperty(if (kind == TitleKind.Role) ROLE else TITLE, name.escapeText())

/** This property, and when [label] is set, Apple's X-ABLabel for it in a shared [group]. */
private fun VCardProperty.labelled(
    group: String,
    label: String?,
): List<VCardProperty> =
    if (label == null) {
        listOf(this)
    } else {
        listOf(copy(group = group), VCardProperty(AB_LABEL, label.escapeText(), group = group))
    }

/** This URI with its control characters, such as CR and LF, percent-encoded, as a URI cannot hold them raw. */
private fun String.uriValue(): String =
    replace(CONTROL_CHARACTER) {
        "%" +
            it.value
                .single()
                .code
                .toString(HEX)
                .uppercase()
                .padStart(2, '0')
    }

/** An iana-token or x-name, the forms a KIND value takes besides the registered names (RFC 6350, section 6.1.4). */
private val KIND_TOKEN = Regex("[A-Za-z0-9-]+")

private val CONTROL_CHARACTER = Regex("[\\u0000-\\u001F\\u007F]")

private const val HEX = 16
