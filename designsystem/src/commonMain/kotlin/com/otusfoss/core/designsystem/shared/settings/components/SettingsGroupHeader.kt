package com.otusfoss.core.designsystem.shared.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_chevron_right
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.shared.rows.components.topHairline
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * The heading above a group of settings search results: the [path] of the page they live on, with
 * its [icon], which [onOpen] opens. A [divided] heading has a hairline above it, separating its
 * group from the one before.
 */
@Composable
public fun SettingsGroupHeader(
    path: String,
    icon: DrawableResource?,
    onOpen: () -> Unit,
    divided: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    CompositionLocalProvider(LocalContentColor provides OtusColors.text3) {
        Row(
            modifier
                .fillMaxWidth()
                .topHairline(OtusColors.bgLight, PageTokens.textInset) { divided }
                .padding(top = SettingTokens.groupGap)
                .heightIn(min = Sizes.touchTarget)
                .clickable(interactions, indication = null, role = Role.Button, onClick = onOpen)
                .controlPressFeedback(interactions, RoundedCornerShape(SheetTokens.rowRadius))
                .padding(horizontal = PageTokens.textInset)
                .semantics { heading() },
            horizontalArrangement = Arrangement.spacedBy(SettingTokens.groupIconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let { Symbol(it, size = SettingTokens.groupIconSize) }
            Text(
                path,
                Modifier.weight(1f),
                color = OtusColors.text3,
                fontSize = SettingTokens.groupLabelSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true, size = SettingTokens.groupIconSize)
        }
    }
}
