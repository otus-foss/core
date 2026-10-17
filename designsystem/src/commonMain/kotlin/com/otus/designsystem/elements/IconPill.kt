package com.otus.designsystem.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.theme.OtusColors
import org.jetbrains.compose.resources.DrawableResource

/**
 * A round pill that shows only [icon] and turns an option on or off, to sit beside [SidebarPill]s.
 * Screen readers read [description] as its name.
 */
@Composable
public fun IconPill(
    description: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = description },
        selected,
        enabled = true,
        fill = OtusColors.bgRegular,
        layout = ChipLayout(ChipWidth.Circle),
    ) { Symbol(icon) }
}
