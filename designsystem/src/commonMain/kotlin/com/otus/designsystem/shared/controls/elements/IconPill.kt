package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Sizes
import org.jetbrains.compose.resources.DrawableResource

/**
 * A round pill that shows only [icon] and turns an option on or off, to sit beside other pills.
 * Screen readers read [description] as its name. [size] matches it to smaller pills beside it.
 */
@Composable
public fun IconPill(
    description: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Sizes.chipHeight,
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
