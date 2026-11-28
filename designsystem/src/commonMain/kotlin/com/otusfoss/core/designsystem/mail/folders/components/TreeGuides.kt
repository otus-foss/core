package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes

/**
 * A thin line down the row under each folder icon above it, one per level of [levels], so nested
 * rows read as a tree. The first line sits [firstCenter] in from the start and each next one
 * [indent] further. Rows follow each other without gaps, so the lines join up.
 */
internal fun Modifier.treeGuides(
    levels: Int,
    indent: Dp,
    firstCenter: Dp,
): Modifier =
    drawBehind {
        val width = Sizes.borderWidth.toPx()
        repeat(levels) { level ->
            val center = (firstCenter + indent * level).toPx()
            val x = if (layoutDirection == LayoutDirection.Rtl) size.width - center else center
            drawLine(OtusColors.bgLight, Offset(x, 0f), Offset(x, size.height), width)
        }
    }
