package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A [Symbol] with a small `accentLight` [count] at its top end, such as the number of active
 * filters on the filter button. A count of zero shows the symbol alone. It fills an [IconButton].
 */
@Composable
public fun BadgedSymbol(
    resource: DrawableResource,
    count: Int,
) {
    Box(Modifier.size(PanelTokens.chipHeight), contentAlignment = Alignment.Center) {
        Symbol(resource)
        if (count > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .sizeIn(minWidth = FilterTokens.badgeSize, minHeight = FilterTokens.badgeSize)
                    .background(OtusColors.accentLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    count.toString(),
                    style = panelTextStyle(),
                    color = OtusColors.bgDark,
                    fontSize = FilterTokens.badgeTextSize,
                    lineHeight = FilterTokens.badgeTextSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
    }
}
