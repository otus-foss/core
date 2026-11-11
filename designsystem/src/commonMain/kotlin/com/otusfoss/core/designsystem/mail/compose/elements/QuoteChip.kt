package com.otusfoss.core.designsystem.mail.compose.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_more_horiz
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.editing.ComposeTokens
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * The quoted message at the end of a reply or forward while it is collapsed, such as
 * "Grace Hopper wrote on 11 Oct" after a ⋯. Tapping it runs [onExpand].
 */
@Composable
public fun QuoteChip(
    label: String,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = SmoothCornerShape(ComposeTokens.quoteRadius)
    Row(
        modifier
            .padding(horizontal = PageTokens.textInset)
            .heightIn(min = Sizes.touchTarget)
            .strokeBorder(Sizes.borderWidth, OtusColors.bgLight, shape)
            .clickable(onClickLabel = "Show quoted text", role = Role.Button, onClick = onExpand)
            .padding(horizontal = ControlTokens.chipPadding),
        horizontalArrangement = Arrangement.spacedBy(ControlTokens.chipPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_more_horiz)
            Text(
                label,
                style = panelTextStyle(),
                color = OtusColors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
