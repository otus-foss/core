package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.settings.ChangeLogTokens

/**
 * A [SheetFrame] that lists [releases] one after another, newest first as given: each release's
 * title as a heading, then its changes the way a [ChangeLogSheet] shows them. The installed
 * releases share one lighter `bgRegular` panel under an [installedLabel] divider, so what the app
 * already has stands apart.
 */
@Composable
public fun ChangeLogHistorySheet(
    title: String,
    releases: List<ChangeLogRelease>,
    onDismiss: () -> Unit,
    installedLabel: String = "Installed",
) {
    SheetFrame(title, onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = SheetTokens.inset, end = SheetTokens.inset, bottom = SheetTokens.inset),
        ) {
            val (installed, newer) = releases.partition { it.installed }
            newer.forEach { Release(it) }
            if (installed.isNotEmpty()) {
                LabeledDivider(installedLabel)
                Column(
                    Modifier
                        .clip(SmoothCornerShape(SheetTokens.rowRadius))
                        .background(OtusColors.bgRegular)
                        .padding(bottom = ChangeLogTokens.releaseGap),
                ) { installed.forEach { Release(it) } }
            }
        }
    }
}

/** A hairline on each side of [label], such as "Installed". */
@Composable
private fun LabeledDivider(label: String) {
    Row(
        Modifier.padding(vertical = ChangeLogTokens.releaseGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ChangeLogTokens.bulletGap),
    ) {
        DividerLine(Modifier.weight(1f))
        Text(label, color = OtusColors.text2, fontSize = SheetTokens.detailSize)
        DividerLine(Modifier.weight(1f))
    }
}

@Composable
private fun DividerLine(modifier: Modifier) {
    Box(modifier.height(Sizes.borderWidth).background(OtusColors.bgLight))
}

@Composable
private fun Release(release: ChangeLogRelease) {
    Text(
        release.title,
        Modifier
            .padding(start = ChangeLogTokens.lineInset, top = ChangeLogTokens.releaseGap)
            .semantics { heading() },
        fontSize = ChangeLogTokens.releaseTitleSize,
        fontWeight = FontWeight.SemiBold,
    )
    ChangeSections(release.sections)
}
