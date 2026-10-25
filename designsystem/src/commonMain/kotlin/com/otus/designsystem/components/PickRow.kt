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
 * of up to [subtitleLines] lines, and a radio or checkbox in the selected-chip colors. A null
 * [onClick] shows the row disabled. A [RowDensity.Compact] row is only a touch target tall, for
 * long lists of short choices.
 */
@Composable
public fun PickRow(
    title: String,
    subtitle: String?,
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitleLines: Int = 1,
    density: RowDensity = RowDensity.Regular,
    leading: (@Composable () -> Unit)? = null,
) {
    PickRowFrame(mark, onClick, modifier.heightIn(min = density.pickRowHeight), leading) {
        PickRowText(title, subtitle, subtitleLines)
    }
}

/**
 * A [PickRow] whose choice is an address: the [person] pill, so a long address stays on one line,
 * over a smaller `text2` [subtitle], such as which card it comes from.
 */
@Composable
public fun PersonPickRow(
    person: Recipient,
    subtitle: String?,
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    density: RowDensity = RowDensity.Regular,
) {
    PickRowFrame(mark, onClick, modifier.heightIn(min = density.pickRowHeight), leading = null) {
        PersonChip(person)
        subtitle?.let {
            Text(
                it,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = SheetTokens.detailSize,
                maxLines = 1,
            )
        }
    }
}

/** The [title] and smaller `text2` [subtitle] of a [PickRow] or [FilterPickRow]. */
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
 * The frame of a [PickRow] and its relatives, such as [FolderPickRow]: the [leading] slot, the
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
 * checkbox fills with `accentDark` inside an `accentLight` outline and a chosen radio turns
 * `accentLight`, as a selected chip does.
 */
@Composable
public fun PickMarkView(mark: PickMark) {
    Canvas(Modifier.size(SignatureTokens.markSize)) {
        val stroke = SignatureTokens.markStroke.toPx()
        val inset = Offset(stroke / 2, stroke / 2)
        val inner = size.copy(size.width - stroke, size.height - stroke)
        // Chosen marks take the selected-chip colors: an accent fill or dot inside an accentLight
        // outline. Unchosen ones keep a quiet text3 outline, so the choice stands out.
        val outline = if (mark.selected) OtusColors.accentLight else OtusColors.text3
        if (mark.multiple) {
            val corner = CornerRadius(SettingTokens.valueRadius.toPx() / 2)
            if (mark.selected) {
                drawRoundRect(OtusColors.accentDark, cornerRadius = corner)
                val check =
                    Path().apply {
                        moveTo(size.width * CHECK_START_X, size.height * CHECK_MID_Y)
                        lineTo(size.width * CHECK_LOW_X, size.height * CHECK_LOW_Y)
                        lineTo(size.width * CHECK_END_X, size.height * CHECK_END_Y)
                    }
                drawPath(check, OtusColors.text1, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            drawRoundRect(outline, inset, inner, corner, Stroke(stroke))
        } else {
            drawCircle(outline, radius = size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
            if (mark.selected) drawCircle(OtusColors.accentLight, radius = SignatureTokens.markDot.toPx() / 2)
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
