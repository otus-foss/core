package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.content.LabelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A count in a circle filled like an [Avatar][com.otus.designsystem.shared.people.elements.Avatar],
 * such as the unread mail beside a pill's label. A longer count, such as "99+", stretches the
 * circle into a pill. The label beside it names what it counts, so it stays decorative.
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
