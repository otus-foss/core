package com.otusfoss.core.designsystem.mail.compose.elements

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_expand_less
import com.otusfoss.core.designsystem.resources.symbol_expand_more
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrame
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrameStyle
import com.otusfoss.core.designsystem.shared.controls.elements.ChipLayout
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.people.SenderTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * The address a message is sent from as a pill: its [avatar], the [address], which ends with an
 * ellipsis past [labelMaxWidth], and a down chevron, since tapping it opens the choice. The
 * chevron points up while what it opens is [expanded]. Screen readers read [description].
 */
@Composable
public fun AddressPill(
    address: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelMaxWidth: Dp = SenderTokens.labelMaxWidth,
    expanded: Boolean = false,
    avatar: @Composable () -> Unit,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = description },
        selected = null,
        enabled = true,
        style = ChipFrameStyle(OtusColors.bgDark),
        layout = ChipLayout(avatarFirst = true),
    ) {
        avatar()
        Text(
            address,
            Modifier.widthIn(max = labelMaxWidth),
            style = panelTextStyle().copy(color = Color.Unspecified),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Symbol(if (expanded) Res.drawable.symbol_expand_less else Res.drawable.symbol_expand_more)
    }
}
