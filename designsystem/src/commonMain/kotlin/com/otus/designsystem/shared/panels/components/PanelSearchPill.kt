package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_search
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import kotlin.math.roundToInt

/**
 * A search prompt as a page-colored pill in the control panel's header, such as "Search Inbox".
 * It tightens from [PanelTokens.searchPillHeight] to [PanelTokens.compactSearchPillHeight] as the
 * panel [collapse]s, and the [filter] button grows in at its end once the pills above the list have
 * faded, so filters stay one tap away.
 */
@Composable
public fun PanelSearchPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapse: () -> Float = { 0f },
    filter: (@Composable () -> Unit)? = null,
) {
    // The pill fills the header, so press feedback would tint the whole field.
    val interactions = remember { MutableInteractionSource() }
    val filterShown by remember { derivedStateOf { CollapseSteps.filter(collapse()) > 0f } }
    Row(
        modifier
            .fillMaxWidth()
            .heightBetween(PanelTokens.searchPillHeight, PanelTokens.compactSearchPillHeight) {
                CollapseSteps.search(collapse())
            }.background(OtusColors.bgRegular, CircleShape)
            .semantics { contentDescription = label }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .padding(start = PanelTokens.searchPillStart, end = PanelTokens.searchPillEnd),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.searchIconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(Res.drawable.symbol_search) }
        Text(
            label,
            Modifier.weight(1f),
            style = panelTextStyle(),
            color = OtusColors.text2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (filter != null && filterShown) {
            Box(
                Modifier.graphicsLayer {
                    val grown = CollapseSteps.filter(collapse())
                    alpha = grown
                    scaleX = lerp(PanelTokens.FILTER_START_SCALE, 1f, grown)
                    scaleY = scaleX
                },
            ) { filter() }
        }
    }
}

/** Lays the content out [from] tall at progress 0 and [to] at 1, centered within its constraints. */
private fun Modifier.heightBetween(
    from: Dp,
    to: Dp,
    progress: () -> Float,
): Modifier =
    layout { measurable, constraints ->
        val height = lerp(from.toPx(), to.toPx(), progress()).roundToInt()
        val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
        layout(placeable.width, height) { placeable.place(0, 0) }
    }
