package com.otus.avatar

/**
 * Where avatars get website icons while website icons are on. [Websites] asks each sender's
 * website, which then sees the device's IP address. [DuckDuckGo] and [Bulwark] ask an icon service
 * instead, so sender websites never see the device. [Bulwark] is the user's own Bulwark webmail
 * server, which also answers brand logos. Build the [AvatarOptions] of a choice with [avatarOptions].
 */
public enum class IconSource {
    Websites,
    DuckDuckGo,
    Bulwark,
    ;

    public val storageValue: String get() = name.lowercase()

    public companion object {
        /** The choice saved as [stored], or null when none is. */
        public fun decode(stored: String?): IconSource? = entries.firstOrNull { it.storageValue == stored }
    }
}
