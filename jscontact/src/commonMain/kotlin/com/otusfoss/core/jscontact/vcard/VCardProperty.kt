package com.otusfoss.core.jscontact.vcard

import com.otusfoss.core.jscontact.vcard.VCardKey.CHARSET
import com.otusfoss.core.jscontact.vcard.VCardKey.ENCODING
import com.otusfoss.core.jscontact.vcard.VCardKey.PREF
import com.otusfoss.core.jscontact.vcard.VCardKey.QUOTED_PRINTABLE
import com.otusfoss.core.jscontact.vcard.VCardKey.TYPE

/**
 * One content line of a vCard, `group.NAME;KEY=value,value:value` (RFC 6350, section 3.3). The name, the
 * group, and the parameter keys are uppercase, because vCard compares them ignoring case.
 */
internal data class VCardProperty(
    val name: String,
    /** The value as written, with its backslash escapes. A quoted-printable value is already decoded. */
    val value: String,
    /** The values of each parameter. Repeated parameters, such as `TYPE=work;TYPE=pref`, are merged. */
    val parameters: Map<String, List<String>> = emptyMap(),
    val group: String? = null,
) {
    /**
     * The TYPE values in lowercase, so `TYPE=WORK;TYPE=pref`, `TYPE=work,PREF`, and the quoted list
     * `TYPE="work,pref"` of RFC 6350 read the same.
     */
    val types: Set<String>
        get() = parameter(TYPE).flatMap { it.split(',') }.map { it.trim().lowercase() }.toSet()

    /**
     * The PREF parameter when it is from 1 to 100 (RFC 6350, section 5.3), or 1 when TYPE holds `pref`, as
     * vCard 3.0 marks a preferred value (RFC 2426, section 3.3.1).
     */
    val pref: Int?
        get() =
            parameter(PREF).firstNotNullOfOrNull { it.trim().toIntOrNull() }?.takeIf { it in 1..MAX_PREF }
                ?: 1.takeIf { "pref" in types }

    /** The value with its escapes resolved and its outer whitespace trimmed. */
    val text: String
        get() = value.unescapeText().trim()

    fun parameter(key: String): List<String> = parameters[key].orEmpty()

    /** The values of [key] joined again, for a parameter that holds one value with commas, such as LABEL. */
    fun single(key: String): String? = parameters[key]?.joinToString(",")

    /** Whether ENCODING, or a bare vCard 2.1 parameter, names one of [encodings], ignoring case. */
    fun isEncoded(vararg encodings: String): Boolean =
        (parameter(ENCODING) + parameter(TYPE)).any { encoding ->
            encodings.any { encoding.equals(it, ignoreCase = true) }
        }
}

/** Parses one unfolded content line, or returns null when it has no name before a colon outside quotes. */
internal fun String.toVCardProperty(): VCardProperty? = indexOfUnquoted(':').takeIf { it > 0 }?.let { propertyAt(it) }

private fun String.propertyAt(colon: Int): VCardProperty {
    val head = substring(0, colon).splitUnquoted(';')
    val qualifiedName = head.first().trim().uppercase()
    val property =
        VCardProperty(
            name = qualifiedName.substringAfterLast('.'),
            value = substring(colon + 1),
            parameters =
                head
                    .drop(1)
                    .filter { it.isNotBlank() }
                    .map { it.toParameter() }
                    .groupBy({ it.first }, { it.second })
                    .mapValues { (_, values) -> values.flatten() },
            group = qualifiedName.substringBeforeLast('.', "").ifEmpty { null },
        )
    return if (property.isEncoded(QUOTED_PRINTABLE)) {
        property.copy(value = property.value.decodeQuotedPrintable(property.single(CHARSET)))
    } else {
        property
    }
}

/** `KEY=a,b` as KEY with its values. A bare value, as vCard 2.1 writes `TEL;WORK;VOICE:...`, is a TYPE. */
private fun String.toParameter(): Pair<String, List<String>> =
    if ('=' in this) {
        substringBefore('=').trim().uppercase() to
            substringAfter('=').splitUnquoted(',').map { it.trim().removeSurrounding("\"").decodeCarets() }
    } else {
        TYPE to listOf(trim())
    }

private fun String.indexOfUnquoted(target: Char): Int {
    var quoted = false
    return indexOfFirst { char ->
        if (char == '"') quoted = !quoted
        char == target && !quoted
    }
}

private fun String.splitUnquoted(delimiter: Char): List<String> {
    val text = this
    return buildList {
        val part = StringBuilder()
        var quoted = false
        for (char in text) {
            if (char == delimiter && !quoted) {
                add(part.toString())
                part.clear()
            } else {
                if (char == '"') quoted = !quoted
                part.append(char)
            }
        }
        add(part.toString())
    }
}

/** This property as one content line, folded at 75 octets. [VCardProperty.value] must already be escaped. */
internal fun VCardProperty.toContentLine(): String {
    val property = this
    return buildString {
        if (property.group != null) append(property.group).append('.')
        append(property.name)
        property.parameters.forEach { (key, values) ->
            append(';').append(key).append('=').append(values.joinToString(",") { it.toParameterValue() })
        }
        append(':').append(property.value)
    }.folded()
}

/** A parameter value with caret escapes, quoted when it holds a character that would end it. */
private fun String.toParameterValue(): String {
    val encoded = encodeCarets()
    return if (encoded.any { it in PARAMETER_DELIMITERS }) "\"$encoded\"" else encoded
}

private const val MAX_PREF = 100
private const val PARAMETER_DELIMITERS = ":;,"
