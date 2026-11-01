package com.otus.designsystem.mail.compose.elements

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
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.shapes.strokeBorder
import com.otus.designsystem.theme.tokens.editing.ComposeTokens
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

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
            .heightIn(min = PanelTokens.touchTarget)
            .strokeBorder(PanelTokens.borderWidth, OtusColors.bgLight, shape)
            .clickable(onClickLabel = "Show quoted text", role = Role.Button, onClick = onExpand)
            .padding(horizontal = PanelTokens.chipPadding),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipPadding),
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
