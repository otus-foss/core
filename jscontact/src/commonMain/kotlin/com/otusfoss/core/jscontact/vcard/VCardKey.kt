package com.otusfoss.core.jscontact.vcard

import com.otusfoss.core.jscontact.VCard

/**
 * The vCard property names, parameter names, and fixed values that [VCard] reads and writes (RFC 6350,
 * RFC 9554, and the Apple extensions that Contacts exports).
 */
internal object VCardKey {
    const val BEGIN = "BEGIN"
    const val END = "END"
    const val VCARD = "VCARD"
    const val VERSION = "VERSION"
    const val WRITTEN_VERSION = "4.0"

    const val FN = "FN"
    const val N = "N"
    const val NICKNAME = "NICKNAME"
    const val EMAIL = "EMAIL"
    const val TEL = "TEL"
    const val ORG = "ORG"
    const val TITLE = "TITLE"
    const val ROLE = "ROLE"
    const val NOTE = "NOTE"
    const val ADR = "ADR"
    const val UID = "UID"
    const val KIND = "KIND"
    const val MEMBER = "MEMBER"
    const val PHOTO = "PHOTO"
    const val URL = "URL"
    const val BDAY = "BDAY"
    const val ANNIVERSARY = "ANNIVERSARY"
    const val DEATHDATE = "DEATHDATE"

    /** Apple's custom label for the property that shares its group, such as `item1.X-ABLabel:Office`. */
    const val AB_LABEL = "X-ABLABEL"

    /** Apple's KIND and MEMBER for groups in vCard 3.0. */
    const val APPLE_KIND = "X-ADDRESSBOOKSERVER-KIND"
    const val APPLE_MEMBER = "X-ADDRESSBOOKSERVER-MEMBER"

    const val TYPE = "TYPE"
    const val PREF = "PREF"
    const val ENCODING = "ENCODING"
    const val CHARSET = "CHARSET"
    const val MEDIATYPE = "MEDIATYPE"

    /** Marks a value made from other properties, such as an FN made from ORG (RFC 9554, section 4.4). */
    const val DERIVED = "DERIVED"
    const val DERIVED_TRUE = "TRUE"
    const val LABEL = "LABEL"
    const val GEO = "GEO"
    const val TZ = "TZ"
    const val CC = "CC"

    /** Apple's marker for a birthday without a year: the placeholder year it wrote in the date. */
    const val OMIT_YEAR = "X-APPLE-OMIT-YEAR"

    const val QUOTED_PRINTABLE = "QUOTED-PRINTABLE"
    const val BASE64 = "BASE64"

    /** The vCard 3.0 name of base64 (RFC 2426, section 5). */
    const val BASE64_SHORT = "B"
}
