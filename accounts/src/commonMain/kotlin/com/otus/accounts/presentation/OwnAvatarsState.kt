package com.otus.accounts.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.accounts.domain.ConnectedAccount
import com.otus.avatar.AvatarOrigin
import com.otus.avatar.AvatarPhotoPicker
import com.otus.avatar.ContactAvatar
import com.otus.avatar.GeneratedAvatar
import com.otus.avatar.OwnAvatarException
import com.otus.avatar.OwnAvatarFailure
import com.otus.avatar.OwnAvatars
import com.otus.avatar.ResolvedAvatar
import com.otus.avatar.generatedAvatar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The avatar of each of the user's own addresses. A save shows the new avatar at once and puts the
 * last saved one back if the server refuses it. Saves run one at a time, so a quick second save
 * finds the self card the first one made instead of making another. [onChange] runs after a save,
 * so the app can redraw the avatars it already shows. Without [photos], the sheet offers no Photo
 * choice.
 */
public class OwnAvatarsState(
    private val avatars: OwnAvatars,
    private val photos: AvatarPhotoPicker?,
    private val scope: CoroutineScope,
    private val onChange: () -> Unit = {},
) {
    private var shown: Map<AddressKey, ResolvedAvatar> by mutableStateOf(emptyMap())

    /** What the server holds, as last loaded or saved, to roll a failed save back to. */
    private var saved: Map<AddressKey, ResolvedAvatar> = emptyMap()

    /** The newest save of each address; an older save that finishes later leaves the screen alone. */
    private var newest: Map<AddressKey, Any> = emptyMap()

    private val saving = Mutex()

    public var error: String? by mutableStateOf(null)
        private set

    public val canPickPhoto: Boolean get() = photos != null

    /** What [email] of [account] shows now, or null before it loads. */
    public fun avatar(
        account: ConnectedAccount,
        email: String,
    ): ResolvedAvatar? = shown[key(account, email)]

    /**
     * The avatar [account] shows as a whole: its first address's, which is the sign-in address
     * when that is one, as the sidebar shows it. Null before it loads.
     */
    public fun accountAvatar(account: ConnectedAccount): ResolvedAvatar? =
        account.ownAddresses.firstOrNull()?.let { shown[key(account, it)] }

    /** Loads the avatar of every address of [accounts]. */
    public fun load(accounts: List<ConnectedAccount>) {
        accounts.forEach { account ->
            account.ownAddresses.forEach { email ->
                scope.launch { confirm(key(account, email), avatars.avatar(account.id, email, account.name)) }
            }
        }
    }

    public suspend fun pickPhoto(): ContactAvatar.Photo? = photos?.pick()

    /**
     * The addresses of [account] whose own avatar would change if [avatar] went to all of them:
     * those with a photo or a style of their own that differs from it.
     */
    public fun replaced(
        account: ConnectedAccount,
        avatar: ContactAvatar?,
    ): List<String> =
        account.ownAddresses.filter { email ->
            shown[key(account, email)]?.let { it.isSet && !it.looksLike(avatar.resolved(it.fallback)) } == true
        }

    /** Saves [avatar] for each of [emails] of [account], as [save] does for one. */
    public fun saveAll(
        account: ConnectedAccount,
        emails: List<String>,
        avatar: ContactAvatar?,
    ): Unit = emails.forEach { save(account, it, avatar) }

    /** Saves [avatar] for [email], or goes back to the generated avatar when it is null. */
    public fun save(
        account: ConnectedAccount,
        email: String,
        avatar: ContactAvatar?,
    ) {
        val key = key(account, email)
        val fallback = shown[key]?.fallback ?: generatedAvatar(account.name, email)
        val save = Any()
        newest = newest + (key to save)
        error = null
        show(key, avatar.resolved(fallback))
        scope.launch {
            saving.withLock {
                try {
                    avatars.save(account.id, email, account.name, avatar)
                    onChange()
                    confirm(key, avatars.avatar(account.id, email, account.name), save)
                } catch (failure: OwnAvatarException) {
                    if (newest[key] === save) saved[key]?.let { show(key, it) }
                    error = failure.reason.message()
                }
            }
        }
    }

    /** Records what the server holds, and shows it unless a newer save is on screen. */
    private fun confirm(
        key: AddressKey,
        avatar: ResolvedAvatar,
        save: Any? = null,
    ) {
        saved = saved + (key to avatar)
        // Same-looking bytes from the server would decode again and flash the initials.
        if (newest[key] === save && !avatar.looksLike(shown[key])) show(key, avatar)
    }

    private fun show(
        key: AddressKey,
        avatar: ResolvedAvatar,
    ) {
        shown = shown + (key to avatar)
    }

    private fun key(
        account: ConnectedAccount,
        email: String,
    ) = AddressKey(account.id, email.lowercase())

    private data class AddressKey(
        val account: String,
        val email: String,
    )
}

private fun ContactAvatar?.resolved(fallback: GeneratedAvatar): ResolvedAvatar =
    when (this) {
        is ContactAvatar.Photo -> ResolvedAvatar(fallback, image, AvatarOrigin.Contact)
        is ContactAvatar.Styled -> ResolvedAvatar(fallback, origin = AvatarOrigin.Contact, style = style)
        null -> ResolvedAvatar(fallback)
    }

/** Whether the user gave this address an avatar: a photo or a style rather than the generated one. */
internal val ResolvedAvatar.isSet: Boolean get() = style != null || origin == AvatarOrigin.Contact

private fun ResolvedAvatar.looksLike(other: ResolvedAvatar?): Boolean =
    other != null && style == other.style && origin == other.origin && image?.bytes.contentEquals(other.image?.bytes)

private fun OwnAvatarFailure.message(): String =
    when (this) {
        OwnAvatarFailure.NoContacts -> "Your server doesn't offer contacts, so it can't keep an avatar."
        OwnAvatarFailure.ReadOnly -> "Couldn't save the avatar: your address book is read-only."
        OwnAvatarFailure.Failed -> "Couldn't save the avatar. Try again."
    }
