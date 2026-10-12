package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PreviewTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SheetTokens

/**
 * A raised `bgRegular` card that previews part of a page, such as its action bar: a sketch of the
 * page starting with [marker], then [preview]. With a [title], a header names the page over a
 * `text2` [description]. With [onClick], the whole card is one button whose header ends in a
 * chevron, since it opens an editor, and screen readers skip the preview inside it. Without one,
 * the preview's own controls stay reachable, so an editor can make its buttons the controls.
 */
@Composable
public fun PreviewCard(
    marker: SketchMarker,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    preview: @Composable () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SheetTokens.rowRadius)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OtusColors.bgRegular)
            .then(
                onClick?.let {
                    Modifier
                        .clickable(interactions, indication = null, role = Role.Button, onClick = it)
                        .controlPressFeedback(interactions, shape)
                } ?: Modifier.semantics(mergeDescendants = true) {},
            ).padding(SheetTokens.gap),
        verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
    ) {
        title?.let { PreviewHeader(it, description, chevron = onClick != null) }
        PageSketch(marker)
        Box(if (onClick != null) Modifier.clearAndSetSemantics {} else Modifier) { preview() }
    }
}

@Composable
private fun PreviewHeader(
    title: String,
    description: String?,
    chevron: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingTokens.textGap)) {
            Text(title, fontSize = SettingTokens.titleSize, lineHeight = SettingTokens.titleLineHeight)
            description?.let {
                Text(
                    it,
                    color = OtusColors.text2,
                    fontSize = SettingTokens.descriptionSize,
                    lineHeight = SettingTokens.descriptionLineHeight,
                )
            }
        }
        if (chevron) Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
    }
}

/** Two text lines on a `bgDark` strip, after a dot for a list row. */
@Composable
private fun PageSketch(marker: SketchMarker) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PreviewTokens.sketchRadius))
            .background(OtusColors.bgDark)
            .clearAndSetSemantics {}
            .padding(PreviewTokens.sketchPadding),
        horizontalArrangement = Arrangement.spacedBy(PreviewTokens.sketchPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        marker.color?.let { Box(Modifier.size(PreviewTokens.dotSize).background(it, CircleShape)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PreviewTokens.lineGap)) {
            SketchLine(PreviewTokens.FIRST_LINE)
            SketchLine(PreviewTokens.SECOND_LINE)
        }
    }
}

@Composable
private fun SketchLine(width: Float) {
    Box(Modifier.fillMaxWidth(width).height(PreviewTokens.lineHeight).background(OtusColors.bgLight, CircleShape))
}

private val SketchMarker.color: Color?
    get() =
        when (this) {
            SketchMarker.None -> null
            SketchMarker.Row -> OtusColors.text3
            SketchMarker.Selected -> OtusColors.accentLight
        }
