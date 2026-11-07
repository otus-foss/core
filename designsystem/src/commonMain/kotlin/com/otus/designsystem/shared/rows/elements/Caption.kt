package com.otus.designsystem.shared.rows.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import com.otus.designsystem.theme.typography.TypeScale
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * Small `text2` over a group of rows in a sheet, menu, or list, on one line. Without [heading] it
 * is a note about them, or a notice under a page's header, and wraps. The caller places it; inside
 * a menu, [listCaptionInset] lines it up with the rows.
 */
@Composable
public fun Caption(
    text: String,
    modifier: Modifier = Modifier,
    heading: Boolean = true,
) {
    Text(
        text,
        if (heading) modifier.semantics { heading() } else modifier,
        style = panelTextStyle(),
        color = OtusColors.text2,
        fontSize = TypeScale.detail,
        maxLines = if (heading) 1 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Lines a [Caption] up with the rows of a menu or dropdown, with a little room above. */
public fun Modifier.listCaptionInset(): Modifier =
    padding(horizontal = MenuTokens.rowPadding - MenuTokens.inset)
        .padding(top = MenuTokens.rowPadding - MenuTokens.inset, bottom = Spacing.iconGap)
