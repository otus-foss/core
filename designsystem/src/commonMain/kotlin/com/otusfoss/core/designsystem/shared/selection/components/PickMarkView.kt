package com.otusfoss.core.designsystem.shared.selection.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_check
import com.otusfoss.core.designsystem.resources.symbol_check_circle_fill
import com.otusfoss.core.designsystem.resources.symbol_radio_button_unchecked
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.selection.PickTokens
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens

/**
 * The mark of a [PickRow] on its own, for rows built from other parts, such as a folder tree row.
 * Unchosen radios and checkboxes have a dim `text3` outline; a ticked checkbox fills with `text1`
 * around a `bgDark` tick, and a chosen radio turns `text1`. A check circle fills in `text1`. A
 * check keeps its room while unchosen, so moving it never rewraps a long label. A single-choice
 * [PickStyle.Card] draws nothing; its fill is the mark.
 */
@Composable
public fun PickMarkView(mark: PickMark) {
    when (mark.style) {
        PickStyle.Mark -> DrawnMark(mark)
        PickStyle.Card -> if (mark.multiple) DrawnMark(mark)
        PickStyle.CheckCircle -> CheckCircle(mark.selected)
        PickStyle.Check -> Box(Modifier.size(Sizes.iconSize)) { if (mark.selected) Symbol(Res.drawable.symbol_check) }
    }
}

@Composable
private fun CheckCircle(selected: Boolean) {
    CompositionLocalProvider(LocalContentColor provides if (selected) OtusColors.text1 else OtusColors.text3) {
        Symbol(if (selected) Res.drawable.symbol_check_circle_fill else Res.drawable.symbol_radio_button_unchecked)
    }
}

@Composable
private fun DrawnMark(mark: PickMark) {
    Canvas(Modifier.size(PickTokens.markSize)) {
        // Unchosen marks keep a quiet text3 outline, so the text1 of a chosen one stands out.
        val outline = if (mark.selected) OtusColors.text1 else OtusColors.text3
        if (mark.multiple) checkbox(mark.selected, outline) else radio(mark.selected, outline)
    }
}

private fun DrawScope.checkbox(
    selected: Boolean,
    outline: Color,
) {
    val stroke = PickTokens.markStroke.toPx()
    val corner = CornerRadius(SettingTokens.valueRadius.toPx() / 2)
    if (selected) {
        drawRoundRect(OtusColors.text1, cornerRadius = corner)
        val check =
            Path().apply {
                moveTo(size.width * CHECK_START_X, size.height * CHECK_MID_Y)
                lineTo(size.width * CHECK_LOW_X, size.height * CHECK_LOW_Y)
                lineTo(size.width * CHECK_END_X, size.height * CHECK_END_Y)
            }
        drawPath(check, OtusColors.bgDark, style = Stroke(stroke, cap = StrokeCap.Round))
    }
    drawRoundRect(
        outline,
        Offset(stroke / 2, stroke / 2),
        size.copy(size.width - stroke, size.height - stroke),
        corner,
        Stroke(stroke),
    )
}

private fun DrawScope.radio(
    selected: Boolean,
    outline: Color,
) {
    val stroke = PickTokens.markStroke.toPx()
    drawCircle(outline, radius = size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
    if (selected) drawCircle(OtusColors.text1, radius = PickTokens.markDot.toPx() / 2)
}

private const val CHECK_START_X = 0.25f
private const val CHECK_MID_Y = 0.52f
private const val CHECK_LOW_X = 0.43f
private const val CHECK_LOW_Y = 0.7f
private const val CHECK_END_X = 0.76f
private const val CHECK_END_Y = 0.32f
