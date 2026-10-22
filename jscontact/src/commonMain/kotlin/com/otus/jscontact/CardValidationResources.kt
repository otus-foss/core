package com.otus.jscontact

/**
 * Validates what every Resource shares: its contexts and pref (RFC 9553, section 1.4.4). Each type checks
 * its own uri, because a JMAP Media may leave it out (RFC 9610, section 3).
 */
internal fun ValidationScope.resource(resource: ContactResource) {
    contexts(resource.contexts)
    pref(resource.pref)
}

/** Validates a CryptoKey, whose kinds RFC 9553 leaves unregistered (section 2.6.1). */
internal fun ValidationScope.checkCryptoKey(key: CryptoKey) {
    propertyNames(key.extra, CryptoKey.serializer())
    enumValue(key.kind, emptySet(), KIND)
    uri(key.uri, "uri")
    resource(key)
}

/** Validates a Directory (RFC 9553, section 2.6.2). */
internal fun ValidationScope.checkDirectory(directory: Directory) {
    propertyNames(directory.extra, Directory.serializer())
    enumValue(directory.kind.value, RegisteredValues.directoryKinds, KIND)
    uri(directory.uri, "uri")
    resource(directory)
    listAs(directory.listAs)
}

/** Validates a Link (RFC 9553, section 2.6.3). */
internal fun ValidationScope.checkLink(link: Link) {
    propertyNames(link.extra, Link.serializer())
    enumValue(link.kind?.value, RegisteredValues.linkKinds, KIND)
    uri(link.uri, "uri")
    resource(link)
}

/**
 * Validates a Media (RFC 9553, section 2.6.4). Over JMAP a blobId may stand in for its uri, and then
 * the media type must be set (RFC 9610, section 3).
 */
internal fun ValidationScope.checkMedia(media: Media) {
    propertyNames(media.extra, Media.serializer())
    enumValue(media.kind.value, RegisteredValues.mediaKinds, KIND)
    ensure(media.uri.isNotEmpty() || media.blobId != null, "A Media needs a uri or a blobId (RFC 9610, section 3)")
    uri(media.uri.ifEmpty { null }, "uri")
    ensure(media.blobId == null || ID.matches(media.blobId), "Not an Id (RFC 8620, section 1.2)", "blobId")
    ensure(
        media.blobId == null || media.mediaType != null,
        "A Media with a blobId needs a mediaType (RFC 9610, section 3)",
        "mediaType",
    )
    resource(media)
}

/** Validates a Note and its author (RFC 9553, section 2.8.3). */
internal fun ValidationScope.checkNote(note: Note) {
    propertyNames(note.extra, Note.serializer())
    member("author", note.author) { author ->
        propertyNames(author.extra, Author.serializer())
        ensure(author.name != null || author.uri != null, "An Author needs a name or a uri (RFC 9553, section 2.8.3)")
    }
}

/** Validates a PersonalInfo (RFC 9553, section 2.8.4). */
internal fun ValidationScope.checkPersonalInfo(info: PersonalInfo) {
    propertyNames(info.extra, PersonalInfo.serializer())
    enumValue(info.kind.value, RegisteredValues.personalInfoKinds, KIND)
    enumValue(info.level?.value, RegisteredValues.personalInfoLevels, "level")
    listAs(info.listAs)
}

private const val KIND = "kind"
