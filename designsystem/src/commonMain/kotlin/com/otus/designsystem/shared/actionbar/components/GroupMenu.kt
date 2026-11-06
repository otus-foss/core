package com.otus.designsystem.shared.actionbar.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.overlays.components.MenuRows
import com.otus.designsystem.shared.panels.elements.darkSurface
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * The menu a bar's group grows into: a panel above the group, joined to it in one
 * [GroupMenuShape]. [progress] runs from 0, the group alone, to 1, the open menu. A drag moves it
 * with the finger and [settle] finishes it; [open] and [close] run the whole way.
 */
@Stable
internal class GroupMenu(
    private val scope: CoroutineScope,
) {
    /** The rows shown, empty while the menu is closed. */
    var actions by mutableStateOf(emptyList<UiAction>())
        private set

    /** Whether the menu is on its way closed, when its rows fade faster than it folds. */
    var closing by mutableStateOf(false)
        private set
    val progress = Animatable(0f)

    /** The group's size, which the shape grows from. */
    var group by mutableStateOf(IntSize.Zero)

    /** The open panel's size, once its rows are measured. */
    var panel by mutableStateOf(IntSize.Zero)
    var gap = 0f
    var fling = 0f
    var density = 1f
    var motionScale = 1f

    fun open(menu: List<UiAction>) {
        show(menu)
        scope.launch { progress.animateTo(1f, paced(1f - progress.value, MotionCurve.Enter)) }
    }

    fun close() {
        closing = true
        scope.launch {
            progress.animateTo(0f, paced(progress.value, MotionCurve.Exit))
            actions = emptyList()
        }
    }

    /** Grows [menu] by a drag of [delta] pixels, negative upward, in step with the finger. */
    fun drag(
        menu: List<UiAction>,
        delta: Float,
    ) {
        if (actions.isEmpty()) show(menu)
        closing = false
        // Until the rows are measured, the menu has no length to follow the finger along.
        val reach = gap + panel.height
        if (reach <= 0f) return
        scope.launch { progress.snapTo((progress.value - delta / reach).coerceIn(0f, 1f)) }
    }

    /**
     * Hands a swipe on the rows to the menu once they cannot scroll it, so a drag down on them
     * closes the menu like a drag on the group.
     */
    val connection =
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || available.y == 0f) return Offset.Zero
                drag(actions, available.y)
                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                settle(available.y)
                return available
            }
        }

    /** Finishes a drag let go at [velocity]: a flick decides, or else whether it passed halfway. */
    fun settle(velocity: Float) {
        val opens = if (abs(velocity) > fling) velocity < 0f else progress.value > HALFWAY
        if (opens) open(actions) else close()
    }

    /** A tween across [share] of the menu's growth, at the pace every menu grows at, whatever its size. */
    private fun paced(
        share: Float,
        curve: MotionCurve,
    ): AnimationSpec<Float> = Motion.paced(((gap + panel.height) * share / density).dp, curve, motionScale)

    private fun show(menu: List<UiAction>) {
        actions = menu
        closing = false
    }
}

@Composable
internal fun rememberGroupMenu(): GroupMenu {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    return remember(scope) { GroupMenu(scope) }.apply {
        gap = with(density) { ActionBarTokens.gap.toPx() }
        fling = with(density) { PanelTokens.sheetCloseFling.toPx() }
        this.density = density.density
        motionScale = LocalMotionScale.current
    }
}

/**
 * A drag up on the element grows the menu of [actions]; nothing happens while there are none.
 * A drag that starts on a button takes over from its tap.
 */
@Composable
internal fun Modifier.pullsUp(
    menu: GroupMenu,
    actions: List<UiAction>,
): Modifier =
    draggable(
        rememberDraggableState { menu.drag(actions, it) },
        Orientation.Vertical,
        enabled = actions.isNotEmpty() || menu.actions.isNotEmpty(),
        onDragStopped = { menu.settle(it) },
    )

/**
 * Draws the open menu over its group in a popup, with the group's [icons] drawn again in their
 * place, so the two read as one surface. The panel widens past the group to fit its rows, and
 * only when that leaves room for the curve between them. A drag on it moves it like a drag on the
 * group, and a tap beside the group or Back closes it.
 */
