package com.otus.designsystem.shared.folders.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.ChipFrame
import com.otus.designsystem.shared.controls.elements.ChipLayout
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.MoveTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A place to move mail to with one tap, such as Inbox or Archive in the Move to sheet. It is
 * smaller than a [SidebarPill][com.otus.designsystem.shared.sidebar.elements.SidebarPill], so a row of them leaves
 * room for the sort button at its end. A
 * long label ends in an ellipsis. A non-null [selected] makes it a toggle, filled with the accent
 * while on, as in the Folders sheet, where it adds mail to the folder.
 */
@Composable
public fun DestinationPill(
    label: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean? = null,
) {
    ChipFrame(
        onClick,
        modifier,
        selected = selected,
        enabled = true,
        fill = OtusColors.bgRegular,
        layout = ChipLayout(height = MoveTokens.pillHeight, start = MoveTokens.pillStart, end = MoveTokens.pillEnd),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MoveTokens.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Symbol(icon, size = MoveTokens.iconSize)
            Text(
                label,
                Modifier.weight(1f, fill = false),
                style = panelTextStyle(),
                color = OtusColors.text1,
                fontSize = MoveTokens.labelSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
