package com.otus.jscontact.validation

import com.otus.jscontact.Address
import com.otus.jscontact.AddressComponent

/**
 * Validates an Address: at least one of components, coordinates, countryCode, full, and timeZone, the
 * component rules, and the forms of countryCode and coordinates (RFC 9553, section 2.5.1.1). The time zone
 * is not checked, because that needs the IANA Time Zone Database.
 */
internal fun ValidationScope.checkAddress(address: Address) {
    propertyNames(address.extra, Address.serializer())
    val details =
        listOfNotNull(address.components, address.coordinates, address.countryCode, address.full, address.timeZone)
    ensure(
        details.isNotEmpty(),
        "An Address needs components, coordinates, countryCode, full, or timeZone ($ADDRESS_SECTION)",
    )
    components(address.componentRules())
    ensure(
        address.countryCode == null || COUNTRY_CODE.matches(address.countryCode),
        "Not an ISO 3166-1 alpha-2 country code ($ADDRESS_SECTION)",
        "countryCode",
    )
    val geo = address.coordinates?.takeIf { it.startsWith(GEO_SCHEME, ignoreCase = true) }
    ensure(address.coordinates == geo, "coordinates must be a geo: URI (RFC 5870; $ADDRESS_SECTION)", "coordinates")
    uri(geo, "coordinates")
    addressContexts(address.contexts)
    pref(address.pref)
}

private fun Address.componentRules(): ComponentRules =
    ComponentRules(
        parts = components?.map { ComponentRules.Part(it.kind.value, it.phonetic, it.extra) },
        isOrdered = isOrdered,
        defaultSeparator = defaultSeparator,
        phoneticScript = phoneticScript,
        phoneticSystem = phoneticSystem,
        kinds = RegisteredValues.addressComponentKinds,
        partSerializer = AddressComponent.serializer(),
        section = ADDRESS_SECTION,
    )

/** The section that defines Address, for messages (RFC 9553, section 2.5.1.1). */
internal const val ADDRESS_SECTION: String = "RFC 9553, section 2.5.1.1"

private const val GEO_SCHEME = "geo:"

private val COUNTRY_CODE = Regex("[A-Za-z]{2}")
