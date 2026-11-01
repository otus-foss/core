package com.otus.designsystem.mail.quickview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_filter_list
import com.otus.designsystem.shared.controls.elements.CountBadge
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A quick view module of up to four [counts] as tiles: the icon over the label, and the count in
 * an accent badge in the corner. A tile with nothing in it hides its badge and dims.
 */
@Composable
public fun QuickCounts(
    counts: List<QuickCount>,
    modifier: Modifier = Modifier,
) {
    QuickModule(modifier, Arrangement.spacedBy(QuickViewTokens.tileGap)) {
        counts.forEach { count -> CountTile(count, Modifier.weight(1f)) }
    }
}

@Composable
private fun CountTile(
    count: QuickCount,
    modifier: Modifier,
) {
    Box(
        modifier
            .height(QuickViewTokens.tileHeight)
            .clip(TileShape)
            .background(OtusColors.bgRegular)
            .quickClick("${count.label}, ${count.count}", TileShape, count.onClick)
            .alpha(if (count.count == 0) QuickViewTokens.EMPTY_ALPHA else 1f),
    ) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(QuickViewTokens.tileLabelGap),
        ) {
            Symbol(count.icon)
            QuickLabel(count.label, OtusColors.text1)
        }
        if (count.count > 0) {
            CountBadge(
                count.count.toString(),
                Modifier.align(Alignment.TopEnd).padding(QuickViewTokens.badgeInset),
            )
        }
    }
}

/**
 * The Jump to row closing every quick view: [destinations], usually folder chips, in one line that
 * scrolls sideways and fades at its end, then the button that opens every filter.
 */
@Composable
public fun QuickJump(
    onFilters: () -> Unit,
    modifier: Modifier = Modifier,
    destinations: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier.fillMaxWidth().height(QuickViewTokens.jumpHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QuickViewTokens.tileGap),
    ) {
        Row(
            Modifier
                .weight(1f)
                .fadeEnd()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(QuickViewTokens.tileGap),
            verticalAlignment = Alignment.CenterVertically,
            content = destinations,
        )
        IconButton("All filters", onFilters) { Symbol(Res.drawable.symbol_filter_list) }
    }
}

@Composable
internal fun QuickModule(
    modifier: Modifier,
    arrangement: Arrangement.Horizontal,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier.fillMaxWidth().height(QuickViewTokens.moduleHeight),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
internal fun QuickLabel(
    text: String,
    color: Color,
) {
    Text(
        text,
        color = color,
        fontSize = QuickViewTokens.labelSize,
        lineHeight = QuickViewTokens.labelLineHeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = QuickViewTokens.tileGap / 2),
    )
}

/** A tap target read out as one [description], with the controls' press feedback in [shape]. */
@Composable
internal fun Modifier.quickClick(
    description: String,
    shape: Shape,
    onClick: () -> Unit,
): Modifier {
    val interactions = remember { MutableInteractionSource() }
    return controlPressFeedback(interactions, shape)
        .clickable(interactions, indication = null, onClick = onClick)
        .clearAndSetSemantics {
            contentDescription = description
            role = Role.Button
            onClick {
                onClick()
                true
            }
        }
}

/** Fades the content out toward its end edge, so a row that scrolls on shows it. */
@Composable
private fun Modifier.fadeEnd(): Modifier {
    val opaque = OtusColors.text1
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
        drawContent()
        val clear = opaque.copy(alpha = 0f)
        val brush =
            if (rtl) {
                Brush.horizontalGradient(0f to clear, 1f - QuickViewTokens.JUMP_FADE to opaque)
            } else {
                Brush.horizontalGradient(QuickViewTokens.JUMP_FADE to opaque, 1f to clear)
            }
        drawRect(brush, blendMode = BlendMode.DstIn)
    }
}

private val TileShape = RoundedCornerShape(CornerTokens.row)
