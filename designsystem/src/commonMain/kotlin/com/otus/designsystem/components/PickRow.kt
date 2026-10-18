package com.otus.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusAlpha
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.SignatureTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A choice in a sheet list: an optional [leading] avatar, the [title], a smaller `text2` [subtitle]
 * of up to [subtitleLines] lines, and a `text1` radio or checkbox, never the accent. A null
 * [onClick] shows the row disabled.
 */
@Composable
public fun PickRow(
    title: String,
    subtitle: String?,
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitleLines: Int = 1,
    leading: (@Composable () -> Unit)? = null,
) {
    PickRowFrame(mark, onClick, modifier, leading) {
        Text(title, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
        subtitle?.let {
            Text(
                it,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = ComposeTokens.detailSize,
                maxLines = subtitleLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The frame of a [PickRow] and its relatives, such as [FolderPickRow]: the [leading] slot, the
 * [content] column, an optional [trailing] slot, and the [mark] at the end, or first when it is
 * [PickMark.atStart]. Rows are at least [minHeight] tall.
 */
@Composable
internal fun PickRowFrame(
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
    minHeight: Dp = SheetTokens.rowHeight,
    content: @Composable ColumnScope.() -> Unit,
) {
    val enabled = onClick != null
    val choose =
        if (mark.multiple) {
            Modifier.toggleable(mark.selected, enabled, Role.Checkbox) { onClick?.invoke() }
        } else {
            Modifier.selectable(mark.selected, enabled, Role.RadioButton) { onClick?.invoke() }
        }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .then(choose)
            .alpha(if (enabled) 1f else OtusAlpha.DISABLED)
            .padding(horizontal = SheetTokens.inset, vertical = PanelTokens.iconGap),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mark.atStart) PickMarkView(mark)
        leading?.invoke()
        Column(Modifier.weight(1f), content = content)
        trailing?.invoke()
        if (!mark.atStart) PickMarkView(mark)
    }
}

@Composable
internal fun PickMarkView(mark: PickMark) {
    val color = OtusColors.text1
    Canvas(Modifier.size(SignatureTokens.markSize)) {
        val stroke = SignatureTokens.markStroke.toPx()
        if (mark.multiple) {
            val corner = CornerRadius(SettingTokens.valueRadius.toPx() / 2)
            drawRoundRect(
                color,
                Offset(stroke / 2, stroke / 2),
                size.copy(size.width - stroke, size.height - stroke),
                corner,
                Stroke(stroke),
            )
            if (mark.selected) {
                val check =
                    Path().apply {
                        moveTo(size.width * CHECK_START_X, size.height * CHECK_MID_Y)
                        lineTo(size.width * CHECK_LOW_X, size.height * CHECK_LOW_Y)
                        lineTo(size.width * CHECK_END_X, size.height * CHECK_END_Y)
                    }
                drawPath(check, color, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        } else {
            drawCircle(color, radius = size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
            if (mark.selected) drawCircle(color, radius = SignatureTokens.markDot.toPx() / 2)
        }
    }
}

private const val CHECK_START_X = 0.25f
private const val CHECK_MID_Y = 0.52f
private const val CHECK_LOW_X = 0.43f
private const val CHECK_LOW_Y = 0.7f
private const val CHECK_END_X = 0.76f
private const val CHECK_END_Y = 0.32f
