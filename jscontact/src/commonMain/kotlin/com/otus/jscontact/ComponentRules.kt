package com.otus.jscontact

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement

/**
 * The parts of a Name or an Address that the component rules read. Both types define the same rules for
 * their components, separators, and phonetics (RFC 9553, sections 2.2.1.1 and 2.5.1.1), so one check
 * covers both.
 */
internal data class ComponentRules(
    /** The components, or null when the object has none. */
    val parts: List<Part>?,
    val isOrdered: Boolean?,
    val defaultSeparator: String?,
    val phoneticScript: String?,
    val phoneticSystem: PhoneticSystem?,
    /** The registered kinds of a component. */
    val kinds: Set<String>,
    /** The serializer of one component, which declares its property names. */
    val partSerializer: KSerializer<*>,
    /** The section that defines the object, for messages. */
    val section: String,
) {
    /** One NameComponent or AddressComponent. */
    data class Part(
        val kind: String,
        val phonetic: String?,
        val extra: Map<String, JsonElement>,
    )
}

/**
 * Reports what breaks the component rules: no component other than separators, separators or a default
 * separator without ordered components, two separators in a row, or a phonetic value without a script or
 * system on the parent (RFC 9553, sections 1.5.4, 2.2.1, and 2.5.1).
 */
internal fun ValidationScope.components(rules: ComponentRules) {
    val parts = rules.parts
    ensure(
        parts == null || parts.any { it.kind != SEPARATOR },
        "Needs a component that is not a separator (${rules.section})",
        COMPONENTS,
    )
    ensure(
        rules.defaultSeparator == null || (parts != null && rules.isOrdered == true),
        "defaultSeparator needs ordered components (${rules.section})",
        "defaultSeparator",
    )
    ensure(
        rules.phoneticScript == null || SCRIPT_SUBTAG.matches(rules.phoneticScript),
        "Not a script subtag (RFC 5646, section 2.2.3; RFC 9553, section 1.5.4)",
        "phoneticScript",
    )
    enumValue(rules.phoneticSystem?.value, RegisteredValues.phoneticSystems, "phoneticSystem")
    items(COMPONENTS, parts) { part -> checkPart(part, rules) }
    member(COMPONENTS, parts) { list ->
        list
            .zipWithNext { first, second -> first.kind == SEPARATOR && second.kind == SEPARATOR }
            .withIndex()
            .filter { it.value }
            .forEach { report("Two separators in a row (${rules.section})", "${it.index + 1}") }
    }
}

private fun ValidationScope.checkPart(
    part: ComponentRules.Part,
    rules: ComponentRules,
) {
    propertyNames(part.extra, rules.partSerializer)
    enumValue(part.kind, rules.kinds, "kind")
    ensure(
        part.kind != SEPARATOR || rules.isOrdered == true,
        "A separator needs ordered components (${rules.section})",
        "kind",
    )
    ensure(
        part.phonetic == null || rules.phoneticScript != null || rules.phoneticSystem != null,
        "phonetic needs a phoneticScript or phoneticSystem on its parent (RFC 9553, section 1.5.4)",
        "phonetic",
    )
}

private const val COMPONENTS = "components"

// The kind "separator" of both NameComponent and AddressComponent (RFC 9553, sections 2.2.1.2 and 2.5.1.2).
private const val SEPARATOR = "separator"

private val SCRIPT_SUBTAG = Regex("[A-Za-z]{4}")
