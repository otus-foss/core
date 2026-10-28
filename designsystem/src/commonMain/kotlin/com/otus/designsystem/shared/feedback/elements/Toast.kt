package com.otus.designsystem.shared.feedback.elements

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.feedback.ToastTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A short message in a pill, with an optional [action] at its end. A ring beside the action's
 * label shows the [remaining] share of the toast's time. A [lifted] toast, one being dragged,
 * draws a shade lighter.
 */
@Composable
public fun Toast(
    text: String,
    action: ToastAction?,
    remaining: Float,
    modifier: Modifier = Modifier,
    lifted: Boolean = false,
) {
    val fill by animateColorAsState(
        if (lifted) OtusColors.bgRegular else OtusColors.bgDark,
        // Slow enough that lifting the whole toast reads as a gentle change, not a flash.
        motionTween(MotionSpeed.Normal),
        label = "Toast fill",
    )
    // A one-line toast is a pill; a longer text grows it to up to [ToastTokens.MAX_LINES] lines with
    // the same corners, and the action keeps the full height beside it.
    val shape = RoundedCornerShape(ToastTokens.height / 2)
    Row(
        modifier
            .heightIn(min = ToastTokens.height)
            .height(IntrinsicSize.Min)
            .background(fill, shape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .padding(start = ToastTokens.textInset, end = if (action == null) ToastTokens.textInset else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            Modifier.weight(1f).padding(vertical = ToastTokens.textPadding),
            style = panelTextStyle(),
            maxLines = ToastTokens.MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        action?.let { ToastButton(it, remaining, if (lifted) OtusColors.bgLight else OtusColors.bgRegular) }
    }
}

/** The action, with a touch area the toast's full height around its visible pill. */
@Composable
private fun ToastButton(
    action: ToastAction,
    remaining: Float,
    fill: Color,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        Modifier
            .fillMaxHeight()
            .clickable(interactions, indication = null, role = Role.Button, onClick = action.onAction)
            .padding(ToastTokens.actionInset),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .height(ToastTokens.height - ToastTokens.actionInset * 2)
                .controlPressFeedback(interactions)
                .background(fill, CircleShape)
                .padding(horizontal = ToastTokens.actionPadding),
            horizontalArrangement = Arrangement.spacedBy(ToastTokens.actionGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimerRing(remaining)
            Text(action.label, style = panelTextStyle(), maxLines = 1)
        }
    }
}

/** An arc that shrinks from a full circle as the toast's time runs out. */
@Composable
private fun TimerRing(remaining: Float) {
    val color = OtusColors.text1
    Canvas(Modifier.size(ToastTokens.ringSize)) {
        val stroke = ToastTokens.ringStroke.toPx()
        drawArc(
            color,
            startAngle = -90f,
            sweepAngle = 360f * remaining.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}
