package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.elements.SidebarDetail
import com.otus.designsystem.elements.SidebarOwl
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_arrow_upward
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens

/**
 * The [Sidebar]'s footer: the [SidebarOwl] in the sidebar's bottom corner, then the app's release
 * [channel], such as "Nightly", and its [version] at the end, both in the same small type and
 * centered on the owl. When a newer build is out, pass [onUpdate]: an arrow then leads the version, and
 * tapping the version calls it.
 */
@Composable
public fun SidebarFooter(
    channel: String,
    version: String,
    modifier: Modifier = Modifier,
    onUpdate: (() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().padding(bottom = SidebarTokens.owlBottomGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SidebarTokens.inset),
    ) {
        SidebarOwl(Modifier.height(SidebarTokens.owlHeight))
        SidebarDetail(channel, Modifier.weight(1f))
        FooterVersion(version, onUpdate)
    }
}

@Composable
private fun FooterVersion(
    version: String,
    onUpdate: (() -> Unit)?,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SidebarTokens.entryRadius)
    Row(
        Modifier
            .heightIn(min = PanelTokens.touchTarget)
            .then(
                if (onUpdate == null) {
                    Modifier
                } else {
                    Modifier
                        .semantics { contentDescription = "Update available, $version installed" }
                        .clickable(interactions, indication = null, onClickLabel = "Get the update", onClick = onUpdate)
                        .clip(shape)
                        .controlPressFeedback(interactions, shape)
                },
            ).padding(horizontal = SidebarTokens.entryPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SidebarTokens.inset),
    ) {
        if (onUpdate != null) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.accentLight) {
                Symbol(Res.drawable.symbol_arrow_upward, size = SidebarTokens.updateIcon)
            }
        }
        SidebarDetail(version)
    }
}
