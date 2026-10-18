package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The page for writing a message, from the "Write" boards. */
public object ComposeTokens {
    /** Space between a field's label and its value. */
    public val labelGap: Dp = 4.dp

    /** The label column of To, Cc, Bcc, and Subject, so their values line up. */
    public val labelWidth: Dp = 56.dp

    /** The `bgLight` line under each header field. */
    public val hairlineWidth: Dp = 1.dp

    /** A recipient chip: its avatar, corner radius, height, and the gaps between wrapped chips. */
    public val recipientAvatarSize: Dp = 22.dp
    public val recipientRadius: Dp = 8.dp
    public val recipientHeight: Dp = 32.dp
    public val recipientGap: Dp = 6.dp

    /** The panel's Send-from pill: its avatar, and the width past which its address ends in an ellipsis. */
    public val fromAvatarSize: Dp = 28.dp
    public val fromLabelMaxWidth: Dp = 180.dp

    /** A row in a dropdown of addresses: its avatar and the smaller line under the address. */
    public val dropdownAvatarSize: Dp = 32.dp
    public val detailSize: TextUnit = 13.sp

    /** The formatting row docked above the keyboard. */
    public val formatBarHeight: Dp = 56.dp
    public val formatBarInset: Dp = 4.dp
    public val formatButtonSize: Dp = 40.dp
    public val formatButtonRadius: Dp = 12.dp
    public val formatIconSize: Dp = 24.dp
    public val formatDividerHeight: Dp = 24.dp

    /** The more-options menu under the panel's ⋯ button. */
    public val optionsMenuWidth: Dp = 286.dp

    /** An image in the message body: the box shown while it loads, its narrowest size, and its presets. */
    public val inlineImageLoadingHeight: Dp = 160.dp
    public val inlineImageMinWidth: Dp = 48.dp
    public val inlineImageSmall: Dp = 120.dp
    public val inlineImageMedium: Dp = 200.dp

    /** The widest an attachment chip grows before its name ends in an ellipsis. */
    public val attachmentMaxWidth: Dp = 280.dp

    /** The collapsed quote at the end of a reply. */
    public val quoteRadius: Dp = 8.dp
}
