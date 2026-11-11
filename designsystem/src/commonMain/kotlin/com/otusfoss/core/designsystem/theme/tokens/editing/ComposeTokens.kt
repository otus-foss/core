package com.otusfoss.core.designsystem.theme.tokens.editing

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens
import com.otusfoss.core.designsystem.theme.typography.TypeScale

/** The page for writing a message, from the "Write" boards. */
public object ComposeTokens {
    /** Space between a field's label and its value. */
    public val labelGap: Dp = 4.dp

    /** The label column of To, Cc, Bcc, and Subject, so their values line up. */
    public val labelWidth: Dp = 56.dp

    /** The `bgLight` line under each header field. */
    public val hairlineWidth: Dp = 1.dp

    public val recipientRadius: Dp = CornerTokens.chip
    public val recipientGap: Dp = 6.dp

    /** The smaller line under an address in a dropdown of addresses. */
    public val detailSize: TextUnit = TypeScale.detail

    /** The formatting row docked above the keyboard. */
    public val formatBarHeight: Dp = 56.dp
    public val formatBarInset: Dp = 4.dp
    public val formatButtonSize: Dp = 40.dp
    public val formatButtonRadius: Dp = formatButtonSize / 2
    public val formatIconSize: Dp = 24.dp
    public val formatDividerHeight: Dp = 24.dp

    /** An image in the message body: the box shown while it loads, its narrowest size, and its presets. */
    public val inlineImageLoadingHeight: Dp = 160.dp
    public val inlineImageMinWidth: Dp = 48.dp
    public val inlineImageSmall: Dp = 120.dp
    public val inlineImageMedium: Dp = 200.dp
    public val inlineImageLarge: Dp = 280.dp

    /** The size guides while an image is dragged: how far past the image they reach, and their dashes. */
    public val guideReach: Dp = 16.dp
    public val guideDash: Dp = 4.dp

    /** The widest an attachment chip grows before its name ends in an ellipsis. */
    public val attachmentMaxWidth: Dp = 280.dp

    /** The collapsed quote at the end of a reply. */
    public val quoteRadius: Dp = CornerTokens.chip
}
