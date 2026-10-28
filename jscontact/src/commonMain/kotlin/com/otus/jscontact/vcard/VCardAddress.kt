package com.otus.jscontact.vcard

import com.otus.jscontact.Address
import com.otus.jscontact.AddressComponent
import com.otus.jscontact.AddressComponentKind
import com.otus.jscontact.vcard.VCardKey.ADR
import com.otus.jscontact.vcard.VCardKey.CC
import com.otus.jscontact.vcard.VCardKey.GEO
import com.otus.jscontact.vcard.VCardKey.LABEL
import com.otus.jscontact.vcard.VCardKey.TZ

/**
 * The [Address] of an ADR property (RFC 9555, Table 2), or null when it holds nothing. The seven RFC 6350
 * components map to postOfficeBox, apartment, name, locality, region, postcode, and country. LABEL, GEO, TZ,
 * and CC become [Address.full], [Address.coordinates], [Address.timeZone], and [Address.countryCode].
 */
internal fun VCardProperty.toAddress(): Address? {
    val components =
        value.splitUnescaped(';').zip(ADR_KINDS).flatMap { (slot, kind) ->
            slot
                .splitUnescaped(',')
                .map { it.unescapeText().trim() }
                .filter { it.isNotEmpty() }
                .map { AddressComponent(it, kind) }
        }
    return Address(
        components = components.ifEmpty { null },
        countryCode = single(CC),
        coordinates = single(GEO),
        timeZone = single(TZ),
        contexts = types.mapTypes(CONTEXT_TYPES),
        full = single(LABEL),
        pref = pref,
    ).takeIf { listOfNotNull(it.components, it.countryCode, it.coordinates, it.timeZone, it.full).isNotEmpty() }
}

/**
 * This address as an ADR property with the seven RFC 6350 components. Room, floor, and building join the
 * apartment, and the other street kinds join the street, where a number and a street name become one value.
 * Separators are dropped.
 */
internal fun Address.toAdrProperty(): VCardProperty {
    val parts = components.orEmpty()
    val value =
        ADR_KINDS.indices.joinToString(";") { slot ->
            parts.filter { it.kind.adrSlot() == slot }.slotValues(slot).joinToString(",") { it.escapeText() }
        }
    val parameters =
        vCardParameters(
            contexts.typesIn(CONTEXT_TYPES),
            pref,
            LABEL to full,
            GEO to coordinates,
            TZ to timeZone,
            CC to countryCode,
        )
    return VCardProperty(ADR, value, parameters)
}

private fun AddressComponentKind.adrSlot(): Int? =
    when (this) {
        AddressComponentKind.Separator -> null
        in APARTMENT_KINDS -> APARTMENT_SLOT
        else -> ADR_KINDS.indexOf(this).takeIf { it >= 0 } ?: STREET_SLOT
    }

private fun List<AddressComponent>.slotValues(slot: Int): List<String> =
    if (slot == STREET_SLOT && any { it.kind == AddressComponentKind.Number }) {
        listOf(joinToString(" ") { it.value })
    } else {
        map { it.value }
    }

private val ADR_KINDS =
    listOf(
        AddressComponentKind.PostOfficeBox,
        AddressComponentKind.Apartment,
        AddressComponentKind.Name,
        AddressComponentKind.Locality,
        AddressComponentKind.Region,
        AddressComponentKind.Postcode,
        AddressComponentKind.Country,
    )

private val APARTMENT_KINDS =
    setOf(AddressComponentKind.Room, AddressComponentKind.Floor, AddressComponentKind.Building)

private const val APARTMENT_SLOT = 1
private const val STREET_SLOT = 2
