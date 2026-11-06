package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.lerp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.resources.symbol_search
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.input.elements.SearchField
import com.otus.designsystem.shared.panels.elements.PanelTitle
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import kotlin.math.roundToInt

/**
 * A page [title] with a search button at the end of the control panel's header. While [open], the
 * button grows leftward into a field that ends just after the chevron: its fill fades in first, the
 * title fades and slides away, the icon stays at the field's start and turns grey, then the caret
 * and placeholder appear, and the clear ✕ last. Closing plays the same frames backward. The motion
 * reads [progress] from [rememberSearchExpansion], which the caller also uses to dim the page.
 */
@Composable
internal fun ExpandingSearch(
    title: String,
    search: PageSearch,
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    progress: () -> Float,
) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(open) {
        if (open) {
            // The field joins the layout on the next frame.
            withFrameNanos {}
            focus.requestFocus()
            keyboard?.show()
        } else {
            focusManager.clearFocus()
        }
    }
    Layout(
        content = {
            SearchTitle(title, progress)
            Box(
                Modifier
                    .layoutId(SearchPart.Fill)
                    .graphicsLayer { alpha = SearchSteps.fill(progress()) }
                    .background(OtusColors.bgRegular, CircleShape),
            )
            SearchInput(search, open, focus, progress)
            SearchIcon(search.placeholder, open, { onOpenChange(true) }, progress)
            SearchClear(open, progress) {
                search.onQueryChange("")
                onOpenChange(false)
            }
        },
        modifier = Modifier.height(Sizes.touchTarget),
        measurePolicy = SearchMeasurePolicy(progress),
    )
}

@Composable
private fun SearchTitle(
    title: String,
    progress: () -> Float,
) {
    PanelTitle(
        title,
        Modifier.layoutId(SearchPart.Title).graphicsLayer {
            val p = progress()
            alpha = 1f - SearchSteps.title(p)
            translationX = -PanelTokens.titleSlide.toPx() * SearchSteps.slide(p)
        },
    )
}

/** The text field, composed only while it shows, so screen readers and taps find the button while search is closed. */
@Composable
private fun SearchInput(
    search: PageSearch,
    open: Boolean,
    focus: FocusRequester,
    progress: () -> Float,
) {
    val shown by remember(open) { derivedStateOf { open || progress() > 0f } }
    Box(Modifier.layoutId(SearchPart.Input)) {
        if (shown) {
            SearchField(
                search.query,
                search.onQueryChange,
                search.placeholder,
                Modifier.focusRequester(focus).graphicsLayer { alpha = SearchSteps.input(progress()) },
            )
        }
    }
}

/** The search button, which stays at the field's start and turns grey once it has grown. */
@Composable
private fun SearchIcon(
    description: String,
    open: Boolean,
    onOpen: () -> Unit,
    progress: () -> Float,
) {
    IconButton(
        description,
        onOpen,
        Modifier.layoutId(SearchPart.Icon).then(if (open) Modifier.clearAndSetSemantics {} else Modifier),
        enabled = !open,
        look = IconButtonLook.Bare,
    ) {
        val color = if (progress() > SearchSteps.GREY_AT) OtusColors.text2 else OtusColors.text1
        CompositionLocalProvider(LocalContentColor provides color) { Symbol(Res.drawable.symbol_search) }
    }
}

@Composable
private fun SearchClear(
    open: Boolean,
    progress: () -> Float,
    onClose: () -> Unit,
) {
    Box(Modifier.layoutId(SearchPart.Clear).graphicsLayer { alpha = SearchSteps.clear(progress()) }) {
        if (open) {
            IconButton("Close search", onClose, look = IconButtonLook.Bare) {
                CompositionLocalProvider(
                    LocalContentColor provides OtusColors.text2,
                ) { Symbol(Res.drawable.symbol_close) }
            }
        }
    }
}

/** Grows the field from the button's place at the end to just after the chevron. */
private class SearchMeasurePolicy(
    private val progress: () -> Float,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val grown = SearchSteps.grow(progress())
        val width = constraints.maxWidth
        val target = Sizes.touchTarget.roundToPx()
        val fieldWidth = lerp(target.toFloat(), (width - PanelTokens.searchFieldStart.roundToPx()).toFloat(), grown)
        val fieldLeft = width - fieldWidth.roundToInt()
        val iconLeft = fieldLeft + (PanelTokens.searchIconShift.toPx() * grown).roundToInt()
        val inputLeft = iconLeft + target - PanelTokens.searchInputPull.roundToPx()

        fun part(id: SearchPart) = measurables.first { it.layoutId == id }

        fun exact(partWidth: Int) = Constraints.fixed(partWidth.coerceAtLeast(0), target)
        val title = part(SearchPart.Title).measure(Constraints(maxWidth = (width - target).coerceAtLeast(0)))
        val fill = part(SearchPart.Fill).measure(exact(width - fieldLeft))
        val icon = part(SearchPart.Icon).measure(exact(target))
        val clear = part(SearchPart.Clear).measure(Constraints(maxHeight = target))
        val input = part(SearchPart.Input).measure(exact(width - inputLeft - target))
        return layout(width, target) {
            title.place(PanelTokens.titleStart.roundToPx(), (target - title.height) / 2)
            fill.place(fieldLeft, 0)
            input.place(inputLeft, 0)
            icon.place(iconLeft, 0)
            clear.place(width - clear.width, 0)
        }
    }
}

private enum class SearchPart { Title, Fill, Input, Icon, Clear }

/** Each part of the search expansion runs over its own slice of the motion, as in the design. */
private object SearchSteps {
    const val GREY_AT = 0.5f
    private const val FILL_END = 0.4f
    private const val TITLE_END = 0.35f
    private const val SLIDE_END = 0.5f
    private const val INPUT_START = 0.55f
    private const val CLEAR_START = 0.7f

    fun grow(p: Float) = MotionCurve.Enter.easing.transform(p)

    fun fill(p: Float) = slice(p, 0f, FILL_END)

    fun title(p: Float) = slice(p, 0f, TITLE_END)

    fun slide(p: Float) = slice(p, 0f, SLIDE_END)

    fun input(p: Float) = slice(p, INPUT_START, 1f)

    fun clear(p: Float) = slice(p, CLEAR_START, 1f)

    private fun slice(
        p: Float,
        start: Float,
        end: Float,
    ) = ((p - start) / (end - start)).coerceIn(0f, 1f)
}
