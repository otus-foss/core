package com.otusfoss.core.designsystem.mail.inbox.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.rows.FolderTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.InboxTokens

/**
 * A small pill that hands over what a message is for straight from the mailbox, without opening
 * it, such as its one-time code to copy. It is as tall as a row's text line, so a row that shows it
 * keeps its height; Compose widens its touch area to the minimum touch target without changing
 * the layout.
 */
@Composable
public fun MailboxQuickUse(
    action: UiAction,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        modifier
            .clickable(
                interactions,
                indication = null,
                enabled = action.enabled,
                role = Role.Button,
                onClick = action.onClick,
            ).controlPressFeedback(interactions),
        shape = CircleShape,
        color = OtusColors.bgDark,
        contentColor = if (action.enabled) OtusColors.text1 else OtusColors.text3,
        border = BorderStroke(Sizes.borderWidth, OtusColors.bgLight),
    ) {
        Row(
            Modifier
                .height(InboxTokens.quickUseHeight)
                .padding(horizontal = InboxTokens.quickUsePadding),
            horizontalArrangement = Arrangement.spacedBy(InboxTokens.markGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            action.symbol?.let { Symbol(it, size = FolderTokens.markSize) }
            Text(action.label, fontSize = InboxTokens.quickUseTextSize, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}
