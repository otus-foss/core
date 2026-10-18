package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.LabelTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.panelTextStyle

/**
 * A count in a circle filled like an [Avatar], such as the unread mail beside a pill's label. A
 * longer count, such as "99+", stretches the circle into a pill. The label beside it names what it
 * counts, so it stays decorative.
 */
@Composable
public fun CountBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .defaultMinSize(LabelTokens.badgeSize, LabelTokens.badgeSize)
            .background(OtusColors.accentDark, CircleShape)
            .padding(horizontal = LabelTokens.badgePadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = panelTextStyle().copy(fontSize = LabelTokens.badgeTextSize, lineHeight = LabelTokens.badgeTextSize),
            color = OtusColors.text1,
            maxLines = 1,
        )
    }
}
