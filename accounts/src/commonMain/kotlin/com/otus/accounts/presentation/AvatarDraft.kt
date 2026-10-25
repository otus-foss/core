package com.otus.accounts.presentation

import com.otus.avatar.AvatarImage
import com.otus.avatar.AvatarMark
import com.otus.avatar.AvatarOrigin
import com.otus.avatar.AvatarStyle
import com.otus.avatar.ContactAvatar
import com.otus.avatar.ResolvedAvatar
import com.otus.avatar.avatarLetters
import com.otus.designsystem.components.ChipAvatar
import com.otus.designsystem.elements.AvatarSymbols

/** The kinds of avatar the sheet offers, in the order of its toggle. */
internal enum class AvatarKind(
    val label: String,
) {
    Photo("Photo"),
    Icon("Icon"),
    Letters("Letters"),
}

/** Where a Photo avatar comes from. */
internal enum class PhotoSource {
    /** A photo the user picked. */
    Upload,

    /** The website icon of the address's domain, even with website icons off. */
    Website,
}

/**
 * The avatar being edited. It keeps every kind's last choice, so switching kinds and back loses
 * nothing. [photo] is a newly picked photo; [savedPhoto] is the one already on the card.
 */
internal data class AvatarDraft(
    val kind: AvatarKind,
    /** A folder color name or a custom `#RRGGBB`, as [AvatarStyle.color] stores it. */
    val color: String,
    val letters: String,
    val source: PhotoSource = PhotoSource.Upload,
    val icon: String = AvatarSymbols.icons.keys.first(),
    val photo: ContactAvatar.Photo? = null,
    val savedPhoto: AvatarImage? = null,
) {
    /** False when the kind has nothing to save yet, such as Your photo before a photo is picked. */
    val ready: Boolean
        get() =
            when (kind) {
                AvatarKind.Photo -> source == PhotoSource.Website || photo != null || savedPhoto != null
                AvatarKind.Icon -> true
                AvatarKind.Letters -> letters.isNotEmpty()
            }

    /** What to save, or null when the saved photo stays as it is. */
    val avatar: ContactAvatar?
        get() =
            when (kind) {
                AvatarKind.Photo -> if (source == PhotoSource.Website) styled(AvatarMark.Website) else photo
                AvatarKind.Icon -> styled(AvatarMark.Icon(icon))
                AvatarKind.Letters -> styled(AvatarMark.Letters(letters))
            }

    /**
     * The preview. Website icon shows the icon when [shown] already came from it, and the initials
     * on the color otherwise, since the lookup runs only after saving.
     */
    fun preview(shown: ResolvedAvatar?): ChipAvatar {
        val initials = shown?.fallback?.initials.orEmpty()
        return when {
            kind != AvatarKind.Photo -> {
                (avatar as ContactAvatar.Styled).style.toChipAvatar()
            }

            source == PhotoSource.Website -> {
                val icon = shown?.takeIf { it.origin == AvatarOrigin.Domain }?.image
                if (icon == null) styledChip(initials, color) else ChipAvatar(initials, 0, icon.bytes)
            }

            else -> {
                ChipAvatar(initials, shown?.fallback?.colorIndex ?: 0, (photo?.image ?: savedPhoto)?.bytes)
            }
        }
    }

    private fun styled(mark: AvatarMark) = ContactAvatar.Styled(AvatarStyle(color, mark))

    companion object {
        /**
         * Starts from what [shown] draws now. An avatar the user never set starts on Your photo, or
         * on Website icon when [canUpload] is false.
         */
        fun of(
            shown: ResolvedAvatar?,
            canUpload: Boolean,
        ): AvatarDraft {
            val fallback = shown?.fallback
            val color = AvatarColors[(fallback?.colorIndex ?: 0) % AvatarColors.size].name
            val source = if (canUpload) PhotoSource.Upload else PhotoSource.Website
            val initial = AvatarDraft(AvatarKind.Photo, color, avatarLetters(fallback?.initials.orEmpty()), source)
            val style = shown?.style
            return when {
                style != null -> {
                    initial.with(style)
                }

                shown?.image != null && shown.origin == AvatarOrigin.Contact -> {
                    initial.copy(source = PhotoSource.Upload, savedPhoto = shown.image)
                }

                else -> {
                    initial
                }
            }
        }

        private fun AvatarDraft.with(style: AvatarStyle): AvatarDraft {
            val styled = copy(color = style.color)
            return when (val mark = style.mark) {
                AvatarMark.Website -> styled.copy(source = PhotoSource.Website)
                is AvatarMark.Letters -> styled.copy(kind = AvatarKind.Letters, letters = mark.text)
                is AvatarMark.Icon -> styled.copy(kind = AvatarKind.Icon, icon = mark.name)
            }
        }
    }
}
