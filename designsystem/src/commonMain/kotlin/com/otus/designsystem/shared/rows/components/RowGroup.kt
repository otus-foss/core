package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * [rows] on one `bgRegular` card with rounded corners, a `bgLight` line between each two. A
 * [raised] group sits on a page instead of a sheet, so it takes `bgDark` with a `strokeBgDark` outline.
 */
@Composable
public fun RowGroup(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    raised: Boolean = false,
) {
    val shape = SmoothCornerShape(SheetTokens.rowRadius)
    val line = if (raised) OtusColors.strokeBgDark else OtusColors.bgLight
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (raised) OtusColors.bgDark else OtusColors.bgRegular)
            .raisedOutline(raised, shape),
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) Box(Modifier.fillMaxWidth().height(PanelTokens.borderWidth).background(line))
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
        if (value != null) Text(value, style = panelTextStyle(), color = OtusColors.text2)
        if (opens) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
            }
        }
    }
}

private fun Modifier.raisedOutline(
    raised: Boolean,
    shape: Shape,
): Modifier = if (raised) border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape) else this
