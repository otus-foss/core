package com.otus.designsystem.shared.sidebar.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_arrow_upward
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.sidebar.elements.SidebarDetail
import com.otus.designsystem.shared.sidebar.elements.SidebarOwl
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens

/**
 * The [Sidebar]'s footer: the [SidebarOwl] in the sidebar's bottom corner, then the app's release
 * [channel], such as "Nightly", and its [version] at the end, both in the same small type and
 * centered on the owl. When a newer build is out, pass [onUpdate]: a circled arrow then leads the
 * version, and tapping the version calls it. While the update downloads, pass its [updateProgress]
 * from 0 to 1: a ring fills around the arrow, and tapping does nothing.
 */
@Composable
public fun SidebarFooter(
    channel: String,
    version: String,
    modifier: Modifier = Modifier,
    onUpdate: (() -> Unit)? = null,
    updateProgress: Float? = null,
) {
    Row(
        modifier.fillMaxWidth().padding(bottom = SidebarTokens.owlBottomGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SidebarTokens.inset),
    ) {
        SidebarOwl(Modifier.height(SidebarTokens.owlHeight))
        SidebarDetail(channel, Modifier.weight(1f))
        FooterVersion(version, onUpdate.takeIf { updateProgress == null }) {
            if (onUpdate != null || updateProgress != null) UpdateBadge(updateProgress)
        }
    }
}

@Composable
private fun FooterVersion(
    version: String,
    onUpdate: (() -> Unit)?,
    badge: @Composable () -> Unit,
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
        badge()
        SidebarDetail(version)
    }
}

/**
 * The `bgRegular` circle, the mailbox tiles' fill, with a `text2` arrow, the version's color,
 * ringed in `accentLight` by the download's [progress] while there is one.
 */
@Composable
private fun UpdateBadge(progress: Float?) {
    val ring = OtusColors.accentLight
    Box(
        Modifier
            .size(SidebarTokens.updateBadge)
            .then(
                if (progress == null) {
                    Modifier
                } else {
                    Modifier
                        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }
                        .drawWithContent {
                            drawContent()
                            val width = SidebarTokens.updateRing.toPx()
                            drawArc(
                                ring,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                topLeft = Offset(width / 2, width / 2),
                                size = Size(size.width - width, size.height - width),
                                style = Stroke(width, cap = StrokeCap.Round),
                            )
                        }
                },
            ).background(OtusColors.bgRegular, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_arrow_upward, size = SidebarTokens.updateIcon)
        }
    }
}
