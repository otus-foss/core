package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check_circle
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.overlays.components.ContextMenu
import com.otus.designsystem.shared.overlays.components.MenuAction
import com.otus.designsystem.shared.overlays.components.onPressDown
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.settings.ThemeTokens

/**
 * A theme to pick: a small sketch of a page drawn in [sketch]'s colors above [title] and
 * [description]. The selected card has an accent ring and a check; the card is one radio button.
 * With [actions], a long press opens them in a [ContextMenu] where the finger is, and the selected
 * card shows a more button that opens them in place of the check.
 */
@Composable
public fun ThemeCard(
    title: String,
    description: String,
    sketch: OtusPalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<MenuAction> = emptyList(),
) {
    var press by remember { mutableStateOf(Offset.Zero) }
    var menuAt by remember { mutableStateOf<Offset?>(null) }
    val openMenu = { menuAt = press }.takeIf { actions.isNotEmpty() }
    val shape = RoundedCornerShape(ThemeTokens.cardRadius)
    val interactions = remember { MutableInteractionSource() }
    // The menu opens in this box, which has no padding, so a press point maps onto it directly.
    Box(modifier.onPressDown { press = it }) {
        Column(
            Modifier
                .semantics { this.selected = selected }
                .combinedClickable(
                    interactions,
                    indication = null,
                    role = Role.RadioButton,
                    onLongClickLabel = openMenu?.let { "Theme actions" },
                    onLongClick = openMenu,
                    onClick = onClick,
                ).controlPressFeedback(interactions, shape)
                .background(OtusColors.bgDark, shape)
                .border(
                    if (selected) PanelTokens.borderWidth * 2 else PanelTokens.borderWidth,
                    if (selected) OtusColors.text1 else OtusColors.strokeBgDark,
                    shape,
                ).padding(ThemeTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap),
        ) {
            ThemeSketch(sketch)
            CardLabel(title, description, selected, openMenu)
        }
        menuAt?.let { ContextMenu(actions, it) { menuAt = null } }
    }
}

/** The title and description, then the check or, with [onMenu], the more button on a selected card. */
@Composable
private fun CardLabel(
    title: String,
    description: String,
    selected: Boolean,
    onMenu: (() -> Unit)?,
) {
    Row(
        Modifier.padding(horizontal = PanelTokens.iconGap).padding(bottom = PanelTokens.iconGap),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = ThemeTokens.cardTitleSize,
                lineHeight = ThemeTokens.cardTitleLineHeight,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                description,
                color = OtusColors.text2,
                fontSize = ThemeTokens.cardDetailSize,
                lineHeight = ThemeTokens.cardDetailLineHeight,
            )
        }
        when {
            selected && onMenu != null -> {
                MoreButton(title, onMenu)
            }

            selected -> {
                CompositionLocalProvider(LocalContentColor provides OtusColors.text1) {
                    Symbol(Res.drawable.symbol_check_circle)
                }
            }
        }
    }
}

/** A more button the size of the check it replaces, so the card keeps its height; its touch target overflows. */
@Composable
private fun MoreButton(
    title: String,
    onClick: () -> Unit,
) {
    Box(Modifier.size(PanelTokens.iconSize), contentAlignment = Alignment.Center) {
        IconButton(
            "Actions for $title",
            onClick,
            Modifier.requiredSize(PanelTokens.touchTarget),
            look = IconButtonLook.Plain,
        ) {
            Symbol(Res.drawable.symbol_more_horiz)
        }
    }
}

/** A page with a bar and two lines of text, the second led by the accent. */
@Composable
private fun ThemeSketch(sketch: OtusPalette) {
    val shape = RoundedCornerShape(ThemeTokens.sketchRadius)
    Column(
        Modifier
            .fillMaxWidth()
            .height(ThemeTokens.sketchHeight)
            .background(sketch.bgRegular, shape)
            .border(PanelTokens.borderWidth, sketch.strokeBgDark, shape)
            .padding(ThemeTokens.sketchPadding),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.sketchGap),
    ) {
        val bar = RoundedCornerShape(ThemeTokens.sketchBarRadius)
        Box(
            Modifier
                .fillMaxWidth()
                .height(ThemeTokens.sketchBarHeight)
                .background(sketch.bgDark, bar)
                .border(PanelTokens.borderWidth, sketch.strokeBgDark, bar),
        )
        SketchLine(sketch.bgLight, sketch.text2, ThemeTokens.SKETCH_LONG_LINE)
        SketchLine(sketch.accentLight, sketch.text1, ThemeTokens.SKETCH_SHORT_LINE)
    }
}

@Composable
private fun SketchLine(
    dot: Color,
    line: Color,
    length: Float,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ThemeTokens.sketchGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(ThemeTokens.sketchDot).background(dot, CircleShape))
        Box(Modifier.fillMaxWidth(length).height(ThemeTokens.sketchLineHeight).background(line, CircleShape))
    }
}
