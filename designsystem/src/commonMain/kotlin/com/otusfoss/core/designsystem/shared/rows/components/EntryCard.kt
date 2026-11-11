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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.colors.OtusAlpha
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.rows.EntryTokens

/**
 * One entry of a list, such as a mail or a person, on its own card: the [leading] avatar, which
 * turns into a check while the entry is selected, then the [content]. [taps] says what a tap and
 * a hold do. Space entries [EntryTokens.gap] apart; [rowSelection] works out each entry's
 * [selection].
 *
 * A selected card fills with the accent. Where [RowSelection.joinedAbove] or
 * [RowSelection.joinedBelow] says a selected neighbor touches it, the corners square off and the
 * fill reaches across the gap, so a run of selected entries reads as one block.
 *
 * [dimmed] fades the content and the card by the same amount, but never the selection fill; pass
 * false for a selected entry, so it reads clearly.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EntryCard(
    taps: EntryTaps,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selection: RowSelection? = null,
    dimmed: Boolean = false,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .entryCard(selection, dimmed)
            .semantics { if (selection != null) selected = selection.selected }
            .combinedClickable(role = Role.Button, onLongClick = taps.onLongClick, onClick = taps.onClick)
            // The fade's layer includes the padding, so an avatar's badge can overhang without being cut.
            .alpha(if (dimmed) OtusAlpha.DIMMED else 1f)
            .padding(
                start = EntryTokens.paddingStart,
                top = EntryTokens.paddingVertical,
                end = EntryTokens.paddingEnd,
                bottom = EntryTokens.paddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(EntryTokens.contentGap),
        verticalAlignment = verticalAlignment,
    ) {
        SelectableLeading(selection?.selected == true, EntryTokens.avatarSize, leading = leading)
        content()
    }
}

/** What tapping an [EntryCard] does, and holding it, such as starting a selection. */
public class EntryTaps(
    public val onClick: () -> Unit,
    public val onLongClick: (() -> Unit)? = null,
)

/**
 * The card on the theme's raised color, filled with the accent while selected. A [dimmed] card
 * fades toward the page as much as its text fades, so read and unread mail differ in both. On a
 * theme whose raised color is its page color, such as Black, an outline marks the card. Under
 * [LocalEntriesOnRaised] the two colors trade places, so the card stands out from a raised surface.
 */
@Composable
private fun Modifier.entryCard(
    selection: RowSelection?,
    dimmed: Boolean,
): Modifier {
    val selected = selection?.selected == true
    val strength = if (dimmed) OtusAlpha.DIMMED else 1f
    val onRaised = LocalEntriesOnRaised.current
    val raised =
        lerp(
            if (onRaised) OtusColors.bgDark else OtusColors.bgRegular,
            if (onRaised) OtusColors.bgRegular else OtusColors.bgDark,
            strength,
        )
    val fill = animateColorAsState(if (selected) OtusColors.accentDark else raised, motionTween()).value
    val top = joinedCorner(selection?.joinedAbove == true, EntryTokens.radius)
    val bottom = joinedCorner(selection?.joinedBelow == true, EntryTokens.radius)
    // Only the lower entry of a joined pair reaches up, so the gap is covered once.
    val reach = animateDpAsState(if (selection?.joinedAbove == true) EntryTokens.gap else 0.dp, motionTween()).value
    val shape = SmoothCornerShape(top, top, bottom, bottom)
    val outline = OtusColors.strokeBgDark.copy(alpha = strength)
    val outlined = !selected && OtusColors.bgDark == OtusColors.bgRegular
    // Drawn behind rather than inside the clip, so the fill can reach past the top edge.
    return drawBehind {
        val up = reach.toPx()
        translate(top = -up) {
            drawOutline(shape.createOutline(Size(size.width, size.height + up), layoutDirection, this), fill)
        }
    }.entryOutline(outlined, outline, shape)
        .clip(shape)
}

/** Whether entries sit on a raised surface, such as a grown control panel, rather than on the page. */
public val LocalEntriesOnRaised: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

private fun Modifier.entryOutline(
    outlined: Boolean,
    color: Color,
    shape: Shape,
): Modifier = if (outlined) strokeBorder(EntryTokens.outline, color, shape) else this
