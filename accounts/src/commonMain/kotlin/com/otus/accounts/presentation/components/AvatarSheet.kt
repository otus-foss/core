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
import com.otus.accounts.presentation.isSet
import com.otus.avatar.ResolvedAvatar
import com.otus.designsystem.shared.controls.components.SegmentedToggle
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.shared.overlays.components.SheetHeaderAction
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.people.AvatarTokens
import kotlinx.coroutines.launch
import com.otus.avatar.ContactAvatar as SavedAvatar

/**
 * Edits the avatar of [label], such as one of the user's addresses, all of an account's, or a
 * contact: a big preview, the kinds of avatar, and the choices of the kind picked. A website icon
 * comes from [domain]. Save runs [onSave] with the new avatar; it keeps a saved photo that did not
 * change. Reset, shown once the user has set an avatar, runs [onSave] with null to go back to the
 * generated one. [pickPhoto] is null when the platform cannot pick photos, which hides Your photo.
 */
@Composable
public fun AvatarSheet(
    label: String,
    domain: String,
    shown: ResolvedAvatar?,
    pickPhoto: (suspend () -> SavedAvatar.Photo?)?,
    onSave: (SavedAvatar?) -> Unit,
    onDismiss: () -> Unit,
) {
    val kinds = AvatarKind.entries
    // Start again once the avatar loads, so a sheet opened early does not start from defaults.
    var draft by remember(shown != null) { mutableStateOf(AvatarDraft.of(shown, canUpload = pickPhoto != null)) }
    var resetting by remember { mutableStateOf(false) }
    val isSet = shown?.isSet == true
    val scope = rememberCoroutineScope()
    val pick = {
        scope.launch {
            val photo = pickPhoto?.invoke() ?: return@launch
            draft = draft.copy(kind = AvatarKind.Photo, source = PhotoSource.Upload, photo = photo)
        }
    }
    SheetFrame(
        "Avatar",
        onDismiss = onDismiss,
        onDone = {
            val avatar = draft.avatar
            if (resetting) {
                onSave(null)
            } else if (avatar != null) {
                onSave(avatar)
            }
            onDismiss()
        },
        doneEnabled = draft.ready,
        doneLabel = "Save",
        leadingAction =
            if (isSet) {
                { done ->
                    SheetHeaderAction("Reset", {
                        resetting = true
                        done()
                    })
                }
            } else {
                null
            },
    ) {
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PersonAvatar(draft.preview(shown), AvatarTokens.editorSize)
            Text(label, color = OtusColors.text2, textAlign = TextAlign.Center)
            SegmentedToggle(kinds.map { it.label }, kinds.indexOf(draft.kind), { draft = draft.copy(kind = kinds[it]) })
            AvatarChoices(
                draft,
                domain,
                onChange = { draft = it },
                onPickPhoto = if (pickPhoto != null) ({ pick() }) else null,
                Modifier.fillMaxWidth(),
            )
        }
    }
}
