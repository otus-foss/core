package com.otusfoss.core.designsystem.theme.tokens.rows

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otusfoss.core.designsystem.theme.tokens.overlays.ActionBarTokens

/**
 * Strips and strip cards, such as the read view's strips above its action bar: their insets,
 * height, title, and the button and plain action at their end.
 */
public object StripTokens {
    /** A strip card: its corner, and its content inset from the start and end. */
    public val radius: Dp = ActionBarTokens.radius
    public val start: Dp = 12.dp
    public val end: Dp = 10.dp

    /** Between the parts of a strip row. */
    public val gap: Dp = 10.dp

    /** A strip with a title over a detail line, such as a thread position or a newsletter. */
    public val height: Dp = 60.dp
    public val titleSize: TextUnit = 15.sp
    public val chevronSize: Dp = 20.dp

    /** A newsletter strip's leading symbol. */
    public val iconSize: Dp = 24.dp

    /** The outlined button at the end of a strip, with its symbol and label. */
    public val buttonHeight: Dp = 40.dp
    public val buttonPadding: Dp = 12.dp
    public val buttonIconSize: Dp = 18.dp
    public val buttonTextSize: TextUnit = 14.sp

    /** The widest a strip button's label gets before it ends in an ellipsis. */
    public val buttonLabelMaxWidth: Dp = 140.dp

    /** A plain text action such as "Save all": its symbol and label. */
    public val plainIconSize: Dp = 18.dp
    public val plainTextSize: TextUnit = 14.sp
    public val plainPadding: Dp = 8.dp

    /** Between a small glyph and its label, such as a folder glyph and its name. */
    public val glyphGap: Dp = 6.dp
}
