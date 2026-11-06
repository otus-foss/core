package com.otus.designsystem.shared.controls.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlin.math.roundToInt

/**
 * A way out and a way forward in one pill, such as Cancel and Delete. [secondary] sits in the
 * outlined track; the wider [primary] pill fills its end and widens for a long label. Each half
 * greys out while it is not enabled. Screen readers announce a changed primary label politely.
 */
@Composable
public fun ButtonPair(
    secondary: PairButton,
    primary: PairButton,
    modifier: Modifier = Modifier,
) {
    // The track paints behind its buttons and clips nothing, so the primary pill's outline stays on top.
    Layout(
        {
            SecondaryHalf(secondary)
            ActionButton(primary.label, primary.onClick, primary = true, enabled = primary.enabled) {
                PrimaryLabel(primary.label, primary.enabled, busy = false)
            }
        },
        modifier
            .fillMaxWidth()
            .pairTrack(),
    ) { (start, end), constraints ->
        val width = constraints.maxWidth
        val primaryWidth = pairPrimaryWidth(width, start.maxIntrinsicWidth(0), end.maxIntrinsicWidth(0))
        val secondaryWidth = width - primaryWidth
        val height =
            maxOf(start.minIntrinsicHeight(secondaryWidth), end.minIntrinsicHeight(primaryWidth))
                .coerceAtLeast(constraints.minHeight)
        val placedSecondary = start.measure(Constraints.fixed(secondaryWidth, height))
        val placedPrimary = end.measure(Constraints.fixed(primaryWidth, height))
        layout(width, height) {
            placedSecondary.place(0, 0)
            placedPrimary.place(secondaryWidth, 0)
        }
    }
}

/**
 * The primary pill's usual share of [width], widened so its label fits on one line and narrowed so
 * the secondary label does too. When both cannot fit, the short secondary label keeps one line, up
 * to its usual share, and the primary label wraps.
 */
internal fun pairPrimaryWidth(
    width: Int,
    secondaryNeeds: Int,
    primaryNeeds: Int,
): Int {
    val share = (width * PanelTokens.PAIR_PRIMARY_SHARE).roundToInt()
    return if (secondaryNeeds + primaryNeeds <= width) {
        share.coerceIn(primaryNeeds, width - secondaryNeeds)
    } else {
        width - secondaryNeeds.coerceAtMost(width - share)
    }
}

/** The track's fill and outline, drawn under its buttons, where `Modifier.border` would draw over them. */
private fun Modifier.pairTrack(): Modifier =
    drawBehind {
        val stroke = PanelTokens.borderWidth.toPx()
        drawRoundRect(OtusColors.bgDark, cornerRadius = CornerRadius(size.height / 2))
        drawRoundRect(
            OtusColors.strokeBgDark,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius((size.height - stroke) / 2),
            style = Stroke(stroke),
        )
    }

/** A primary button's label, with a progress ring after it while [busy]. */
@Composable
internal fun PrimaryLabel(
    label: String,
    enabled: Boolean,
    busy: Boolean,
) {
    val color = if (enabled) OtusColors.text1 else OtusColors.text3
    Row(
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = panelTextStyle(),
            color = color,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        if (busy) {
            CircularProgressIndicator(
                Modifier.size(PanelTokens.progressSize),
                color = color,
                strokeWidth = PanelTokens.progressStroke,
            )
        }
    }
}

@Composable
private fun SecondaryHalf(button: PairButton) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        Modifier
            .fillMaxHeight()
            .clickable(
                interactions,
                indication = null,
                enabled = button.enabled,
                role = Role.Button,
                onClick = button.onClick,
            ).controlPressFeedback(interactions, UnderPrimary)
            .padding(horizontal = SheetTokens.inset, vertical = PanelTokens.iconGap),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            button.label,
            style = panelTextStyle(),
            color = if (button.enabled) OtusColors.text2 else OtusColors.text3,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Round at the start like the track, square at the end, and running one height past the end, so a
 * press fills the track behind the primary pill's curve and the square end stays hidden under it.
 */
private val UnderPrimary =
    GenericShape { size, _ ->
        val radius = CornerRadius(size.height / 2)
        addRoundRect(
            RoundRect(
                Rect(0f, 0f, size.width + size.height, size.height),
                topLeft = radius,
                bottomLeft = radius,
            ),
        )
    }
