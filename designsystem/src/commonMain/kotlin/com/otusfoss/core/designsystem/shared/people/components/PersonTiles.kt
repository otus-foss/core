package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens
import com.otusfoss.core.designsystem.theme.tokens.people.PersonSheetTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/** One tile in [PersonTiles]: its [label] under a symbol or a number, and what a tap does. */
public sealed interface PersonTile {
    public val label: String
    public val onClick: () -> Unit

    /** A way to reach the person, such as Write; the one [primary] tile takes the accent. */
    public class Action(
        override val label: String,
        public val icon: DrawableResource,
        override val onClick: () -> Unit,
        public val primary: Boolean = false,
    ) : PersonTile

    /** How much mail there is, such as "48 Messages"; a tap shows that mail. */
    public class Count(
        override val label: String,
        public val count: Long,
        override val onClick: () -> Unit,
    ) : PersonTile
}

/** [tiles] side by side in equal widths, so leaving one out widens the others. */
@Composable
public fun PersonTiles(
    tiles: List<PersonTile>,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PersonSheetTokens.tileGap)) {
        tiles.forEach { tile -> Tile(tile, Modifier.weight(1f)) }
    }
}

@Composable
private fun Tile(
    tile: PersonTile,
    modifier: Modifier,
) {
    val primary = (tile as? PersonTile.Action)?.primary == true
    val shape = SmoothCornerShape(CornerTokens.row)
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier
            .height(PersonSheetTokens.tileHeight)
            .controlPressFeedback(interactions, shape)
            .clip(shape)
            .background(if (primary) OtusColors.accentDark else OtusColors.bgRegular)
            .border(Sizes.borderWidth, if (primary) OtusColors.accentRegular else OtusColors.strokeBgDark, shape)
            .clickable(interactions, indication = null, onClick = tile.onClick)
            .clearAndSetSemantics {
                contentDescription = if (tile is PersonTile.Count) "${tile.count} ${tile.label}" else tile.label
                role = Role.Button
                onClick {
                    tile.onClick()
                    true
                }
            },
        verticalArrangement = Arrangement.spacedBy(PersonSheetTokens.tileLabelGap, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (tile) {
            is PersonTile.Action -> {
                CompositionLocalProvider(LocalContentColor provides OtusColors.text1) { Symbol(tile.icon) }
            }

            is PersonTile.Count -> {
                Text(
                    tile.count.toString(),
                    style = panelTextStyle(),
                    fontSize = PersonSheetTokens.countSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
        Text(
            tile.label,
            style = panelTextStyle(),
            color = if (primary) OtusColors.text1 else OtusColors.text2,
            fontSize = PersonSheetTokens.labelSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
