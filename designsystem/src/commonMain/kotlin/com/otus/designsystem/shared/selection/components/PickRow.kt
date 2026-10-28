package com.otus.designsystem.shared.selection.components

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
import com.otus.designsystem.shared.rows.components.RowDensity
import com.otus.designsystem.theme.colors.OtusAlpha
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.editing.ComposeTokens
import com.otus.designsystem.theme.tokens.editing.SignatureTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.settings.SettingTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A choice in a sheet list: an optional [leading] avatar, the [title], a smaller `text2` [subtitle],
 * and a radio or checkbox in the selected-chip colors. A null [onClick] shows the row disabled.
 * [layout] sets its height and how many lines the subtitle may take.
 */
@Composable
public fun PickRow(
    title: String,
    subtitle: String?,
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    layout: PickRowLayout = PickRowLayout(),
    leading: (@Composable () -> Unit)? = null,
) {
    PickRowFrame(mark, onClick, modifier.heightIn(min = layout.density.pickRowHeight), leading) {
        PickRowText(title, subtitle, layout.subtitleLines)
    }
}

/**
 * The [title] and smaller `text2` [subtitle] of a [PickRow] or
 * [FilterPickRow][com.otus.designsystem.shared.filters.components.FilterPickRow].
 */
@Composable
internal fun PickRowText(
    title: String,
    subtitle: String?,
    subtitleLines: Int = 1,
) {
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

/**
 * The frame of a [PickRow] and its relatives, such as
 * [FolderPickRow][com.otus.designsystem.mail.folders.components.FolderPickRow]: the [leading] slot, the
 * [content] column, an optional [trailing] slot, and the [markView] at the end, or first when
 * [mark] is [PickMark.atStart]. [modifier] sets the row's minimum height.
 */
@Composable
internal fun PickRowFrame(
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
    markView: @Composable () -> Unit = { PickMarkView(mark) },
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
            .then(choose)
            .alpha(if (enabled) 1f else OtusAlpha.DISABLED)
            .padding(horizontal = SheetTokens.inset, vertical = PanelTokens.iconGap),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mark.atStart) markView()
        leading?.invoke()
        Column(Modifier.weight(1f), content = content)
        trailing?.invoke()
        if (!mark.atStart) markView()
    }
}

/**
 * The radio or checkbox of a [PickRow] on its own, for rows built from other parts, such as a
 * folder tree row in the Folders sheet. Unchosen marks have a dim `text3` outline; a ticked
 * checkbox fills with `text1` around a `bgDark` tick, as in a `SelectionRow`, and a chosen radio
 * turns `text1`.
 */
@Composable
public fun PickMarkView(mark: PickMark) {
    Canvas(Modifier.size(SignatureTokens.markSize)) {
        val stroke = SignatureTokens.markStroke.toPx()
        val inset = Offset(stroke / 2, stroke / 2)
        val inner = size.copy(size.width - stroke, size.height - stroke)
        // Unchosen marks keep a quiet text3 outline, so the text1 of a chosen one stands out.
        val outline = if (mark.selected) OtusColors.text1 else OtusColors.text3
        if (mark.multiple) {
            val corner = CornerRadius(SettingTokens.valueRadius.toPx() / 2)
            if (mark.selected) {
                drawRoundRect(OtusColors.text1, cornerRadius = corner)
                val check =
                    Path().apply {
                        moveTo(size.width * CHECK_START_X, size.height * CHECK_MID_Y)
                        lineTo(size.width * CHECK_LOW_X, size.height * CHECK_LOW_Y)
                        lineTo(size.width * CHECK_END_X, size.height * CHECK_END_Y)
                    }
                drawPath(check, OtusColors.bgDark, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            drawRoundRect(outline, inset, inner, corner, Stroke(stroke))
        } else {
            drawCircle(outline, radius = size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
            if (mark.selected) drawCircle(OtusColors.text1, radius = SignatureTokens.markDot.toPx() / 2)
        }
    }
}

private const val CHECK_START_X = 0.25f
private const val CHECK_MID_Y = 0.52f
private const val CHECK_LOW_X = 0.43f
private const val CHECK_LOW_Y = 0.7f
private const val CHECK_END_X = 0.76f
private const val CHECK_END_Y = 0.32f

/** A pick row's least height: a full row, or a touch target for long lists of short choices. */
private val RowDensity.pickRowHeight
    get() = if (this == RowDensity.Compact) PanelTokens.touchTarget else SheetTokens.rowHeight

/**
 * How a [PickRow] is laid out: a [RowDensity.Compact] row is only a touch target tall, for long
 * lists of short choices, and the subtitle takes up to [subtitleLines] lines.
 */
public data class PickRowLayout(
    val density: RowDensity = RowDensity.Regular,
    val subtitleLines: Int = 1,
)
