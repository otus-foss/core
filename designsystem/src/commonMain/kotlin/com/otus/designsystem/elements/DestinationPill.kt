package com.otus.designsystem.elements

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.MoveTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A place to move mail to with one tap, such as Inbox or Archive in the Move to sheet. It is
 * smaller than a [SidebarPill], so a row of them leaves room for the sort button at its end.
 */
@Composable
public fun DestinationPill(
    label: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipFrame(
        onClick,
        modifier,
        selected = null,
        enabled = true,
        fill = OtusColors.bgRegular,
        layout = ChipLayout(height = MoveTokens.pillHeight, start = MoveTokens.pillStart, end = MoveTokens.pillEnd),
    ) {
        Symbol(icon, size = MoveTokens.iconSize)
        Text(label, style = panelTextStyle(), color = OtusColors.text1, fontSize = MoveTokens.labelSize, maxLines = 1)
    }
}
