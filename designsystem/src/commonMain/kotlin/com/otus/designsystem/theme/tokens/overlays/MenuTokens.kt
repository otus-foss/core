package com.otus.designsystem.theme.tokens.overlays

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.typography.TypeScale

/** The small menu that opens where the user pressed. */
public object MenuTokens {
    public val radius: Dp = CornerTokens.surface
    public val minWidth: Dp = 180.dp
    public val inset: Dp = 4.dp
    public val rowPadding: Dp = Spacing.gutter

    /** The tallest a dropdown grows before its rows scroll. */
    public val dropdownMaxHeight: Dp = 480.dp

    /** The least room below its anchor a dropdown opens into, about three rows, before it may open above. */
    public val dropdownMinRoom: Dp = 160.dp

    /** The widest a dropdown row's end detail grows, such as an address, so its title keeps room. */
    public val detailMaxWidth: Dp = 200.dp

    /** Between a row's icon and its label. */
    public val iconGap: Dp = 12.dp

    /** A row's smaller second line, such as an option's detail. */
    public val detailSize: TextUnit = TypeScale.detail

    /** How far a [com.otus.designsystem.shared.actionbar.components.QuickActionBar] sits from the pressed point. */
    public val barGap: Dp = 12.dp

    /** The space the menu keeps from the screen edges. */
    public val edge: Dp = 8.dp

    /** The more-options menu under the panel's ⋯ button. */
    public val optionsMenuWidth: Dp = 286.dp
}
