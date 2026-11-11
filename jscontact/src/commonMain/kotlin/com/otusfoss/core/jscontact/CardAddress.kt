package com.otusfoss.core.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * A postal address or geographic location of the entity, an entry of the Card `addresses` map
 * (RFC 9553, section 2.5.1.1). A valid address sets at least one of [components], [coordinates],
 * [countryCode], [full], or [timeZone]. It has no label. Its [contexts] may also use
 * [ContactContext.Billing] and [ContactContext.Delivery], and every value must be `true`.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Address.Serializer::class)
public data class Address(
    /** The parts of the address. At least one must be of a kind other than [AddressComponentKind.Separator]. */
    val components: List<AddressComponent>? = null,
    /**
     * Whether joining the [components] in order gives a valid full address. Null means `false`, the
     * RFC default. Unordered components have no separators and no [defaultSeparator].
     */
    val isOrdered: Boolean? = null,
    /** The ISO 3166-1 alpha-2 country code, such as `US`. */
    val countryCode: String? = null,
    /** A `geo:` URI (RFC 5870), not a number pair. */
    val coordinates: String? = null,
    /** A time zone name from the IANA Time Zone Database, such as `Europe/Vienna`. */
    val timeZone: String? = null,
    val contexts: Map<ContactContext, Boolean>? = null,
    /** The whole address as one string, for when its components are unknown. */
    val full: String? = null,
    /** What to put between ordered components that have no separator component between them. */
    val defaultSeparator: String? = null,
    val pref: Int? = null,
    /** The RFC 5646 script subtag of the components' phonetic values (section 1.5.4). */
    val phoneticScript: String? = null,
    /** The phonetic system of the components' phonetic values (section 1.5.4). */
    val phoneticSystem: PhoneticSystem? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<Address>(
        generatedSerializer(),
        "Address",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/** One part of an [Address], such as its street name or postcode (RFC 9553, section 2.5.1.2). */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = AddressComponent.Serializer::class)
public data class AddressComponent(
    /** The component's text. A separator's value may be empty. */
    val value: String,
    val kind: AddressComponentKind,
    /** How to pronounce [value]. The parent [Address] then sets its phonetic script or system. */
    val phonetic: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<AddressComponent>(
        generatedSerializer(),
        "AddressComponent",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of an [AddressComponent] (RFC 9553, section 2.5.1.2; IANA table 10). Values are
 * case-sensitive. Other registered or vendor values (section 1.8.2) are kept as received.
 */
@Serializable
@JvmInline
public value class AddressComponentKind(
    public val value: String,
) {
    /** The registered kinds, in RFC order (RFC 9553, section 3.7, table 10). */
    public companion object {
        /** The room, suite number, or identifier. */
        public val Room: AddressComponentKind = AddressComponentKind("room")

        /** The apartment, unit, or box number. */
        public val Apartment: AddressComponentKind = AddressComponentKind("apartment")

        /** The floor or level. */
        public val Floor: AddressComponentKind = AddressComponentKind("floor")

        /** The building, tower, or condominium. */
        public val Building: AddressComponentKind = AddressComponentKind("building")

        /** The street number, which need not be numeric, such as `112-10` or `123 1/2`. */
        public val Number: AddressComponentKind = AddressComponentKind("number")

        /** The street name. */
        public val Name: AddressComponentKind = AddressComponentKind("name")

        /** The block name or number. */
        public val Block: AddressComponentKind = AddressComponentKind("block")

        /** The subdistrict, ward, or other subunit of a district. */
        public val Subdistrict: AddressComponentKind = AddressComponentKind("subdistrict")

        /** The district name. */
        public val District: AddressComponentKind = AddressComponentKind("district")

        /** The city, town, village, post town, or other locality. */
        public val Locality: AddressComponentKind = AddressComponentKind("locality")

        /** The province, state, prefecture, county, canton, or other administrative area. */
        public val Region: AddressComponentKind = AddressComponentKind("region")

        /** The postal code, such as a ZIP code. */
        public val Postcode: AddressComponentKind = AddressComponentKind("postcode")

        /** The country name. */
        public val Country: AddressComponentKind = AddressComponentKind("country")

        /** The cardinal direction or quadrant, such as `north`. */
        public val Direction: AddressComponentKind = AddressComponentKind("direction")

        /** A well-known feature that stands in for the street and number, such as `White House`. */
        public val Landmark: AddressComponentKind = AddressComponentKind("landmark")

        /** The post office box number or identifier. */
        public val PostOfficeBox: AddressComponentKind = AddressComponentKind("postOfficeBox")

        /**
         * The verbatim text between two ordered non-separator components. It overrides the
         * address's default separator and is never set on an unordered address.
         */
        public val Separator: AddressComponentKind = AddressComponentKind("separator")
    }
}
