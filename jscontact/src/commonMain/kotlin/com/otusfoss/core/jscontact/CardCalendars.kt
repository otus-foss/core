package com.otusfoss.core.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

/**
 * A calendar of the entity, such as one for free-busy lookups, an entry of the Card `calendars`
 * map (RFC 9553, section 2.4.1). It is a [ContactResource] whose [kind] is mandatory.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Calendar.Serializer::class)
public data class Calendar(
    val kind: CalendarKind,
    override val uri: String,
    override val mediaType: String? = null,
    override val contexts: Map<ContactContext, Boolean>? = null,
    override val pref: Int? = null,
    override val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) : ContactResource {
    internal object Serializer : JsContactSerializer<Calendar>(
        generatedSerializer(),
        "Calendar",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}

/**
 * The kind of a [Calendar] (RFC 9553, section 2.4.1; IANA table 12). Values are case-sensitive.
 * Other registered or vendor values (section 1.8.2) are kept as received.
 */
@Serializable
@JvmInline
public value class CalendarKind(
    public val value: String,
) {
    /** The registered kinds (RFC 9553, section 3.7, table 12). */
    public companion object {
        /** A calendar of entries such as events or tasks. */
        public val Calendar: CalendarKind = CalendarKind("calendar")

        /** A free-busy lookup, for example to schedule group events. */
        public val FreeBusy: CalendarKind = CalendarKind("freeBusy")
    }
}

/**
 * An address at which the entity receives calendar scheduling invitations, an entry of the Card
 * `schedulingAddresses` map (RFC 9553, section 2.4.2). It is not a [ContactResource]: it has no
 * kind and no media type.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = SchedulingAddress.Serializer::class)
public data class SchedulingAddress(
    /** An RFC 3986 URI, such as `mailto:janedoe@example.com`. */
    val uri: String,
    val contexts: Map<ContactContext, Boolean>? = null,
    val pref: Int? = null,
    val label: String? = null,
    /** Unknown and vendor-specific properties, kept as received (RFC 9553, section 1.7.4). */
    @Transient val extra: Map<String, JsonElement> = emptyMap(),
) {
    internal object Serializer : JsContactSerializer<SchedulingAddress>(
        generatedSerializer(),
        "SchedulingAddress",
        { it.extra },
        { value, extra -> value.copy(extra = extra) },
    )
}
