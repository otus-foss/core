package com.otusfoss.core.designsystem.shared.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.controls.components.ButtonPair
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.overlays.components.SheetHeader
import com.otusfoss.core.designsystem.shared.overlays.components.SheetSurface
import com.otusfoss.core.designsystem.shared.overlays.elements.sheetInset
import com.otusfoss.core.designsystem.shared.rows.elements.SectionCaption
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.settings.ChangeLogTokens

/**
 * A sheet that lists what changed, one captioned [sections] group of bulleted lines after another.
 * Under the list sit [secondaryLabel], such as "Changelog", and the wide primary [actionLabel],
 * such as "Install update". Its header has only the title; the scrim, Back, and swiping down close
 * it. With no sections it says so.
 */
@Composable
public fun ChangeLogSheet(
    title: String,
    sections: List<ChangeLogSection>,
    actionLabel: String,
    onAction: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    onDismiss: () -> Unit,
) {
    SheetSurface(title, onDismiss, onDone = onDismiss) { _, _, _ ->
        SheetHeader(title, onDone = null)
        Column(
            Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .sheetInset(),
        ) {
            ChangeSections(sections)
        }
        ButtonPair(
            UiAction(secondaryLabel, onClick = onSecondary),
            UiAction(actionLabel, onClick = onAction),
            Modifier.padding(SheetTokens.inset),
        )
    }
}

/** Each of [sections] as a caption over its bulleted changes, or a note when there are none. */
@Composable
internal fun ChangeSections(sections: List<ChangeLogSection>) {
    if (sections.isEmpty()) ChangeLine("No changes listed", bullet = false)
    sections.forEach { section ->
        SectionCaption(section.title)
        section.changes.forEach { ChangeLine(it) }
    }
}

@Composable
private fun ChangeLine(
    text: String,
    bullet: Boolean = true,
) {
    Row(
        Modifier.padding(horizontal = ChangeLogTokens.lineInset, vertical = ChangeLogTokens.lineGap),
        horizontalArrangement = Arrangement.spacedBy(ChangeLogTokens.bulletGap),
    ) {
        if (bullet) Text("•", color = OtusColors.text2)
        Text(text, color = if (bullet) OtusColors.text1 else OtusColors.text2)
    }
}
