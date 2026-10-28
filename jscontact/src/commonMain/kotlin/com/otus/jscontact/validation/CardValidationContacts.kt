package com.otus.jscontact.validation

import com.otus.jscontact.EmailAddress
import com.otus.jscontact.LanguagePref
import com.otus.jscontact.OnlineService
import com.otus.jscontact.Phone
import com.otus.jscontact.isEmailAddress

/** Validates an EmailAddress, whose address is an RFC 5322 addr-spec (RFC 9553, section 2.3.1). */
internal fun ValidationScope.checkEmail(email: EmailAddress) {
    propertyNames(email.extra, EmailAddress.serializer())
    ensure(isEmailAddress(email.address), "Not an addr-spec (RFC 5322, section 3.4.1)", "address")
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
