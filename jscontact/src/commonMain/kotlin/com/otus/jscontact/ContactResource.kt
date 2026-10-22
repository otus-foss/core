package com.otus.jscontact

/**
 * A resource of the entity a Card represents, identified by a URI (RFC 9553, section 1.4.4).
 * Resource itself is abstract: each value is a concrete type, [Calendar] (section 2.4.1),
 * [CryptoKey] (2.6.1), [Directory] (2.6.2), [Link] (2.6.3), or [Media] (2.6.4), and its `@type`
 * names that type, never `Resource`. The `kind` property is left to each concrete type, because
 * each one has its own set of kinds.
 */
public sealed interface ContactResource {
    /** The resource, a URI as defined in section 3 of RFC 3986. Mandatory. */
    public val uri: String

    /** The media type (RFC 2046) of the resource at [uri]. */
    public val mediaType: String?

    /** The contexts in which to use this resource (RFC 9553, section 1.5.1). */
    public val contexts: Map<ContactContext, Boolean>?

    /** The preference among resources of the same property, 1 (most preferred) to 100 (section 1.5.3). */
    public val pref: Int?

    /** A custom label for the resource, usually shown next to it (RFC 9553, section 1.5.2). */
    public val label: String?
}
