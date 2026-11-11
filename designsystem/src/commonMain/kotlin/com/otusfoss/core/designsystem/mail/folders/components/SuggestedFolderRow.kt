package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_chevron_right
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.folders.elements.FolderPath
import com.otusfoss.core.designsystem.shared.people.components.PersonChip
import com.otusfoss.core.designsystem.shared.people.components.Recipient
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.editing.ComposeTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.people.PersonPillTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens

/**
 * A folder the sheet suggests, such as where a sender's mail usually goes: the [person] pill, an
 * arrow, and a pill with the [folder]'s glyph and path, on one line with no card around them. The
 * whole row is one button that runs [onClick]; screen readers hear [description].
 */
@Composable
public fun SuggestedFolderRow(
    person: Recipient,
    folder: FolderName,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(SmoothCornerShape(SheetTokens.rowRadius))
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            }.padding(vertical = SheetTokens.gap),
        horizontalArrangement = Arrangement.spacedBy(StripTokens.glyphGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonChip(person, Modifier.weight(1f, fill = false))
        CompositionLocalProvider(LocalContentColor provides OtusColors.text3) {
            Symbol(Res.drawable.symbol_chevron_right, size = Sizes.iconSize)
        }
        FolderPill(folder, Modifier.weight(1f, fill = false))
    }
}

/** The folder in the same outlined pill as a [PersonChip]: its glyph, then its path. */
@Composable
private fun FolderPill(
    folder: FolderName,
    modifier: Modifier,
) {
    Row(
        modifier
            .height(PersonPillTokens.recipientHeight)
            .border(Sizes.borderWidth, OtusColors.bgLight, CircleShape)
            .padding(horizontal = ControlTokens.chipPadding),
        horizontalArrangement = Arrangement.spacedBy(StripTokens.glyphGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderNameGlyph(folder, StripTokens.chevronSize)
        FolderPath(folder.path, fontSize = ComposeTokens.detailSize)
    }
}
