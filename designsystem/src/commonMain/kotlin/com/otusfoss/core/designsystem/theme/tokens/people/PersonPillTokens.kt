package com.otusfoss.core.designsystem.theme.tokens.people

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.typography.TypeScale

/** Person pills: a recipient in a field, and the person in a quote header with its date toggle. */
internal object PersonPillTokens {
    /** The person pill in a quote header, its avatar, and the fold toggle's symbol. */
    public val quoteHeight: Dp = 26.dp
    public val quoteAvatarSize: Dp = 20.dp
    public val quoteStart: Dp = 3.dp
    public val quoteTextSize: TextUnit = TypeScale.label

    /** The chevron after a quote's date that shows the full date and time. */
    public val dateToggleSize: Dp = 16.dp

    /** A recipient chip: its avatar and height. */
    public val recipientAvatarSize: Dp = 22.dp
    public val recipientHeight: Dp = 32.dp
}
