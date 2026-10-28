package com.otus.jscontact.validation

import com.otus.jscontact.ContactContext

/** Reports a [pref] outside 1 to 100, although its UnsignedInt type allows more (RFC 9553, section 1.5.3). */
internal fun ValidationScope.pref(pref: Int?) {
    ensure(pref == null || pref in 1..MAX_PREF, "pref must be from 1 to 100 (RFC 9553, section 1.5.3)", "pref")
}

/** Reports a [listAs] that is not above zero (RFC 9553, sections 2.6.2 and 2.8.4) or not an UnsignedInt. */
internal fun ValidationScope.listAs(listAs: Long?) {
    ensure(
        listAs == null || listAs in 1..MAX_UNSIGNED_INT,
        "listAs must be above zero (RFC 9553, sections 2.6.2 and 2.8.4)",
        "listAs",
    )
}

/**
 * Reports each key of the map at member [token] that is not an Id: 1 to 255 characters of the base64url
 * alphabet without padding (RFC 9553, section 1.4.1, and RFC 8620, section 1.2, which [source] cites).
 */
internal fun ValidationScope.idKeys(
    token: String,
    map: Map<String, *>?,
    source: String,
) {
    member(token, map) { values -> values.keys.filterNot(ID::matches).forEach { report("Not an Id ($source)", it) } }
}

/** Validates an `Id[X]` map at member [token]: its keys with [idKeys], and each value with [check]. */
internal fun <T : Any> ValidationScope.idMap(
    token: String,
    values: Map<String, T>?,
    check: ValidationScope.(T) -> Unit,
) {
    idKeys(token, values, "RFC 9553, section 1.4.1")
    entries(token, values, check)
}

/** Reports each `false` in the set at member [token], whose values must all be `true` as [source] says. */
internal fun ValidationScope.trueSet(
    token: String,
    set: Map<String, Boolean>?,
    source: String,
) {
    member(token, set) { values ->
        values.filterValues { !it }.keys.forEach { report("A set value must be true ($source)", it) }
    }
}

/** Validates a set at member [token] whose keys are enumerated values and whose values must be `true`. */
internal fun ValidationScope.enumSet(
    token: String,
    set: Map<String, Boolean>?,
    registered: Set<String>,
    source: String,
) {
    enumKeys(token, set?.keys, registered)
    trueSet(token, set, source)
}

/**
 * Validates the `contexts` of any type but Address (RFC 9553, section 1.5.1). The values may be `false`,
 * since only Address requires `true`, but `billing` and `delivery` are contexts of addresses only.
 */
internal fun ValidationScope.contexts(contexts: Map<ContactContext, Boolean>?) {
    val keys = contexts?.keys?.map { it.value }
    enumKeys(CONTEXTS, keys, RegisteredValues.contexts)
    member(CONTEXTS, keys) { values ->
        values
            .filter { it in ADDRESS_CONTEXTS }
            .forEach { context -> report("\"$context\" is a context of addresses only ($ADDRESS_SECTION)", context) }
    }
}

/** Validates the `contexts` of an Address, a set whose values must be `true` (RFC 9553, section 2.5.1.1). */
internal fun ValidationScope.addressContexts(contexts: Map<ContactContext, Boolean>?) {
    enumSet(CONTEXTS, contexts?.mapKeys { it.key.value }, RegisteredValues.contexts, ADDRESS_SECTION)
}

/** An Id (RFC 9553, section 1.4.1) or a JMAP Id (RFC 8620, section 1.2), which share one syntax. */
internal val ID: Regex = Regex("[A-Za-z0-9_-]{1,255}")

/** The largest UnsignedInt, 2^53 - 1 (RFC 9553, section 1.4.2). */
internal const val MAX_UNSIGNED_INT: Long = 9_007_199_254_740_991L

private const val MAX_PREF = 100

private const val CONTEXTS = "contexts"

private val ADDRESS_CONTEXTS = setOf(ContactContext.Billing.value, ContactContext.Delivery.value)
