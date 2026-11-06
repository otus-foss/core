package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.rows.StripTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A secondary action as a symbol and a `text2` label without an outline, such as "Save all" in a
 * strip header, where a pill would compete with the content around it.
 */
@Composable
public fun PlainAction(
    label: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .heightIn(min = Sizes.touchTarget)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = StripTokens.plainPadding),
        horizontalArrangement = Arrangement.spacedBy(StripTokens.glyphGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(icon, size = StripTokens.plainIconSize)
        }
        Text(
            label,
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = StripTokens.plainTextSize,
            maxLines = 1,
        )
    }
}
