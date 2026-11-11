package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.otusfoss.core.designsystem.resources.symbol_chevron_right
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A row that opens a person's or an address's page: the [leading] avatar, the [title], a
 * [subtitle] under it in `text2`, or in `text3` when it only says something is [missing], such as
 * "No signature", and a chevron.
 */
@Composable
public fun PersonRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    missing: Boolean = false,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SheetTokens.rowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PageTokens.textInset, vertical = PageTokens.rowGap),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.inset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Column(Modifier.weight(1f)) {
            Text(title, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = panelTextStyle(),
                color = if (missing) OtusColors.text3 else OtusColors.text2,
                fontSize = SheetTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
        }
    }
}
