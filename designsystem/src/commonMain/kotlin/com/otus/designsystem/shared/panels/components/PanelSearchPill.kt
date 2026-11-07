package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.lerp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.resources.symbol_search
import com.otus.designsystem.shared.controls.elements.IconButton
import com.otus.designsystem.shared.controls.elements.IconButtonLook
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.input.elements.SearchField
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A search prompt as a page-colored pill in the control panel's header, such as "Search Inbox".
 * As the panel [collapse]s, the [filter] button grows in at its end once the pills above the list
 * have faded, so filters stay one tap away. With a [search], the pill itself is the field, its prompt
 * the placeholder, so starting a search moves nothing.
 */
@Composable
public fun PanelSearchPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapse: () -> Float = { 0f },
    filter: (@Composable () -> Unit)? = null,
    search: PillSearch? = null,
) {
    // The pill fills the header, so press feedback would tint the whole field.
    val interactions = remember { MutableInteractionSource() }
    val filterShown by remember { derivedStateOf { CollapseSteps.filter(collapse()) > 0f } }
    // The 44 dp pill sits in a 48 dp touch target, so the prompt keeps an accessible size.
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.touchTarget)
            .tappable(search == null, label, interactions, onClick),
        contentAlignment = Alignment.CenterStart,
    ) { PillContent(label, filter, filterShown, collapse, search) }
}

@Composable
private fun PillContent(
    label: String,
    filter: (@Composable () -> Unit)?,
    filterShown: Boolean,
    collapse: () -> Float,
    search: PillSearch?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(PanelTokens.searchPillHeight)
            .background(OtusColors.bgRegular, CircleShape)
            .padding(start = PanelTokens.searchPillStart, end = PanelTokens.searchPillEnd),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.searchIconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(Res.drawable.symbol_search) }
        if (search == null) {
            Text(
                label,
                Modifier.weight(1f),
                style = panelTextStyle(),
                color = OtusColors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            SearchField(search.query, search.onQueryChange, label, search.fieldModifier.weight(1f), search.onSubmit)
            if (search.query.isNotEmpty()) {
                IconButton("Clear search", { search.onQueryChange("") }, look = IconButtonLook.Bare) {
                    CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                        Symbol(Res.drawable.symbol_close)
                    }
                }
            }
        }
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

/**
 * What a [PanelSearchPill] edits while it is the search field: the [query], with [onSubmit] on the
 * keyboard's search key. [fieldModifier] goes on the text field, such as to focus it.
 */
public class PillSearch(
    public val query: String,
    public val onQueryChange: (String) -> Unit,
    public val onSubmit: () -> Unit = {},
    public val fieldModifier: Modifier = Modifier,
)

/** A tap on the pill while it is a prompt; as a field, the text field takes taps itself. */
private fun Modifier.tappable(
    prompt: Boolean,
    label: String,
    interactions: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier =
    if (prompt) {
        semantics { contentDescription = label }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
    } else {
        this
    }
