package com.otusfoss.core.designsystem.shared.panels.elements

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/** A page's title beside the control panel's chevron, such as "Settings" or a message's subject. */
@Composable
public fun PanelTitle(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    Text(
        text,
        modifier.semantics { heading() },
        style = panelTextStyle(),
        fontSize = PanelTokens.titleSize,
        lineHeight = PanelTokens.titleLineHeight,
        fontWeight = FontWeight.SemiBold,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = onTextLayout,
    )
}
