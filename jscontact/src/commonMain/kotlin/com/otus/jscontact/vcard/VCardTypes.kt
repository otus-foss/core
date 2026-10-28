package com.otus.jscontact.vcard

import com.otus.jscontact.AnniversaryKind
import com.otus.jscontact.ContactContext
import com.otus.jscontact.PhoneFeature
import com.otus.jscontact.vcard.VCardKey.ANNIVERSARY
import com.otus.jscontact.vcard.VCardKey.BDAY
import com.otus.jscontact.vcard.VCardKey.DEATHDATE
import com.otus.jscontact.vcard.VCardKey.PREF
import com.otus.jscontact.vcard.VCardKey.TYPE

/** The TYPE values that map to a context (RFC 9555). vCard `home` is JSContact `private`. */
internal val CONTEXT_TYPES: Map<String, ContactContext> =
    mapOf("work" to ContactContext.Work, "home" to ContactContext.Private)

/** The TEL TYPE values that map to a phone feature (RFC 9555, Table 3). */
internal val FEATURE_TYPES: Map<String, PhoneFeature> =
    mapOf(
        "cell" to PhoneFeature.Mobile,
        "voice" to PhoneFeature.Voice,
        "fax" to PhoneFeature.Fax,
        "pager" to PhoneFeature.Pager,
        "text" to PhoneFeature.Text,
        "video" to PhoneFeature.Video,
        "textphone" to PhoneFeature.Textphone,
        "main-number" to PhoneFeature.MainNumber,
    )

/** The date properties and the anniversary kind each maps to (RFC 9555). */
internal val ANNIVERSARY_PROPERTIES: Map<String, AnniversaryKind> =
    mapOf(BDAY to AnniversaryKind.Birth, ANNIVERSARY to AnniversaryKind.Wedding, DEATHDATE to AnniversaryKind.Death)

/** The values of [table] that these TYPE values name, as a JSContact set, or null when none match. */
internal fun <T : Any> Set<String>.mapTypes(table: Map<String, T>): Map<T, Boolean>? =
    mapNotNull { table[it] }.associateWith { true }.ifEmpty { null }

/** The TYPE values of [table] for the members of this JSContact set. */
internal fun <T : Any> Map<T, Boolean>?.typesIn(table: Map<String, T>): List<String> =
    orEmpty().filterValues { it }.keys.mapNotNull { table.keyOf(it) }

internal fun <K, V> Map<K, V>.keyOf(value: V): K? = entries.firstOrNull { it.value == value }?.key

/** The TYPE and PREF parameters, followed by each of [others] that has a value. */
internal fun vCardParameters(
    types: List<String>,
    pref: Int?,
    vararg others: Pair<String, String?>,
): Map<String, List<String>> =
    buildMap {
        if (types.isNotEmpty()) put(TYPE, types)
        if (pref != null) put(PREF, listOf(pref.toString()))
        others.forEach { (key, value) -> if (value != null) put(key, listOf(value)) }
    }

/** These values as an `Id[X]` map with the keys [prefix]`1`, [prefix]`2`, and so on, or null when empty. */
internal fun <T> List<T>.keyed(prefix: String): Map<String, T>? =
    mapIndexed { index, value -> "$prefix${index + 1}" to value }.toMap().ifEmpty { null }
