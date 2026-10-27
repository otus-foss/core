package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A round pill that shows only [icon] and turns an option on or off, to sit beside
 * [SidebarPill][com.otus.designsystem.shared.sidebar.elements.SidebarPill]s.
 * Screen readers read [description] as its name. [size] matches it to smaller pills beside it,
 * such as [DestinationPill][com.otus.designsystem.shared.folders.elements.DestinationPill]s.
 */
@Composable
public fun IconPill(
    description: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = PanelTokens.chipHeight,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = description },
        selected,
        enabled = true,
        fill = OtusColors.bgRegular,
        layout = ChipLayout(ChipWidth.Circle, height = size),
    ) { Symbol(icon) }
}
