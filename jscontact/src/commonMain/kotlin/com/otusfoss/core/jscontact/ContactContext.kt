package com.otusfoss.core.jscontact

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * A key of a `contexts` map: where to use a piece of contact information (RFC 9553, section 1.5.1).
 * The set is open, so values registered later and vendor-specific values (section 1.8.2) decode
 * as they are. Values are case-sensitive (section 1.7.1).
 */
@Serializable
@JvmInline
public value class ContactContext(
    public val value: String,
) {
    /** The registered contexts (RFC 9553, sections 1.5.1 and 2.5.1.1). */
    public companion object {
        /** Information for private use (RFC 9553, section 1.5.1). */
        public val Private: ContactContext = ContactContext("private")

        /** Information for professional use (RFC 9553, section 1.5.1). */
        public val Work: ContactContext = ContactContext("work")

        /** A billing address. Valid only in `Address.contexts` (RFC 9553, section 2.5.1.1). */
        public val Billing: ContactContext = ContactContext("billing")

        /** A delivery address. Valid only in `Address.contexts` (RFC 9553, section 2.5.1.1). */
        public val Delivery: ContactContext = ContactContext("delivery")
    }
}
