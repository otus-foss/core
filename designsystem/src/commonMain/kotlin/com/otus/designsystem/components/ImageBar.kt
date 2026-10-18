package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.ComposeTokens

/**
 * The bottom bar while an image is selected, where the page's bar sits: the [sizing]'s presets
 * in a segmented track, none chosen while the width is free, and ⋯, which grows the group into
 * the image's [options]. The accent ✓ in the primary's place runs [onDone]. Inside a
 * [BarMorph], the group and ✓ move into the other bar's parts.
 */
@Composable
public fun ImageBar(
    sizing: ImageSizing,
    options: List<OptionsItem>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parts = LocalBarParts.current
    Row(
        modifier.fillMaxWidth().height(ActionBarTokens.height),
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExpandingBarGroup(
            "Image options",
            panel = { close -> OptionsList(options, close) },
            Modifier.then(parts.group).weight(1f),
        ) {
            SegmentedToggle(
                sizing.presets.map { it.name },
                selected = sizing.presets.indexOfFirst { it.width == sizing.width },
                onSelect = { sizing.onWidthChange(sizing.presets[it].width) },
                Modifier.weight(1f),
                fontSize = ComposeTokens.detailSize,
            )
        }
        Box(parts.primary) { CompactSendButton("Done", onDone, enabled = true, icon = Res.drawable.symbol_check) }
    }
}
