package com.otus.jscontact.validation

import com.otus.jscontact.TYPE_PROPERTY
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.JsonElement

/** Reports [value] at member [token] unless it is a URI as defined in section 3 of RFC 3986. */
internal fun ValidationScope.uri(
    value: String?,
    token: String,
) {
    ensure(value == null || URI.matches(value), "Not a URI (RFC 3986, section 3; RFC 9553, section 1.6.2)", token)
}

/** Reports [value] at member [token] unless it has the syntax of a language tag (RFC 5646, section 2.1). */
internal fun ValidationScope.languageTag(
    value: String?,
    token: String,
) {
    ensure(value == null || LANGUAGE_TAG.matches(value), "Not a language tag (RFC 5646, section 2.1)", token)
}

/**
 * Reports the enumerated [value] at member [token] when it differs from one of the [registered] values only
 * in case (RFC 9553, section 1.7.1), or has a vendor prefix but is not a `v-extension` (section 1.8.2).
 * Other unknown values pass, because later minor versions register more of them (section 1.9).
 */
internal fun ValidationScope.enumValue(
    value: String?,
    registered: Set<String>,
    token: String,
) {
    val problem =
        when {
            value == null || value in registered -> null
            registered.any { it.equals(value, ignoreCase = true) } -> VALUE_DIFFERS_IN_CASE
            ':' in value && !V_EXTENSION.matches(value) -> INVALID_VENDOR_VALUE
            else -> null
        }
    if (problem != null) report("\"$value\" $problem", token)
}

/** Checks each of [keys], the enumerated keys of the map at member [token], as [enumValue] does. */
internal fun ValidationScope.enumKeys(
    token: String,
    keys: Collection<String>?,
    registered: Set<String>,
) {
    member(token, keys) { values -> values.forEach { enumValue(it, registered, it) } }
}

/**
 * Reports each name in [extra] that is reserved (RFC 9553, section 1.7.3), that differs only in case from a
 * property [serializer] declares (section 1.7.1), or that is neither in the syntax of registered names
 * (section 1.7.2) nor a vendor-specific `v-extension` (section 1.8.1). Values pass unchecked, because
 * unknown and vendor-specific values must be kept as they are (sections 1.7.4 and 1.8.1).
 */
internal fun ValidationScope.propertyNames(
    extra: Map<String, JsonElement>,
    serializer: KSerializer<*>,
) {
    val declared = serializer.descriptor.elementNames.toSet() + TYPE_PROPERTY
    extra
        .keys
        .filterNot { it in declared }
        .forEach { name -> propertyNameProblem(name, declared)?.let { report(it, name) } }
}

private fun propertyNameProblem(
    name: String,
    declared: Set<String>,
): String? =
    when {
        ':' in name -> if (V_EXTENSION.matches(name)) null else INVALID_VENDOR_NAME
        name == RESERVED -> RESERVED_NAME
        (declared + RESERVED).any { it.equals(name, ignoreCase = true) } -> NAME_DIFFERS_IN_CASE
        !REGISTERED_NAME.matches(name) -> UNREGISTERED_NAME
        else -> null
    }

private const val VALUE_DIFFERS_IN_CASE = "differs from a registered value only in case (RFC 9553, section 1.7.1)"

private const val INVALID_VENDOR_VALUE = "is not a valid vendor-specific value (RFC 9553, section 1.8.2)"

private const val INVALID_VENDOR_NAME = "Not a vendor-specific property name (RFC 9553, section 1.8.1)"

private const val RESERVED_NAME = "The property name \"extra\" is reserved (RFC 9553, section 1.7.3)"

private const val NAME_DIFFERS_IN_CASE = "Differs from a property name only in case (RFC 9553, section 1.7.1)"

private const val UNREGISTERED_NAME =
    "Neither a registered nor a vendor-specific property name (RFC 9553, sections 1.7.2 and 1.8.1)"

private const val RESERVED = "extra"

// RFC 9553, section 1.7.2: ASCII letters, digits, and "@", in lower camel case.
private val REGISTERED_NAME = Regex("[a-z@][A-Za-z0-9@]*")

/**
 * NON-ASCII (RFC 6350, section 3.3): any code point from U+0080. The regex engines match by code point, and a
 * negated class also matches the code points above U+FFFF, such as emoji, which a range up to \uFFFF misses.
 */
internal const val NON_ASCII: String = """[^\x00-\x7F]"""

// alnum-int (RFC 9553, figure 2): an ASCII letter or digit, or NON-ASCII.
private const val ALNUM_INT = """(?:[A-Za-z0-9]|$NON_ASCII)"""

// v-label (RFC 9553, figure 2): alnum-int characters and hyphens, starting and ending with alnum-int.
private const val V_LABEL = """$ALNUM_INT(?:(?:[A-Za-z0-9-]|$NON_ASCII)*$ALNUM_INT)?"""

// v-name (RFC 9553, figure 2): any characters but controls other than HTAB, '"', '/', '~', and DEL.
private const val V_NAME = """[^\x00-\x08\x0A-\x1F"/~\x7F]+"""

// v-extension (RFC 9553, figure 2): a prefix of dot-separated labels, a colon, and a name.
private val V_EXTENSION = Regex("""$V_LABEL(?:\.$V_LABEL)*:$V_NAME""")

// A scheme, a colon, then only URI characters, with "%" always starting a percent-encoded octet.
private val URI = Regex("""[A-Za-z][A-Za-z0-9+.-]*:(?:[A-Za-z0-9\-._~:/?#\[\]@!$&'()*+,;=]|%[0-9A-Fa-f]{2})*""")

// The well-formed shape of RFC 5646 tags: subtags of up to 8 ASCII letters or digits, the first all letters.
private val LANGUAGE_TAG = Regex("[A-Za-z]{1,8}(?:-[A-Za-z0-9]{1,8})*")
