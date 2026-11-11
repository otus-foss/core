package com.otusfoss.core.designsystem.shared.people.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.shared.controls.elements.optionalButton
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.people.AvatarTokens

/**
 * A few [avatars] as large faces with their labels, in one centered row. When the row is too
 * narrow, the faces share its width evenly. A label takes up to [labelLines] lines, such as two
 * for an email address.
 */
@Composable
public fun FeaturedAvatars(
    avatars: List<FeaturedAvatar>,
    modifier: Modifier = Modifier,
    labelLines: Int = 1,
) {
    Row(
        modifier.fillMaxWidth().padding(vertical = PageTokens.rowGap),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap, Alignment.CenterHorizontally),
    ) {
        avatars.forEach { Face(it, labelLines, Modifier.weight(1f, fill = false)) }
    }
}

@Composable
private fun Face(
    face: FeaturedAvatar,
    labelLines: Int,
    modifier: Modifier,
) {
    Column(
        modifier
            .width(AvatarTokens.featuredWidth)
            .optionalButton(face.onClick),
        verticalArrangement = Arrangement.spacedBy(PageTokens.rowGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PersonAvatar(face.avatar, AvatarTokens.featuredSize)
        Text(
            face.label,
            color = OtusColors.text2,
            textAlign = TextAlign.Center,
            maxLines = labelLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
