package com.otusfoss.core.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * A phone number of the entity, an entry of the Card `phones` map (RFC 9553, section 2.3.3).
 * A lower [pref] ranks the number higher among the Card's numbers, from 1 to 100, and a missing one
 * ranks last (section 1.5.3).
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Phone.Serializer::class)
public data class Phone(
    /** A URI such as `tel:+1-201-555-0123` or `sip:...`, or free text. */
    val number: String,
    /** The set of [PhoneFeature]s the number supports. Every value must be `true`. */
    val features: Map<PhoneFeature, Boolean>? = null,
    val contexts: Map<ContactContext, Boolean>? = null,
    val pref: Int? = null,
    val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<Phone>(
        generatedSerializer(),
        "Phone",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * What a [Phone] number can be used for (RFC 9553, section 2.3.3; IANA table 8). Values are
 * case-sensitive. Other registered or vendor values (`example.com:foo`, section 1.8.2) are kept as
 * received.
 */
@Serializable
@JvmInline
public value class PhoneFeature(
    public val value: String,
) {
    /** The registered features (RFC 9553, section 3.7, table 8). */
    public companion object {
        /** The number is for a mobile phone. */
        public val Mobile: PhoneFeature = PhoneFeature("mobile")

        /** The number takes voice calls. */
        public val Voice: PhoneFeature = PhoneFeature("voice")

        /** The number takes text messages (SMS). */
        public val Text: PhoneFeature = PhoneFeature("text")

        /** The number takes video calls. */
        public val Video: PhoneFeature = PhoneFeature("video")

        /** A main number, such as a company's front desk, rather than a direct dial. */
        public val MainNumber: PhoneFeature = PhoneFeature("main-number")

        /** The number is for a device for people with hearing or speech difficulties. */
        public val Textphone: PhoneFeature = PhoneFeature("textphone")

        /** The number takes faxes. */
        public val Fax: PhoneFeature = PhoneFeature("fax")

        /** The number is for a pager. */
        public val Pager: PhoneFeature = PhoneFeature("pager")
    }
}
