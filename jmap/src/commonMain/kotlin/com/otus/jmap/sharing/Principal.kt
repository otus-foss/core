package com.otus.jmap.sharing

import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

/**
 * What a [Principal] represents (RFC 9670, section 2). The RFC allows only the five values below,
 * but any string decodes, so one Principal with a newer value cannot fail a whole `Principal/get`.
 */
@Serializable
@JvmInline
public value class PrincipalKind(
    public val value: String,
) {
    public companion object {
        /** A single person. */
        public val Individual: PrincipalKind = PrincipalKind("individual")

        /** A group of other Principals. */
        public val Group: PrincipalKind = PrincipalKind("group")

        /** A resource, such as a projector. */
        public val Resource: PrincipalKind = PrincipalKind("resource")

        /** A location, such as a room. */
        public val Location: PrincipalKind = PrincipalKind("location")

        /** Any other kind of Principal. */
        public val Other: PrincipalKind = PrincipalKind("other")
    }
}

/**
 * A person, group, resource, location, or other entity that data can be shared with (RFC 9670,
 * section 2). Its [id] is a key of `shareWith` on shareable objects (RFC 9670, section 4). Decoding
 * does not check the server's [email] and [timeZone] rules, so one bad value cannot hide the rest.
 */
@Serializable
public data class Principal(
    val id: String,
    /**
     * Null only when a `/get` asked for a subset of `properties` that leaves out `type`. The server
     * then returns just the properties asked for and [id] (RFC 8620, section 5.1), and that answer
     * must still decode, although RFC 9670 requires `type` on a whole Principal.
     */
    val type: PrincipalKind? = null,
    /** The display name, such as "Jane Doe" or "Room 4B". */
    val name: String = "",
    val description: String? = null,
    /** An RFC 5322 addr-spec, or null when the Principal has no email. */
    val email: String? = null,
    /** A name from the IANA Time Zone Database, or null when the server does not know it. */
    val timeZone: String? = null,
    /** Details for each capability URI, in the form the document that registered it defines. */
    val capabilities: Map<String, JsonObject> = emptyMap(),
    /**
     * The accounts with this Principal's data that the user may access, by account id, even those
     * the user has not subscribed to yet (RFC 9670, section 1.4). Null when there are none.
     */
    val accounts: Map<String, PrincipalAccount>? = null,
)

/**
 * An account in [Principal.accounts], shaped like a session Account object (RFC 8620, section 2).
 * The session lists only accounts the user owns or subscribes to (RFC 9670, section 1.4), so this
 * is how a client finds the others.
 */
@Serializable
public data class PrincipalAccount(
    val name: String = "",
    val isPersonal: Boolean = false,
    val isReadOnly: Boolean = false,
    /** Each account capability's object, by capability URI. */
    val accountCapabilities: Map<String, JsonObject> = emptyMap(),
)

/**
 * The `Principal` data type (RFC 9670, sections 2 and 7.3) for the standard `/get`, `/changes`,
 * `/set`, `/query`, and `/queryChanges`. The RFC defines no sort property for `/query`, and a
 * directory-backed server may answer every `/changes` and `/queryChanges` with
 * `cannotCalculateChanges` (RFC 9670, sections 2.2 and 2.5).
 */
public val PrincipalType: JmapType<Principal> =
    JmapType("Principal", JmapCapability.PRINCIPALS, Principal.serializer())
