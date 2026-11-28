package com.otusfoss.core.designsystem.shared.rows.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.rows.EntryTokens

/**
 * One entry of a list, such as a mail or a person, on its own card: the [leading] avatar, which
 * turns into a check while the entry is selected, then the [content]. [taps] says what a tap and
 * a hold do. Space entries [EntryTokens.gap] apart; [entryNeighbors] works out each entry's
 * [neighbors], and [rowSelection] its [selection]. [size] sets the padding and the avatar's room.
 *
 * A selected card fills with the accent. Where [RowSelection.joinedAbove] or
 * [RowSelection.joinedBelow] says a selected neighbor touches it, the corners square off and the
 * fill reaches across the gap, so a run of selected entries reads as one block.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EntryCard(
    taps: EntryTaps,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selection: RowSelection? = null,
    neighbors: EntryNeighbors = EntryNeighbors(),
    size: EntrySize = EntrySize.Compact,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .entryCard(selection, neighbors)
            .semantics { if (selection != null) selected = selection.selected }
            .combinedClickable(role = Role.Button, onLongClick = taps.onLongClick, onClick = taps.onClick)
            .padding(
                start = EntryTokens.paddingStart,
                top = size.paddingVertical,
                end = EntryTokens.paddingEnd,
                bottom = size.paddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(EntryTokens.contentGap),
        verticalAlignment = verticalAlignment,
    ) {
        SelectableLeading(selection?.selected == true, size.avatarSize, leading = leading)
        content()
    }
}

/** What tapping an [EntryCard] does, and holding it, such as starting a selection. */
public class EntryTaps(
    public val onClick: () -> Unit,
    public val onLongClick: (() -> Unit)? = null,
)

/**
 * The card on the theme's raised color, filled with the accent while selected. It rounds less
 * toward its [neighbors], and not at all toward a joined selected one. On a
 * theme whose raised color is its page color, such as Black, an outline marks the card. Under
 * [LocalEntriesOnRaised] the two colors trade places, so the card stands out from a raised surface.
 */
@Composable
private fun Modifier.entryCard(
    selection: RowSelection?,
    neighbors: EntryNeighbors,
): Modifier {
    val selected = selection?.selected == true
    val raised = if (LocalEntriesOnRaised.current) OtusColors.bgRegular else OtusColors.bgDark
    val fill = animateColorAsState(if (selected) OtusColors.accentDark else raised, motionTween()).value
    val top = entryCorner(selection?.joinedAbove == true, neighbors.above)
    val bottom = entryCorner(selection?.joinedBelow == true, neighbors.below)
    // Only the lower entry of a joined pair reaches up, so the gap is covered once.
    val reach = animateDpAsState(if (selection?.joinedAbove == true) EntryTokens.gap else 0.dp, motionTween()).value
    val shape = SmoothCornerShape(top, top, bottom, bottom)
    val outlined = !selected && OtusColors.bgDark == OtusColors.bgRegular
    // Drawn behind rather than inside the clip, so the fill can reach past the top edge.
    return drawBehind {
        val up = reach.toPx()
        translate(top = -up) {
            drawOutline(shape.createOutline(Size(size.width, size.height + up), layoutDirection, this), fill)
        }
    }.entryOutline(outlined, OtusColors.strokeBgDark, shape)
        .clip(shape)
}

@Composable
private fun entryCorner(
    joined: Boolean,
    neighbor: Boolean,
): Dp =
    animateDpAsState(
        when {
            joined -> 0.dp
            neighbor -> EntryTokens.innerRadius
            else -> EntryTokens.radius
        },
        motionTween(),
    ).value

/** Whether entries sit on a raised surface, such as a grown control panel, rather than on the page. */
public val LocalEntriesOnRaised: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

private fun Modifier.entryOutline(
    outlined: Boolean,
    color: Color,
    shape: Shape,
): Modifier = if (outlined) strokeBorder(EntryTokens.outline, color, shape) else this
