package com.otus.accounts.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.otus.accounts.presentation.AvatarDraft
import com.otus.accounts.presentation.AvatarKind
import com.otus.accounts.presentation.PhotoSource
import com.otus.avatar.ResolvedAvatar
import com.otus.designsystem.components.PersonAvatar
import com.otus.designsystem.components.SegmentedToggle
import com.otus.designsystem.components.SheetFrame
import com.otus.designsystem.theme.AvatarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SheetTokens
import kotlinx.coroutines.launch
import com.otus.avatar.ContactAvatar as SavedAvatar

/**
 * Edits the avatar of [email]: a big preview, the kinds of avatar, and the choices of the kind
 * picked. Save runs [onSave] with the new avatar; it keeps a saved photo that did not change.
 * [pickPhoto] is null when the platform cannot pick photos, which hides Your photo.
 */
@Composable
internal fun OwnAvatarSheet(
    email: String,
    shown: ResolvedAvatar?,
    pickPhoto: (suspend () -> SavedAvatar.Photo?)?,
    onSave: (SavedAvatar) -> Unit,
    onDismiss: () -> Unit,
) {
    val kinds = AvatarKind.entries
    var draft by remember { mutableStateOf(AvatarDraft.of(shown, canUpload = pickPhoto != null)) }
    val scope = rememberCoroutineScope()
    val pick = {
        scope.launch {
            pickPhoto?.invoke()?.let {
                draft =
                    draft.copy(kind = AvatarKind.Photo, source = PhotoSource.Upload, photo = it)
            }
        }
    }
    SheetFrame(
        "Avatar",
        onDismiss = onDismiss,
        onDone = {
            draft.avatar?.let(onSave)
            onDismiss()
        },
        doneEnabled = draft.ready,
        doneLabel = "Save",
    ) {
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PersonAvatar(draft.preview(shown), AvatarTokens.editorSize)
            Text(email, color = OtusColors.text2, textAlign = TextAlign.Center)
            SegmentedToggle(kinds.map { it.label }, kinds.indexOf(draft.kind), { draft = draft.copy(kind = kinds[it]) })
            AvatarChoices(
                draft,
                email.substringAfterLast('@'),
                onChange = { draft = it },
                onPickPhoto = if (pickPhoto != null) ({ pick() }) else null,
                Modifier.fillMaxWidth(),
            )
        }
    }
}
