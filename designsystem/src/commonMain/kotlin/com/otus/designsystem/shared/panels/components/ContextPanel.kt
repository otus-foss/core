package com.otus.designsystem.shared.panels.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.lerp
import com.otus.designsystem.shared.pages.components.PageChrome
import com.otus.designsystem.shared.pages.components.pinnedChrome
import com.otus.designsystem.shared.rows.components.Collapsible
import com.otus.designsystem.shared.sidebar.components.sidebarOpenZone
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlin.math.roundToInt

/**
 * Where the chip rail sits: [Overlapping] across the panel's bottom edge, [Below] under it, or
 * [None], without a rail, where the trailing action moves into the header row.
 */
public enum class ChipPlacement { Below, Overlapping, None }

/**
 * The control panel at the top of a page. It runs edge to edge, draws behind the status bar, and
 * rounds only its bottom corners. Its one header row holds the [navigation] button and the
 * [content], which puts anything at the row's end, such as an [AccountStack], in its own row;
 * while there is no rail, the [trailingAction] shows there too. Each
 * [chips] item needs a stable key and a pill-shaped visual, and the panel cuts it out of its
 * surface.
 *
 * The [motion]'s collapse reads a [PanelCollapse.progress]: as a list scrolls down, the panel
 * shortens and pills across its edge rise, shrink, and fade out. Pass the same reading to header
 * content that collapses with it, such as a [PanelSearchPill] or an [AccountStack]. With a
 * [PanelExpansion], dragging the panel down grows it under its header row, which stays where it
 * is, and shows the motion's expanded content in the new room; the pills fade as it grows.
 */
@Composable
public fun ContextPanel(
    modifier: Modifier = Modifier,
    placement: ChipPlacement = ChipPlacement.Below,
    navigation: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    chips: @Composable ContextPanelScope.() -> Unit = {},
    motion: PanelMotion = PanelMotion(),
    content: @Composable () -> Unit,
) {
    val overlap =
        animateFloatAsState(
            targetValue = if (placement == ChipPlacement.Overlapping) 1f else 0f,
            animationSpec = motionTween(),
            label = "Panel surface expansion",
        )
    val geometry = remember { PanelLayoutState() }
    val scope = remember(geometry) { ContextPanelScope(geometry) }
    val railShown = placement != ChipPlacement.None
    Layout(
        contents =
            listOf(
                { PanelHeader(navigation, HeaderAction(trailingAction, shown = !railShown), null, content) },
                { PanelRail(railShown, geometry, scope, trailingAction, chips) },
                { if (motion.expansion != null) ExpansionRoom(motion) },
            ),
        modifier =
            modifier
                .pinnedChrome(PageChrome.ContextPanel)
                .fillMaxWidth()
                .sidebarOpenZone()
                .onGloballyPositioned(geometry::trackPanel)
                .expandable(motion.expansion)
                .panelSurface(geometry),
        measurePolicy = PanelMeasurePolicy(placement, overlap::value, motion, geometry),
    )
}

/** The chip rail with the trailing action at its end, which collapses away while not [shown]. */
@Composable
private fun PanelRail(
    shown: Boolean,
    geometry: PanelLayoutState,
    scope: ContextPanelScope,
    trailingAction: (@Composable () -> Unit)?,
    chips: @Composable ContextPanelScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    Collapsible(shown) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PanelChipRail(Modifier.weight(1f), geometry, scroll) { scope.chips() }
            if (trailingAction != null) {
                Box(Modifier.padding(end = PanelTokens.panelInset)) { scope.item(ActionKey, trailingAction) }
            }
        }
    }
}

/**
 * Places the header and the rail, and puts the panel's edge where the [placement], its [overlap]
 * animation, and the [collapse] say. The rail keeps one anchor in every placement, so only the edge
 * moves as pills go from below the panel to across it; it rises as it fades on collapse.
 */
