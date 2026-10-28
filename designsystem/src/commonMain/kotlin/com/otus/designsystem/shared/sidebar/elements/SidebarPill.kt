package com.otus.designsystem.shared.sidebar.elements

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.controls.elements.ChipFrame
import com.otus.designsystem.shared.controls.elements.ChipText
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A built-in mailbox in the sidebar: a [Chip][com.otus.designsystem.shared.controls.elements.Chip] filled
 * `bgRegular` to stand apart from the sidebar's
 * `bgDark`, with an optional [count] after the label in smaller `text2`.
 */
@Composable
public fun SidebarPill(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: String? = null,
) {
    ChipFrame(onClick, modifier, selected, enabled = true, fill = OtusColors.bgRegular) {
        Symbol(icon)
        ChipText(label)
        count?.let {
            Text(it, style = panelTextStyle(), color = OtusColors.text2, fontSize = PanelTokens.countSize, maxLines = 1)
        }
    }
}
