package com.otus.avatar

/** The avatar of each of the user's own addresses, kept on a self card in their address book. */
public interface OwnAvatars {
    /** What the app shows for [email] now, the same way it shows any sender. */
    public suspend fun avatar(
        accountKey: String,
        email: String,
        name: String,
    ): ResolvedAvatar

    /**
     * Saves [avatar] on the self card of [email], creating the card when there is none, or removes
     * the card's avatar when [avatar] is null. Throws [OwnAvatarException] when it cannot save.
     */
    public suspend fun save(
        accountKey: String,
        email: String,
        name: String,
        avatar: ContactAvatar?,
    )
}

public class OwnAvatarException(
    public val reason: OwnAvatarFailure,
    cause: Throwable? = null,
) : Exception(reason.name, cause)

public enum class OwnAvatarFailure {
    /** The server offers no contacts for this login (RFC 9610). */
    NoContacts,

    /** No address book the user may write to holds or can hold the card. */
    ReadOnly,

    /** The connection or the server failed; trying again may work. */
    Failed,
}

/** Lets the user pick a photo, already cropped and small enough to upload, or returns null. */
public fun interface AvatarPhotoPicker {
    public suspend fun pick(): ContactAvatar.Photo?
}
