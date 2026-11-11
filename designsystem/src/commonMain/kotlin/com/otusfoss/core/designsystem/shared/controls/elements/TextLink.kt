package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A quiet action as underlined `text2` text, one touch target tall, for a question or a way out
 * that should not compete with the buttons beside it, such as "Which providers work?".
 */
@Composable
public fun TextLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .heightIn(min = PageTokens.linkHeight)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions)
            .padding(horizontal = Spacing.l),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = PageTokens.captionSize,
            textDecoration = TextDecoration.Underline,
        )
    }
}
