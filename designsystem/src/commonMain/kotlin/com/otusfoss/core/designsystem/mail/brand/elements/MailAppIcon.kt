package com.otusfoss.core.designsystem.mail.brand.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.brand_mail_icon_bi
import com.otusfoss.core.designsystem.resources.brand_mail_icon_blue
import com.otusfoss.core.designsystem.resources.brand_mail_icon_chrome
import com.otusfoss.core.designsystem.resources.brand_mail_icon_green
import com.otusfoss.core.designsystem.resources.brand_mail_icon_lesbian
import com.otusfoss.core.designsystem.resources.brand_mail_icon_monochrome
import com.otusfoss.core.designsystem.resources.brand_mail_icon_orange
import com.otusfoss.core.designsystem.resources.brand_mail_icon_otus_blue
import com.otusfoss.core.designsystem.resources.brand_mail_icon_pink
import com.otusfoss.core.designsystem.resources.brand_mail_icon_rainbow
import com.otusfoss.core.designsystem.resources.brand_mail_icon_sky
import com.otusfoss.core.designsystem.resources.brand_mail_icon_trans
import com.otusfoss.core.designsystem.resources.brand_mail_icon_violet
import com.otusfoss.core.designsystem.theme.brand.AppIconStyle
import com.otusfoss.core.designsystem.theme.brand.BrandColors
import com.otusfoss.core.designsystem.theme.brand.BrandTokens
import com.otusfoss.core.designsystem.theme.colors.AccentPreset
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Mail's launcher icon in [style], in a rounded square as most launchers show it.
 * [AppIconStyle.Accent] shows the icon of the preset closest to the current accent, as the
 * launcher does.
 */
@Composable
public fun MailAppIcon(
    style: AppIconStyle,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(BrandTokens.appIconSize)
            .clip(SmoothCornerShape(BrandTokens.appIconRadius))
            .background(style.tile),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painterResource(style.foreground(AccentPreset.closest(OtusColors.accentRegular))),
            contentDescription = null,
            modifier = Modifier.requiredSize(BrandTokens.appIconSize * BrandTokens.ADAPTIVE_ICON_BLEED),
        )
    }
}

private val AppIconStyle.tile: Brush
    get() =
        if (this == AppIconStyle.Chrome) {
            Brush.verticalGradient(listOf(BrandColors.chromeTileTop, BrandColors.chromeTileBottom))
        } else {
            SolidColor(BrandColors.iconBackground)
        }

private fun AppIconStyle.foreground(accent: AccentPreset): DrawableResource =
    when (this) {
        AppIconStyle.Accent -> accent.foreground
        AppIconStyle.Monochrome -> Res.drawable.brand_mail_icon_monochrome
        AppIconStyle.Chrome -> Res.drawable.brand_mail_icon_chrome
        AppIconStyle.Rainbow -> Res.drawable.brand_mail_icon_rainbow
        AppIconStyle.Trans -> Res.drawable.brand_mail_icon_trans
        AppIconStyle.Bi -> Res.drawable.brand_mail_icon_bi
        AppIconStyle.Lesbian -> Res.drawable.brand_mail_icon_lesbian
    }

private val AccentPreset.foreground: DrawableResource
    get() =
        when (this) {
            AccentPreset.OtusBlue -> Res.drawable.brand_mail_icon_otus_blue
            AccentPreset.Sky -> Res.drawable.brand_mail_icon_sky
            AccentPreset.Blue -> Res.drawable.brand_mail_icon_blue
            AccentPreset.Violet -> Res.drawable.brand_mail_icon_violet
            AccentPreset.Pink -> Res.drawable.brand_mail_icon_pink
            AccentPreset.Orange -> Res.drawable.brand_mail_icon_orange
            AccentPreset.Green -> Res.drawable.brand_mail_icon_green
        }
