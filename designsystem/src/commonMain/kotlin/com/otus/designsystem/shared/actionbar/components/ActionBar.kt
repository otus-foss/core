package com.otus.designsystem.shared.actionbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import com.otus.designsystem.shared.actionbar.elements.BarAddButton
import com.otus.designsystem.shared.actionbar.elements.BarIconButton
import com.otus.designsystem.shared.overlays.components.MenuAction
import com.otus.designsystem.shared.pages.components.PageChrome
import com.otus.designsystem.shared.pages.components.ReportClearance
import com.otus.designsystem.shared.pages.components.pinnedChrome
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlin.math.roundToInt

/**
 * The bottom bar of a page: up to [MAX_SECONDARY] icon-only [secondary] actions in a group, and
 * the [primary] action in a shell beside it. Without a [context] the primary fills the shell with
 * its label; with one, it shrinks to its icon at the shell's end and the context fills the rest.
 * The whole shell then runs the primary. A bar that takes over from another, such as a page's bar
 * opening over the list's, moves its shell and action from where the other drew them in one motion;
 * see [BarEntry]. A [minimized] bar shows only the primary's icon, in a circle at the bar's end;
 * see [MinimizedBar] and [BarMinimized]. Inside a
 * [BarMorph], a full bar's group and shell move into the other bar's parts. With [onAdd], a full
 * bar with room ends its group in a [BarAddButton], such as where settings edit the bar. A
 * secondary action with a [BarAction.menu] opens it above its button.
 */
@Composable
public fun ActionBar(
    primary: BarAction,
    modifier: Modifier = Modifier,
    secondary: List<BarAction> = emptyList(),
    context: BarContext? = null,
    minimized: BarMinimized? = null,
    onAdd: (() -> Unit)? = null,
) {
    require(secondary.size <= MAX_SECONDARY) { "An action bar holds at most $MAX_SECONDARY secondary actions" }
    if (minimized != null) {
        MinimizedBar(primary, context, minimized.hidden, minimized.more, modifier)
        return
    }
    val front = rememberFrontBar(primary, context)
    val entry = rememberBarEntry(front)
    val parts = LocalBarParts.current
    ProvideTextStyle(panelTextStyle()) {
        Layout(
            listOf(
                { SecondaryGroup(secondary, onAdd.takeIf { secondary.size < MAX_SECONDARY }, parts.group) },
                { PrimaryShell(primary, context, front, entry, parts.primary) },
            ),
            modifier.fillMaxWidth(),
        ) { (groups, shells), constraints ->
            val width = constraints.maxWidth
            val gap = ActionBarTokens.gap.roundToPx()
            val group = groups.single()
            val ownGroup =
                if (secondary.isEmpty() && onAdd == null) {
                    0
                } else {
                    group.maxIntrinsicWidth(ActionBarTokens.height.roundToPx()) +
                        gap
                }
            val shellWidth = entry.blend((width - ownGroup).toFloat()) { it.shellWidth.toFloat() }.roundToInt()
            val shell = shells.single().measure(constraints.copy(minWidth = shellWidth, maxWidth = shellWidth))
            // The group ends one gap before the shell, also while the shell moves in.
            val groupWidth = width - shellWidth - gap
            val placedGroup =
                group
                    .takeIf {
                        groupWidth > 0
                    }?.measure(constraints.copy(minWidth = groupWidth, maxWidth = groupWidth))
            val height = maxOf(shell.height, placedGroup?.height ?: 0)
            layout(width, height) {
                placedGroup?.placeRelative(0, (height - placedGroup.height) / 2)
                shell.placeRelative(width - shellWidth, (height - shell.height) / 2)
            }
        }
    }
}

/**
 * How a minimized [ActionBar] differs from the full one. It puts [more] behind a round button at
 * its start, which opens them in a [ContextMenu][com.otus.designsystem.shared.overlays.components.ContextMenu].
 * While [hidden], such as while a search field is
 * about to take the keyboard, it scales out in place.
 */
public class BarMinimized(
    public val hidden: Boolean = false,
    public val more: List<MenuAction> = emptyList(),
)

/** Most secondary actions an [ActionBar] shows. */
public const val MAX_SECONDARY: Int = 3

@Composable
private fun SecondaryGroup(
    actions: List<BarAction>,
    onAdd: (() -> Unit)?,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(ActionBarTokens.radius)
    // The bar sets the group's width; while it moves in, the icons keep their places and clip.
    Box(
        modifier
            .height(ActionBarTokens.height)
            .clip(shape)
            .background(OtusColors.bgDark, shape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape),
    ) {
        Row(
            Modifier
                .align(Alignment.CenterStart)
                .wrapContentWidth(Alignment.Start, unbounded = true)
                .padding(horizontal = ActionBarTokens.groupPadding),
            horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.slotGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach {
                val menu = rememberBarMenu()
                Box {
                    BarIconButton(
                        it.label,
                        it.symbol,
                        menu.click(it),
                        enabled = it.enabled,
                        mirrorInRtl = it.mirrorInRtl,
                    )
                    menu.Menu(it)
                }
            }
            onAdd?.let { BarAddButton("Add action", it) }
        }
    }
}

/**
 * Places [bar] over the bottom of [content] at [actionBarInset]. [content] receives bottom padding
 * that lets its last item scroll above the bar; a null [bar] leaves the page as it is. How high
 * the bar reaches goes to
 * [LocalBottomClearance][com.otus.designsystem.shared.pages.components.LocalBottomClearance], so toasts float above
 * it.
 */
@Composable
public fun ActionBarHost(
    bar: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    var barHeight by remember { mutableIntStateOf(0) }
    Box(modifier) {
        val bottom = if (bar == null) 0 else barHeight
        val density = LocalDensity.current
        content(PaddingValues(bottom = with(density) { bottom.toDp() }))
        bar?.let {
            // Measured from the window's bottom, since padding around the host, such as for the
            // system bars, keeps the bar's own size from saying how high it reaches.
            var reach by remember { mutableIntStateOf(0) }
            ReportClearance(with(density) { reach.toDp() })
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .pinnedChrome(PageChrome.ActionBar)
                    .onSizeChanged { barHeight = it.height }
                    .onGloballyPositioned {
                        // Not boundsInRoot: a bar clipped out of view, such as the list's under an
                        // open message, has empty bounds at the top and would claim the whole height.
                        reach = (it.findRootCoordinates().size.height - it.positionInRoot().y).roundToInt()
                    }.actionBarInset(),
            ) { CompositionLocalProvider(LocalHostedBar provides true) { it() } }
        }
    }
}

/**
 * Keeps a hosted bar, or what floats above it, [ActionBarTokens.barInset] above the navigation bar
 * and keyboard, and as far from the sides as it sits from the bottom of the screen.
 */
@Composable
public fun Modifier.actionBarInset(): Modifier {
    val navigation = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
        .padding(ActionBarTokens.barInset)
        .padding(horizontal = navigation)
}

/**
 * Widens a hosted bar over the keyboard by the side padding [actionBarInset] adds for the
 * navigation bar. There the bar sits [ActionBarTokens.barInset] from the keyboard, so it keeps
 * only that gap at its sides too.
 */
@Composable
internal fun Modifier.overKeyboard(): Modifier {
    val navigation = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // A layout wider than its constraints is centered over them, so it reaches past both sides.
    return layout { measurable, constraints ->
        val extra = navigation.roundToPx() * 2
        val placeable =
            measurable.measure(
                constraints.copy(minWidth = constraints.minWidth + extra, maxWidth = constraints.maxWidth + extra),
            )
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
}
