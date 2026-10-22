package com.otus.jmap.sharing

import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapType
import com.otus.jmap.UtcDateSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A record that the user's rights on a shared object changed (RFC 9670, section 3). Only the server
 * creates these, in the account that holds the Principals. A client dismisses one by destroying it
 * with `ShareNotification/set`. The server rejects creates and updates with `forbidden`
 * (RFC 9670, section 3.3). Every property is server-set and immutable.
 *
 * [created], [changedBy], [objectType], [objectAccountId], and [objectId] are null only when a `/get`
 * asked for a subset of `properties` that leaves them out. The server then returns just the
 * properties asked for and [id] (RFC 8620, section 5.1), and that answer must still decode,
 * although RFC 9670 requires all five on a whole notification.
 */
@Serializable
public data class ShareNotification(
    /** The RFC types this as a String rather than an Id (RFC 9670, section 3), so any string decodes. */
    val id: String,
    @Serializable(with = UtcDateSerializer::class)
    val created: Instant? = null,
    /** Who made the change. */
    val changedBy: ShareEntity? = null,
    /** The data type name of the changed object, from the IANA "JMAP Data Types" registry, such as `AddressBook`. */
    val objectType: String? = null,
    /** The account that holds the object. The session leaves it out while the user subscribes to nothing in it. */
    val objectAccountId: String? = null,
    val objectId: String? = null,
    /** The object's `myRights` for the user before the change. */
    val oldRights: Map<String, Boolean>? = null,
    /** The object's `myRights` for the user after the change. */
    val newRights: Map<String, Boolean>? = null,
    /** The object's name when the server made the notification, so a user who lost access can tell what it was. */
    val name: String = "",
)

/** The Entity in [ShareNotification.changedBy] (RFC 9670, section 3). */
@Serializable
public data class ShareEntity(
    val name: String = "",
    /** Null when no email is available. */
    val email: String? = null,
    /** Null when the entity has no Principal. */
    val principalId: String? = null,
)

/**
 * The `ShareNotification` data type (RFC 9670, sections 3 and 7.4) for the standard `/get`,
 * `/changes`, `/set`, `/query`, and `/queryChanges`. Only `destroy` works in `/set`.
 */
public val ShareNotificationType: JmapType<ShareNotification> =
    JmapType("ShareNotification", JmapCapability.PRINCIPALS, ShareNotification.serializer())
