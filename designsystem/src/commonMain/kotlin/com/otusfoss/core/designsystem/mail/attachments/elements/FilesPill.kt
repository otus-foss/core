package com.otusfoss.core.designsystem.mail.attachments.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.otusfoss.core.designsystem.mail.attachments.components.AttachmentKind
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrame
import com.otusfoss.core.designsystem.shared.controls.elements.ChipFrameStyle
import com.otusfoss.core.designsystem.shared.controls.elements.ChipText
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A pill for a message's files, one [AttachmentKind] per file in [files], which runs [onClick]. It
 * shows how many files there are when there is more than one, then the symbol of each kind while
 * up to [MAX_KINDS] kinds fit, else the paperclip alone.
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
        files.symbols().forEach { Symbol(it) }
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
    val gaps = symbols - if (count == null) 1 else 0
    val content = Sizes.iconSize * symbols + (count ?: 0.dp) + Spacing.iconGap * gaps
    return max(Sizes.touchTarget, content + ControlTokens.chipPadding * 2 + Sizes.borderWidth * 2)
}

private fun List<AttachmentKind>.description(): String = if (size == 1) "Show the file" else "Show $size files"

private fun List<AttachmentKind>.countLabel(): String? = size.takeIf { it > 1 }?.toString()

private fun List<AttachmentKind>.symbols() =
    distinct()
        .takeIf { it.size <= MAX_KINDS }
        ?.map { it.icon }
        ?: listOf(AttachmentKind.Other.icon)

/** Kinds of file the pill names by symbol before it shows the paperclip instead. */
private const val MAX_KINDS = 2
