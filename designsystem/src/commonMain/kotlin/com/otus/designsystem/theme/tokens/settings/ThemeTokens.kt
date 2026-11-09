package com.otus.designsystem.theme.tokens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/** Theme cards, accent swatches, and color sliders on the theme settings page, and folder color swatches. */
public object ThemeTokens {
    public val cardRadius: Dp = CornerTokens.button
    public val cardPadding: Dp = 6.dp
    public val cardGap: Dp = 8.dp
    public val cardTitleSize: TextUnit = 15.sp
    public val cardTitleLineHeight: TextUnit = 20.sp
    public val sketchHeight: Dp = 64.dp

    /** A card's colors as rings in two rows, each [ringStep] from the next and the lower row [ringDrop] down. */
    public val ringSize: Dp = 34.dp
    public val ringStep: Dp = 30.dp
    public val ringDrop: Dp = 24.dp

    public val swatchSize: Dp = 30.dp
    public val swatchTargetWidth: Dp = 40.dp
    public val swatchRing: Dp = 2.dp

    /** The ring and the gap between it and the swatch. */
    public val swatchRingSpace: Dp = 4.dp
    public val swatchCheck: Dp = 16.dp
    public val swatchAddIcon: Dp = 18.dp
    public val swatchGap: Dp = 2.dp

    /** The overlapping theme colors on the row that opens them. */
    public val stackSwatch: Dp = 24.dp
    public val stackOverlap: Dp = 8.dp

    /** The phone preview above the theme cards: its pills, message rows, and action bar. */
    public val previewPadding: Dp = 10.dp
    public val previewGap: Dp = 8.dp
    public val previewRowPadding: Dp = 8.dp
    public val previewRowRadius: Dp = SheetTokens.radius - previewPadding
    public val previewRowGap: Dp = 10.dp
    public val previewAvatar: Dp = 30.dp
    public val previewDetailSize: TextUnit = 11.sp
    public val previewSubjectSize: TextUnit = 14.sp
    public val previewSubjectLineHeight: TextUnit = 18.sp
    public val previewIcon: Dp = 16.dp
    public val previewIconGap: Dp = 6.dp
    public val previewBarHeight: Dp = 44.dp
    public val previewBarPadding: Dp = 14.dp

    /** The phone around the preview: a metal frame, then a black bezel, then the screen. */
    public val phoneRadius: Dp = 44.dp
    public val phoneFrameWidth: Dp = 3.dp
    public val phoneBezelWidth: Dp = 7.dp
    public val phoneScreenRadius: Dp = phoneRadius - phoneFrameWidth - phoneBezelWidth
    public val phoneFrame: Color = Color(0xFF4A4A4F)
    public val phoneBezel: Color = Color(0xFF050506)

    /** The color sheet's picked color beside its hex code, and the pill in the phone preview. */
    public val customColorSize: Dp = 64.dp
    public val customPillHeight: Dp = 28.dp
    public val customPillPadding: Dp = 10.dp

    /** The check on a selected swatch; it sits on the accent itself, never on the page. */
    public val onSwatch: Color = Color(0xFFFFFFFF)

    /** The colored dot inside a value button, such as the current accent. */
    public val valueDot: Dp = 18.dp

    /** The folder color circle beside each option in the folder color sheet. */
    public val folderSwatch: Dp = 20.dp

    public val sliderHeight: Dp = 32.dp
    public val sliderThumb: Dp = 28.dp
    public val sliderThumbBorder: Dp = 3.dp
}
