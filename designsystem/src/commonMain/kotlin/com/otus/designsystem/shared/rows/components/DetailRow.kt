package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.rows.elements.nestIndent
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/** A pressable row with caller-supplied details and optional tree indentation. */
@Composable
public fun DetailRow(
    title: String,
    details: List<String>,
    onClick: () -> Unit,
    enabled: Boolean = true,
    depth: Int = 0,
    maxLines: Int = 2,
    leading: @Composable () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = SmoothCornerShape(SheetTokens.rowRadius)
    Row(
        Modifier
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
        Box(Modifier.nestIndent(PageTokens.nestIndent, depth)) { leading() }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) OtusColors.text1 else OtusColors.text2,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
            details.forEach {
                Text(
                    it,
                    fontSize = SheetTokens.detailSize,
                    color = OtusColors.text2,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
