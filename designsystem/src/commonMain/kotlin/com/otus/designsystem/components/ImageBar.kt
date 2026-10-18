package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens

/**
 * The bottom bar while an image is selected, where the page's bar sits: the [sizing]'s presets
 * in a segmented track, none chosen while the width is free, and ⋯ with the image's [options].
 * The accent ✓ in the primary's place runs [onDone]. Inside a [BarMorph], the group and ✓ move
 * into the other bar's parts.
 */
@Composable
public fun ImageBar(
    sizing: ImageSizing,
    options: List<OptionsItem>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parts = LocalBarParts.current
    val shape = RoundedCornerShape(ActionBarTokens.radius)
    Row(
        modifier.fillMaxWidth().height(ActionBarTokens.height),
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .then(parts.group)
                .weight(1f)
                .height(ActionBarTokens.height)
                .background(OtusColors.bgDark, shape)
                .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
                .padding(start = ActionBarTokens.groupPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegmentedToggle(
                sizing.presets.map { it.name },
                selected = sizing.presets.indexOfFirst { it.width == sizing.width },
                onSelect = { sizing.onWidthChange(sizing.presets[it].width) },
                Modifier.weight(1f),
                fontSize = ComposeTokens.detailSize,
            )
            if (options.isNotEmpty()) MoreOptions(options)
        }
        Box(parts.primary) { CompactSendButton("Done", onDone, enabled = true, icon = Res.drawable.symbol_check) }
    }
}

@Composable
private fun MoreOptions(options: List<OptionsItem>) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton("Image options", { open = true }, outlined = false) { Symbol(Res.drawable.symbol_more_horiz) }
        if (open) OptionsMenu(options) { open = false }
    }
}
