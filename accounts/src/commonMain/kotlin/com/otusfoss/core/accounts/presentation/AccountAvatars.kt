package com.otusfoss.core.accounts.presentation

import com.otusfoss.core.accounts.domain.ConnectedAccount
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar

/**
 * The avatars the account page shows: one for each account and one for each of its own
 * addresses. The app supplies them, and the editor the page opens for them, so accounts needs no
 * avatar code.
 */
public interface AccountAvatars {
    /** Why the last avatar change failed, or null. */
    public val error: String?

    /** Starts loading the avatars of every address of [accounts]. */
    public fun load(accounts: List<ConnectedAccount>)

    /** The avatar that leads [account]'s row. */
    public fun accountAvatar(account: ConnectedAccount): ChipAvatar

    /** The avatar on the pill of [email], one of [account]'s own addresses. */
    public fun avatar(
        account: ConnectedAccount,
        email: String,
    ): ChipAvatar
}
