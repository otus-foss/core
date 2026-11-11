package com.otusfoss.core.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

/**
 * A company or organization associated with the entity, as a value of the Card `organizations` map
 * (RFC 9553, section 2.2.3). At least one of [name] and [units] must be set.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Organization.Serializer::class)
public data class Organization(
    val name: String? = null,
    /** The units, highest in the hierarchy first. When set, the list has at least one unit. */
    val units: List<OrgUnit>? = null,
    /** The verbatim string to compare when sorting organizations, in place of [name]. */
    val sortAs: String? = null,
    /** Where the association applies, such as a choir membership in private life only (section 1.5.1). */
    val contexts: Map<ContactContext, Boolean>? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<Organization>(
        generatedSerializer(),
        "Organization",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/** A unit of an [Organization], such as a division or a department (RFC 9553, section 2.2.3). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = OrgUnit.Serializer::class)
public data class OrgUnit(
    val name: String,
    /** The verbatim string to compare when sorting units of the same level, in place of [name]. */
    val sortAs: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<OrgUnit>(
        generatedSerializer(),
        "OrgUnit",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}
