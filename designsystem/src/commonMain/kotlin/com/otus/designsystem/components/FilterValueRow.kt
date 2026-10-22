package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A row of a [FilterRowCard] that opens a filter page, such as Folders: a `text2` [icon], the
 * [title], the page's current [value] at the end, and a chevron. The value is `text3` while the
 * page is at its default and `text2` once it is [set]. Screen readers hear the title, the
 * [spokenValue], such as "Clients and 4 more" for "Clients +4", and "opens page".
 */
@Composable
public fun FilterValueRow(
    icon: DrawableResource,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    set: Boolean = false,
    spokenValue: String = value,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(FilterTokens.valueRowHeight)
            .clearAndSetSemantics {
                contentDescription = "$title, $spokenValue, opens page"
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            }.clickable(onClick = onClick)
            .padding(horizontal = SheetTokens.inset),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(icon) }
        Text(title, style = panelTextStyle(), fontSize = FilterTokens.rowTitleSize, maxLines = 1)
        Text(
            value,
            Modifier.weight(1f),
            style = panelTextStyle(),
            color = if (set) OtusColors.text2 else OtusColors.text3,
            fontSize = FilterTokens.chipTextSize,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CompositionLocalProvider(LocalContentColor provides OtusColors.text3) {
            Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
        }
    }
}
