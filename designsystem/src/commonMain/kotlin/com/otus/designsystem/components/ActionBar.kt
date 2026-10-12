package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.otus.designsystem.elements.BarIconButton
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The bottom bar of a page: up to [MAX_SECONDARY] icon-only [secondary] actions in a group, and
 * the [primary] action in a shell beside it. Without a [context] the primary fills the shell with
 * its label; with one, it shrinks to its icon at the shell's end and the context fills the rest.
 * The whole shell then runs the primary. A [minimized] bar shows only the primary's icon, in a
 * circle at the bar's end.
 */
@Composable
public fun ActionBar(
    primary: BarAction,
    modifier: Modifier = Modifier,
    secondary: List<BarAction> = emptyList(),
    context: BarContext? = null,
    minimized: Boolean = false,
) {
    require(secondary.size <= MAX_SECONDARY) { "An action bar holds at most $MAX_SECONDARY secondary actions" }
    if (minimized) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { RoundPrimary(primary, context) }
        return
    }
    ProvideTextStyle(panelTextStyle()) {
        Row(
            modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (secondary.isNotEmpty()) SecondaryGroup(secondary)
            PrimaryShell(primary, context, Modifier.weight(1f))
        }
    }
}

/** Most secondary actions an [ActionBar] shows. */
public const val MAX_SECONDARY: Int = 3

@Composable
private fun SecondaryGroup(actions: List<BarAction>) {
    val shape = RoundedCornerShape(ActionBarTokens.radius)
    Row(
        Modifier
            .height(ActionBarTokens.height)
            .background(OtusColors.bgDark, shape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .padding(horizontal = ActionBarTokens.groupPadding),
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.slotGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach {
            BarIconButton(
                it.label,
                it.symbol,
                it.onClick,
                enabled = it.enabled,
                mirrorInRtl = it.mirrorInRtl,
            )
        }
    }
}

/**
 * Places [bar] over the bottom of [content], [ActionBarTokens.barInset] from the edges and above
 * the navigation bar and keyboard. [content] receives bottom padding that lets its last item
 * scroll above the bar; a null [bar] leaves the page as it is.
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
        content(PaddingValues(bottom = with(LocalDensity.current) { bottom.toDp() }))
        bar?.let {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { barHeight = it.height }
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(ActionBarTokens.barInset),
            ) { it() }
        }
    }
}
