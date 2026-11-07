package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A round pill that shows only [icon] and turns an option on or off, to sit beside
 * [SidebarPill][com.otus.designsystem.shared.sidebar.elements.SidebarPill]s.
 * Screen readers read [description] as its name. [size] matches it to smaller pills beside it,
 * such as [DestinationPill][com.otus.designsystem.shared.folders.elements.DestinationPill]s.
 * [fill] matches it to the pills beside it, such as `bgDark` on a panel of `bgDark` pills.
 */
@Composable
public fun IconPill(
    description: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = PanelTokens.chipHeight,
    fill: Color = OtusColors.bgRegular,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = description },
        selected,
        enabled = true,
        fill = fill,
        layout = ChipLayout(ChipWidth.Circle, height = size),
    ) { Symbol(icon) }
}
