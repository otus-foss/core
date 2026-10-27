package com.otus.accounts.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.accounts.presentation.AvatarColors
import com.otus.accounts.presentation.AvatarDraft
import com.otus.accounts.presentation.AvatarKind
import com.otus.accounts.presentation.PhotoSource
import com.otus.avatar.MAX_AVATAR_LETTERS
import com.otus.avatar.avatarLetters
import com.otus.designsystem.shared.controls.elements.SymbolSwatch
import com.otus.designsystem.shared.input.elements.InputField
import com.otus.designsystem.shared.people.elements.AvatarSymbols
import com.otus.designsystem.shared.selection.components.PickMark
import com.otus.designsystem.shared.selection.components.PickRow
import com.otus.designsystem.shared.settings.components.ColorSheet
import com.otus.designsystem.shared.settings.elements.AddSwatch
import com.otus.designsystem.shared.settings.elements.ColorSwatch
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.ThemeTokens
import com.otus.designsystem.theme.parseHexColor
import com.otus.designsystem.theme.toHex

/**
 * The choices of the draft's kind: where the photo comes from, or the colors and the icons or
 * letters. [domain] names the website whose icon Website icon shows. Without [onPickPhoto], Your
 * photo is missing.
 */
@Composable
internal fun AvatarChoices(
    draft: AvatarDraft,
    domain: String,
    onChange: (AvatarDraft) -> Unit,
    onPickPhoto: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap)) {
        if (draft.kind == AvatarKind.Photo) {
            PhotoSources(draft, domain, onChange, onPickPhoto)
            return@Column
        }
        ColorChoices(draft.color) { onChange(draft.copy(color = it)) }
        if (draft.kind == AvatarKind.Icon) {
            FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(ThemeTokens.swatchGap)) {
                AvatarSymbols.icons.forEach { (name, symbol) ->
                    SymbolSwatch(symbol, name, name == draft.icon, { onChange(draft.copy(icon = name)) })
                }
            }
        } else {
            InputField(
                "Letters",
                draft.letters,
                { onChange(draft.copy(letters = avatarLetters(it))) },
                supportingText = "Up to $MAX_AVATAR_LETTERS characters.",
            )
        }
    }
}

@Composable
private fun PhotoSources(
    draft: AvatarDraft,
    domain: String,
    onChange: (AvatarDraft) -> Unit,
    onPickPhoto: (() -> Unit)?,
) {
    Column(Modifier.selectableGroup()) {
        if (onPickPhoto != null) {
            val hasPhoto = draft.photo != null || draft.savedPhoto != null
            PickRow(
                "Your photo",
                if (hasPhoto) "Tap again to choose another." else "Choose one from your photos.",
                PickMark(draft.source == PhotoSource.Upload),
                {
                    if (hasPhoto && draft.source != PhotoSource.Upload) {
                        onChange(draft.copy(source = PhotoSource.Upload))
                    } else {
                        onPickPhoto()
                    }
                },
            )
        }
        PickRow(
            "Website icon",
            "The icon of $domain.",
            PickMark(draft.source == PhotoSource.Website),
            { onChange(draft.copy(source = PhotoSource.Website)) },
        )
    }
}

/** The folder colors, the custom [color] when it is one, and a swatch that picks a custom color. */
@Composable
private fun ColorChoices(
    color: String,
    onPick: (String) -> Unit,
) {
    var custom by remember { mutableStateOf(false) }
    val customColor = parseHexColor(color)
    FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(ThemeTokens.swatchGap)) {
        AvatarColors.forEach { preset ->
            val tint = preset.tint ?: return@forEach
            ColorSwatch(tint, preset.label, preset.name == color, { onPick(preset.name) })
        }
        customColor?.let { ColorSwatch(it, "Custom color $color", selected = true, onClick = { custom = true }) }
        AddSwatch("Custom color", { custom = true })
    }
    if (custom) {
        val start = customColor ?: FolderColor.named(color).tint ?: OtusColors.accentRegular
        ColorSheet("Custom color", start, { onPick(it.toHex()) }, { custom = false })
    }
}
