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

/**
 * The avatar of each of the user's own addresses. A save shows the new avatar at once and puts the
 * old one back if the server refuses it. [onChange] runs after a save, so the app can redraw the
 * avatars it already shows. Without [photos], the sheet offers no Photo choice.
 */
public class OwnAvatarsState(
    private val avatars: OwnAvatars,
    private val photos: AvatarPhotoPicker?,
    private val scope: CoroutineScope,
    private val onChange: () -> Unit = {},
) {
    private var shown: Map<AddressKey, ResolvedAvatar> by mutableStateOf(emptyMap())

    public var error: String? by mutableStateOf(null)
        private set

    public val canPickPhoto: Boolean get() = photos != null

    /** What [email] of [account] shows now, or null before it loads. */
    public fun avatar(
        account: ConnectedAccount,
        email: String,
    ): ResolvedAvatar? = shown[AddressKey(account.id, email.lowercase())]

    /** Loads the avatar of every address of [accounts]. */
    public fun load(accounts: List<ConnectedAccount>) {
        accounts.forEach { account ->
            account.ownAddresses.forEach { email ->
                scope.launch { show(account, email, avatars.avatar(account.id, email, account.name)) }
            }
        }
    }

    public suspend fun pickPhoto(): ContactAvatar.Photo? = photos?.pick()

    /** Saves [avatar] for [email], or goes back to the generated avatar when it is null. */
    public fun save(
        account: ConnectedAccount,
        email: String,
        avatar: ContactAvatar?,
    ) {
        val before = avatar(account, email)
        val fallback = before?.fallback ?: generatedAvatar(account.name, email)
        error = null
        show(account, email, avatar.resolved(fallback))
        scope.launch {
            try {
                avatars.save(account.id, email, account.name, avatar)
                onChange()
                val saved = avatars.avatar(account.id, email, account.name)
                // Same-looking bytes from the server would decode again and flash the initials.
                if (!saved.looksLike(avatar(account, email))) show(account, email, saved)
            } catch (failure: OwnAvatarException) {
                before?.let { show(account, email, it) }
                error = failure.reason.message()
            }
        }
    }

    private fun show(
        account: ConnectedAccount,
        email: String,
        avatar: ResolvedAvatar,
    ) {
        shown = shown + (AddressKey(account.id, email.lowercase()) to avatar)
    }

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

private fun ResolvedAvatar.looksLike(other: ResolvedAvatar?): Boolean =
    other != null && style == other.style && origin == other.origin && image?.bytes.contentEquals(other.image?.bytes)

private fun OwnAvatarFailure.message(): String =
    when (this) {
        OwnAvatarFailure.NoContacts -> "Your server doesn't offer contacts, so it can't keep an avatar."
        OwnAvatarFailure.ReadOnly -> "Couldn't save the avatar: your address book is read-only."
        OwnAvatarFailure.Failed -> "Couldn't save the avatar. Try again."
    }
