package com.otus.jscontact

/** Validates an EmailAddress, whose address is an RFC 5322 addr-spec (RFC 9553, section 2.3.1). */
internal fun ValidationScope.checkEmail(email: EmailAddress) {
    propertyNames(email.extra, EmailAddress.serializer())
    ensure(ADDR_SPEC.matches(email.address), "Not an addr-spec (RFC 5322, section 3.4.1)", "address")
    contexts(email.contexts)
    pref(email.pref)
}

/** Validates an OnlineService, which needs a uri or a user (RFC 9553, section 2.3.2). */
internal fun ValidationScope.checkOnlineService(service: OnlineService) {
    propertyNames(service.extra, OnlineService.serializer())
    ensure(
        service.uri != null || service.user != null,
        "An OnlineService needs a uri or a user (RFC 9553, section 2.3.2)",
    )
    uri(service.uri, "uri")
    contexts(service.contexts)
    pref(service.pref)
}

/** Validates a Phone, whose features form a set (RFC 9553, section 2.3.3). */
internal fun ValidationScope.checkPhone(phone: Phone) {
    propertyNames(phone.extra, Phone.serializer())
    enumSet(
        "features",
        phone.features?.mapKeys { it.key.value },
        RegisteredValues.phoneFeatures,
        "RFC 9553, section 2.3.3",
    )
    contexts(phone.contexts)
    pref(phone.pref)
}

/** Validates a LanguagePref (RFC 9553, section 2.3.4). */
internal fun ValidationScope.checkLanguagePref(language: LanguagePref) {
    propertyNames(language.extra, LanguagePref.serializer())
    languageTag(language.language, "language")
    contexts(language.contexts)
    pref(language.pref)
}

/** Validates a Calendar (RFC 9553, section 2.4.1). */
internal fun ValidationScope.checkCalendar(calendar: Calendar) {
    propertyNames(calendar.extra, Calendar.serializer())
    enumValue(calendar.kind.value, RegisteredValues.calendarKinds, "kind")
    uri(calendar.uri, "uri")
    resource(calendar)
}

/** Validates a SchedulingAddress (RFC 9553, section 2.4.2). */
internal fun ValidationScope.checkSchedulingAddress(address: SchedulingAddress) {
    propertyNames(address.extra, SchedulingAddress.serializer())
    uri(address.uri, "uri")
    contexts(address.contexts)
    pref(address.pref)
}

/**
 * Validates an Address: at least one of components, coordinates, countryCode, full, and timeZone, the
 * component rules, and the forms of countryCode and coordinates (RFC 9553, section 2.5.1.1). The time zone
 * is not checked, because that needs the IANA Time Zone Database.
 */
internal fun ValidationScope.checkAddress(address: Address) {
    propertyNames(address.extra, Address.serializer())
    ensure(
        listOfNotNull(address.components, address.coordinates, address.countryCode, address.full, address.timeZone)
            .isNotEmpty(),
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

// atext (RFC 5322, section 3.2.3), plus non-ASCII as RFC 6532, section 3.2, allows in internationalized mail.
private const val ATEXT = """(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]|$NON_ASCII)"""

private const val DOT_ATOM = """$ATEXT+(?:\.$ATEXT+)*"""

// quoted-string (RFC 5322, section 3.2.4): quotes around any text but '"', '\', CR, and LF, or a quoted pair.
private const val QUOTED_STRING = """"(?:[^"\\\r\n]|\\.)*""""

// domain-literal (RFC 5322, section 3.4.1): brackets around text without brackets, backslashes, or spaces.
private const val DOMAIN_LITERAL = """\[[^\[\]\\\s]*\]"""

// addr-spec = local-part "@" domain (RFC 5322, section 3.4.1), without the obsolete forms and comments.
private val ADDR_SPEC = Regex("(?:$DOT_ATOM|$QUOTED_STRING)@(?:$DOT_ATOM|$DOMAIN_LITERAL)")

private val COUNTRY_CODE = Regex("[A-Za-z]{2}")
