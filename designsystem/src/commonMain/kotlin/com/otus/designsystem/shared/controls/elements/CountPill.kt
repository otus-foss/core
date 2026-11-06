package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.content.LabelTokens
import com.otus.designsystem.theme.tokens.core.Sizes

/**
 * A small outlined capsule with a count, such as the mails in a conversation. It is decoration:
 * the row it sits in says the count in its description.
 */
@Composable
internal fun CountPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clearAndSetSemantics {}
            .height(LabelTokens.countHeight)
            .border(Sizes.borderWidth, OtusColors.bgLight, CircleShape)
            .padding(horizontal = LabelTokens.countPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = OtusColors.text2,
            fontSize = LabelTokens.countTextSize,
            lineHeight = LabelTokens.countTextSize,
            maxLines = 1,
        )
    }
}
