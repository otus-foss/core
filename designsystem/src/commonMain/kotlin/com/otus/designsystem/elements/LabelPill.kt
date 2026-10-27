package com.otus.designsystem.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.LabelTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A read-only pill that names something an item belongs to, such as a folder. [highlighted] gives
 * the one label that matters most `text1` and a `text3` outline; others stay quiet.
 */
@Composable
public fun LabelPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    highlighted: Boolean = false,
) {
    val color = if (highlighted) OtusColors.text1 else OtusColors.text2
    Row(
        modifier
            .height(LabelTokens.height)
            .border(
                PanelTokens.borderWidth,
                if (highlighted) OtusColors.text3 else OtusColors.bgLight,
                CircleShape,
            ).padding(horizontal = LabelTokens.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(LabelTokens.iconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            icon?.let { Symbol(it, size = LabelTokens.iconSize) }
            Text(text, color = color, fontSize = LabelTokens.textSize, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