@Composable
internal fun GroupMenu.Shown(icons: @Composable () -> Unit) {
    if (actions.isEmpty()) return
    val density = LocalDensity.current
    val limit = LocalWindowInfo.current.containerSize.width - with(density) { ActionBarTokens.barInset.roundToPx() } * 2
    val fillet = ActionBarTokens.radius + ActionBarTokens.gap
    val room = with(density) { (ActionBarTokens.radius + fillet).roundToPx() }
    val shown = progress.value
    val groupHeight = group.height.toFloat()
    val cutHeight = groupHeight + gap * shown
    val width = lerp(group.width.toFloat(), maxOf(panel.width, group.width).toFloat(), shown)
    val shape =
        with(density) {
            GroupMenuShape(
                Size(width - group.width, cutHeight),
                groupHeight,
                ActionBarTokens.radius.toPx(),
                fillet.toPx(),
            )
        }
    Popup(AtGroupBottom, onDismissRequest = ::close, properties = PopupProperties(focusable = true)) {
        // The popup keeps its open size, so it never moves while the shape grows inside it.
        with(density) {
            Box(
                Modifier
                    .size(maxOf(panel.width, group.width).toDp(), (gap + panel.height).toDp() + group.height.toDp())
                    .nestedScroll(connection)
                    .pullsUp(this@Shown, actions),
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .size(width.toDp(), (cutHeight + panel.height * shown).toDp())
                        .clip(shape)
                        .darkSurface(shape),
                ) {
                    // Like an expanding group's panel, the rows unroll from the bottom edge, never
                    // over the group, and fade out faster than they fold.
                    Box(
                        Modifier.panelWidth(this@Shown, room, limit).graphicsLayer {
                            translationY = panel.height * (progress.value - 1)
                            val fade = if (closing) (progress.value - FADE_LEAD) / (1 - FADE_LEAD) else progress.value
                            alpha = fade.coerceIn(0f, 1f)
                        },
                    ) {
                        MenuRows(actions, ::close)
                    }
                    Box(Modifier.align(Alignment.BottomStart).size(group.width.toDp(), group.height.toDp())) {
                        icons()
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size((panel.width - group.width).coerceAtLeast(0).toDp(), (groupHeight + gap).toDp())
                        .pointerInput(Unit) { detectTapGestures { close() } },
                )
            }
        }
    }
}

/**
 * Measures the rows as wide as they need, but at least as wide as the group, and wide enough past
 * it to fit the curve and corner between them when they reach past it at all. Records the size as
 * the menu's [GroupMenu.panel] and places the rows at the shape's top.
 */
private fun Modifier.panelWidth(
    menu: GroupMenu,
    room: Int,
    limit: Int,
): Modifier =
    layout { measurable, constraints ->
        val group = menu.group.width
        val natural = measurable.maxIntrinsicWidth(Constraints.Infinity)
        val width =
            when {
                natural <= group -> group
                natural < group + room -> group + room
                else -> natural
            }.coerceAtMost(maxOf(limit, group))
        val placeable = measurable.measure(Constraints.fixedWidth(width))
        menu.panel = IntSize(placeable.width, placeable.height)
        layout(constraints.constrainWidth(placeable.width), constraints.constrainHeight(placeable.height)) {
            placeable.placeRelative(0, 0)
        }
    }

/** Puts the popup's bottom start corner on the group's, so the group it draws covers the real one. */
private object AtGroupBottom : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset =
        IntOffset(
            if (layoutDirection ==
                LayoutDirection.Ltr
            ) {
                anchorBounds.left
            } else {
                anchorBounds.right - popupContentSize.width
            },
            anchorBounds.bottom - popupContentSize.height,
        )
}

private const val HALFWAY = 0.5f

/** Share of the fold left once the rows have faded, as a Fast fade ends two thirds into a Normal fold. */
private val FADE_LEAD = 1 - MotionSpeed.Normal.factor / MotionSpeed.Fast.factor
