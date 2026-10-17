package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/** [rows] on one `bgRegular` card with rounded corners, a `bgLight` line between each two. */
@Composable
public fun RowGroup(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(SheetTokens.rowRadius)).background(OtusColors.bgRegular)) {
        rows.forEachIndexed { index, row ->
            if (index > 0) Box(Modifier.fillMaxWidth().height(PanelTokens.borderWidth).background(OtusColors.bgLight))
            row()
        }
    }
}

/**
 * A row of a [RowGroup]: an [icon], the [label], and a `text2` [value] at its end, such as a time,
 * or a chevron when it [opens] something more.
 */
@Composable
public fun GroupRow(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    opens: Boolean = false,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SheetTokens.rowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = SheetTokens.inset),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.inset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Symbol(icon)
        Text(label, Modifier.weight(1f), style = panelTextStyle())
        value?.let { Text(it, style = panelTextStyle(), color = OtusColors.text2) }
        if (opens) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
            }
        }
    }
}