private class PanelMeasurePolicy(
    private val placement: ChipPlacement,
    private val overlap: () -> Float,
    private val motion: PanelMotion,
    private val geometry: PanelLayoutState,
) : MultiContentMeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<List<Measurable>>,
        constraints: Constraints,
    ): MeasureResult {
        val loose = constraints.copy(minHeight = 0)
        val header = measurables[0].first().measure(loose)
        val rail = measurables[1].firstOrNull()?.measure(loose)
        val grown = motion.expansion?.height ?: 0f
        val room =
            measurables[2]
                .firstOrNull()
                ?.takeIf {
                    grown >= 1f
                }?.measure(Constraints.fixed(constraints.maxWidth, grown.roundToInt()))
        val progress = motion.collapse().coerceIn(0f, 1f)
        val growFade = (grown / QuickViewTokens.pillsFadeDistance.toPx()).coerceIn(0f, 1f)
        val fade = maxOf(if (placement == ChipPlacement.Overlapping) CollapseSteps.pills(progress) else 0f, growFade)
        val drop = lerp(PanelTokens.railDrop.toPx(), PanelTokens.collapsedDrop.toPx(), progress)
        val railHeight = rail?.height ?: 0
        val closed = PanelTokens.collapsedDrop.toPx()
        // Below, the edge runs along the rail's top; across, through its middle.
        val below = if (placement != ChipPlacement.None) maxOf(closed, drop - railHeight / 2f) else closed
        // The room's grab handle ends the grown panel, so the rail's share of the edge goes with the pills.
        val edge = header.height + grown + lerp(below, drop, overlap()) * (1f - growFade)
        val railTop = header.height + grown + drop - railHeight / 2f - PanelTokens.pillRise.toPx() * fade
        geometry.edge = edge
        geometry.railFade = fade
        val railPlaced = rail != null && fade < 1f
        val height = if (railPlaced) maxOf(edge, railTop + railHeight) else edge
        return layout(constraints.maxWidth, height.roundToInt()) {
            header.place(0, 0)
            room?.place(0, header.height)
            // A faded rail is not placed, so its pills take no taps.
            if (railPlaced) {
                rail.placeWithLayer(0, railTop.roundToInt()) {
                    alpha = 1f - fade
                    scaleX = 1f - PanelTokens.PILL_SHRINK * fade
                    scaleY = scaleX
                }
            }
        }
    }
}

internal object ActionKey

/** The trailing action, which shows in the header row while the panel has no chip rail. */
internal class HeaderAction(
    val content: (@Composable () -> Unit)?,
    val shown: Boolean,
)

/**
 * One row below the status bar. Content wraps to more lines downward, and the [navigation] button
 * stays on the first line. The button is laid over the content's start, so its touch target may
 * overlap what follows it. The [action] fades in and out as the chip rail goes and comes.
 */
@Composable
internal fun PanelHeader(
    navigation: (@Composable () -> Unit)?,
    action: HeaderAction,
    actions: (@Composable RowScope.() -> Unit)?,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = PanelTokens.headerRowHeight)
                .padding(
                    start = if (navigation == null) PanelTokens.headerInset else PanelTokens.navigationContentStart,
                    end =
                        if (actions == null &&
                            action.content == null
                        ) {
                            PanelTokens.headerInset
                        } else {
                            PanelTokens.panelInset
                        },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = PanelTokens.touchTarget),
                contentAlignment = Alignment.CenterStart,
            ) { ProvideTextStyle(panelTextStyle()) { content() } }
            if (actions != null) Row(verticalAlignment = Alignment.CenterVertically, content = actions)
            if (action.content != null) {
                AnimatedVisibility(action.shown, enter = fadeIn(motionTween()), exit = fadeOut(motionTween())) {
                    action.content.invoke()
                }
            }
        }
        if (navigation != null) {
            Box(Modifier.padding(start = PanelTokens.navigationStart, top = PanelTokens.headerContentPadding)) {
                navigation()
            }
        }
    }
}

/**
 * The room a [PanelExpansion] grows below the header row: the expanded content, cut to the room,
 * with the grab handle at its bottom.
 */
@Composable
private fun ExpansionRoom(motion: PanelMotion) {
    Column(Modifier.clipToBounds()) {
        Box(Modifier.weight(1f).fillMaxWidth()) { motion.expanded() }
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = PanelTokens.handlePadding)
                .size(PanelTokens.handleWidth, PanelTokens.handleHeight)
                .clip(CircleShape)
                .background(OtusColors.bgLight),
        )
    }
}

/**
 * Lets the user drag the panel taller and shorter with [expansion]; scrolling content inside hands
 * it what it does not scroll itself.
 */
@Composable
private fun Modifier.expandable(expansion: PanelExpansion?): Modifier {
    if (expansion == null) return this
    return nestedScroll(expansion.connection).draggable(
        rememberDraggableState(expansion::drag),
        Orientation.Vertical,
        onDragStopped = { velocity -> expansion.release(velocity) },
    )
}
