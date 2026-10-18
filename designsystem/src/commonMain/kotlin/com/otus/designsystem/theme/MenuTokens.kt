package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The small menu that opens where the user pressed. */
public object MenuTokens {
    public val radius: Dp = 16.dp
    public val minWidth: Dp = 180.dp
    public val inset: Dp = 4.dp
    public val rowPadding: Dp = 16.dp

    /** The tallest a dropdown grows before its rows scroll. */
    public val dropdownMaxHeight: Dp = 480.dp

    /** The least room below its anchor a dropdown opens into, about three rows, before it may open above. */
    public val dropdownMinRoom: Dp = 160.dp

    /** The widest a dropdown row's end detail grows, such as an address, so its title keeps room. */
    public val detailMaxWidth: Dp = 200.dp

    /** Between a row's icon and its label. */
    public val iconGap: Dp = 12.dp

    /** How far a [com.otus.designsystem.components.QuickActionBar] sits from the pressed point. */
    public val barGap: Dp = 12.dp

    /** The space the menu keeps from the screen edges. */
    public val edge: Dp = 8.dp
}
