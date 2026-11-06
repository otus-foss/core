package com.otus.avatar.ui.presentation

import com.otus.avatar.AvatarMark
import com.otus.avatar.AvatarStyle
import com.otus.avatar.ContactAvatar
import com.otus.avatar.GeneratedAvatar
import com.otus.avatar.ResolvedAvatar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AvatarDraftTest {
    private val fallback = GeneratedAvatar("FR", 1)

    @Test
    fun anAddressWithoutAnAvatarWaitsForAPhoto() {
        val draft = AvatarDraft.of(ResolvedAvatar(fallback), canUpload = true)
        assertEquals(AvatarKind.Photo, draft.kind)
        assertFalse(draft.ready)
        assertNull(draft.avatar)
    }

    @Test
    fun withoutAPickerTheDraftStartsOnTheWebsiteIcon() {
        val draft = AvatarDraft.of(ResolvedAvatar(fallback), canUpload = false)
        assertEquals(PhotoSource.Website, draft.source)
        assertEquals(AvatarMark.Website, (draft.avatar as ContactAvatar.Styled).style.mark)
    }

    @Test
    fun aSavedStyleOpensOnItsKindAndColor() {
        val style = AvatarStyle("#123456", AvatarMark.Icon("pets"))
        val draft = AvatarDraft.of(ResolvedAvatar(fallback, style = style), canUpload = true)
        assertEquals(AvatarKind.Icon, draft.kind)
        assertEquals(style, (draft.avatar as ContactAvatar.Styled).style)
    }

    @Test
    fun switchingKindsKeepsEachChoice() {
        val draft = AvatarDraft.of(ResolvedAvatar(fallback), canUpload = true)
        val letters = draft.copy(kind = AvatarKind.Letters)
        assertEquals(AvatarMark.Letters("FR"), (letters.avatar as ContactAvatar.Styled).style.mark)
        assertEquals(PhotoSource.Upload, letters.copy(kind = AvatarKind.Photo).source)
    }
}
