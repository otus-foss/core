package com.otus.jscontact

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * The kind of entity a Card represents (RFC 9553, section 2.1.4). An absent Card `kind` means
 * [Individual]. Other values are vendor-specific, in the form `example.com:baz` (section 1.8.2).
 */
@Serializable
@JvmInline
public value class CardKind(
    public val value: String,
) {
    /** The registered kinds (RFC 9553, section 2.1.4). */
    public companion object {
        /** A single person. */
        public val Individual: CardKind = CardKind("individual")

        /** A group of people or entities. A Card with `members` must have this kind. */
        public val Group: CardKind = CardKind("group")

        /** An organization. */
        public val Org: CardKind = CardKind("org")

        /** A named location. */
        public val Location: CardKind = CardKind("location")

        /** A device such as an appliance, a computer, or a network element. */
        public val Device: CardKind = CardKind("device")

        /** A software application. */
        public val Application: CardKind = CardKind("application")
    }
}
