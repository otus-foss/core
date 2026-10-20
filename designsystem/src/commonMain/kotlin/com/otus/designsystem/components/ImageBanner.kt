package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.StripButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/** A button of an [ImageBanner]: its short [label] and what it does. */
public data class BannerAction(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * A banner at the top of a message body with its [actions] as buttons, such as "Images blocked for
 * privacy" with Show. The same banner changes its text and buttons after the step, such as
 * "Images shown" with Hide and Always. Keep the text and labels short; they never wrap.
 */
@Composable
public fun ImageBanner(
    icon: DrawableResource,
    text: String,
    actions: List<BannerAction>,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ReadTokens.bannerRadius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = ReadTokens.bannerHeight)
            .background(OtusColors.bgDark, shape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .padding(start = ReadTokens.bannerStart, end = ReadTokens.bannerEnd),
        horizontalArrangement = Arrangement.spacedBy(ReadTokens.stripGap),
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
