package com.otusfoss.core.designsystem.mail.attachments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_attach_file
import com.otusfoss.core.designsystem.resources.symbol_file_other
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrame
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrameStyle
import com.otusfoss.core.designsystem.shared.controls.elements.ChipText
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.SymbolTone
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A pill for a message's files, one [AttachmentKind] per file in [files], which runs [onClick]. It
 * shows how many files there are when there is more than one, then the symbol of each kind while
 * up to [MAX_KINDS] kinds fit, overlapping like stacked avatars, else the paperclip alone.
 */
@Composable
public fun FilesPill(
    files: List<AttachmentKind>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = files.description() },
        selected = null,
        enabled = true,
        style = ChipFrameStyle(OtusColors.bgDark),
    ) {
        files.countLabel()?.let { ChipText(it) }
        Row(horizontalArrangement = Arrangement.spacedBy(-ControlTokens.chipFileOverlap)) {
            files.symbols().forEach { RingedSymbol(it) }
        }
    }
}

/**
 * [icon] over a bare page in the pill's fill, a ring wider than it, which hides the part of the
 * symbol before it that this one overlaps, as the ring around stacked avatars does.
 */
@Composable
private fun RingedSymbol(icon: DrawableResource) {
    Box(contentAlignment = Alignment.Center) {
        val ring = Sizes.iconSize + ControlTokens.chipFileRing * 2
        CompositionLocalProvider(LocalContentColor provides OtusColors.bgDark, LocalContentAlpha provides 1f) {
            Box(
                Modifier.requiredSize(ring),
            ) { Symbol(Res.drawable.symbol_file_other, size = ring, tone = SymbolTone.Solid) }
        }
        Symbol(icon)
    }
}

/** How wide a [FilesPill] for [files] is, at least its touch target, for rows that share out their width. */
@Composable
public fun filesPillWidth(files: List<AttachmentKind>): Dp {
    val measurer = rememberTextMeasurer()
    val style = panelTextStyle()
    val count =
        files.countLabel()?.let { label ->
            with(LocalDensity.current) {
                measurer
                    .measure(label, style)
                    .size.width
                    .toDp()
            }
        }
    val symbols = files.symbols().size
    val countPart = count?.let { it + Spacing.iconGap } ?: 0.dp
    val content = countPart + Sizes.iconSize * symbols - ControlTokens.chipFileOverlap * (symbols - 1)
    return max(Sizes.touchTarget, content + ControlTokens.chipPadding * 2 + Sizes.borderWidth * 2)
}

private fun List<AttachmentKind>.description(): String = if (size == 1) "Show the file" else "Show $size files"

private fun List<AttachmentKind>.countLabel(): String? = size.takeIf { it > 1 }?.toString()

private fun List<AttachmentKind>.symbols() =
    distinct()
        .takeIf { it.size <= MAX_KINDS }
        ?.map { it.icon }
        ?: listOf(Res.drawable.symbol_attach_file)

/** Kinds of file the pill names by symbol before it shows the paperclip instead. */
private const val MAX_KINDS = 2
