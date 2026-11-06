package com.otus.designsystem.mail.read.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.StripButton
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.panels.elements.darkSurface
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.rows.StripTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A banner at the top of a message body with its [actions] as buttons, such as "Images blocked for
 * privacy" with Show. The same banner changes its text and buttons after the step, such as
 * "Images shown" with Hide and Always. Keep the text and labels short; they never wrap.
 */
@Composable
public fun ImageBanner(
    icon: DrawableResource,
    text: String,
    actions: List<UiAction>,
    modifier: Modifier = Modifier,
) {
    val shape = SmoothCornerShape(ReadTokens.bannerRadius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = ReadTokens.bannerHeight)
            .darkSurface(shape)
            .padding(start = ReadTokens.bannerStart, end = ReadTokens.bannerEnd),
        horizontalArrangement = Arrangement.spacedBy(StripTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text3) { Symbol(icon) }
        Text(
            text,
            Modifier.weight(1f),
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = ReadTokens.bannerTextSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        actions.forEach { StripButton(it.label, icon = null, ReadTokens.bannerButtonHeight, it.onClick) }
    }
}
