package com.otus.designsystem.shared.rows.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.shared.controls.elements.LeadingSymbol
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/** A tappable row that opens something: a subpage, a choice, or an editor. */
@Composable
public fun ActionRow(
    title: String,
    onClick: () -> Unit,
    trailing: DrawableResource,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: DrawableResource? = null,
    mirrorTrailingInRtl: Boolean = false,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = SmoothCornerShape(SheetTokens.rowRadius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SheetTokens.rowHeight)
            .clip(shape)
            .background(OtusColors.bgDark)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, shape)
            .padding(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
    ) {
        if (icon != null) LeadingSymbol(icon)
        Column(Modifier.weight(1f)) {
            Text(title, style = panelTextStyle())
            if (summary != null) {
                Text(summary, style = panelTextStyle(), fontSize = SheetTokens.detailSize, color = OtusColors.text2)
            }
        }
        Symbol(trailing, mirrorInRtl = mirrorTrailingInRtl)
    }
}
