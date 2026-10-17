package com.otus.designsystem.elements

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.panelTextStyle

/** A pill that acts, such as "+ Cc", with an optional [leading] icon. Screen readers read [description]. */
@Composable
public fun ButtonPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String = label,
    leading: (@Composable () -> Unit)? = null,
) {
    ChipFrame(onClick, modifier.semantics { contentDescription = description }, null, true, OtusColors.bgDark) {
        leading?.invoke()
        ChipText(label)
    }
}

/**
 * The address a message is sent from as a pill: its [avatar], the [address], which ends with an
 * ellipsis past [labelMaxWidth], and a down chevron, since tapping it opens the choice. Screen
 * readers read [description].
 */
@Composable
public fun AddressPill(
    address: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelMaxWidth: Dp = ComposeTokens.fromLabelMaxWidth,
    avatar: @Composable () -> Unit,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = description },
        null,
        true,
        OtusColors.bgDark,
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
        Symbol(Res.drawable.symbol_expand_more)
    }
}
