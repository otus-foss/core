package com.otus.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Theme cards, accent swatches, and color sliders on the theme settings page, and folder color swatches. */
public object ThemeTokens {
    public val cardRadius: Dp = 18.dp
    public val cardPadding: Dp = 6.dp
    public val cardGap: Dp = 8.dp
    public val cardTitleSize: TextUnit = 15.sp
    public val cardTitleLineHeight: TextUnit = 20.sp
    public val cardDetailSize: TextUnit = 12.sp
    public val cardDetailLineHeight: TextUnit = 16.sp
    public val sketchHeight: Dp = 64.dp
    public val sketchRadius: Dp = 12.dp
    public val sketchPadding: Dp = 8.dp
    public val sketchGap: Dp = 6.dp
    public val sketchBarHeight: Dp = 14.dp
    public val sketchBarRadius: Dp = 6.dp
    public val sketchDot: Dp = 12.dp
    public val sketchLineHeight: Dp = 5.dp
    public const val SKETCH_LONG_LINE: Float = 0.5f
    public const val SKETCH_SHORT_LINE: Float = 0.35f

    public val swatchSize: Dp = 30.dp
    public val swatchTargetWidth: Dp = 40.dp
    public val swatchRing: Dp = 2.dp

    /** The ring and the gap between it and the swatch. */
    public val swatchRingSpace: Dp = 4.dp
    public val swatchCheck: Dp = 16.dp
    public val swatchAddIcon: Dp = 18.dp
    public val swatchGap: Dp = 2.dp

    /** Small captions, such as a theme's name over its accent preview. */
    public val roleCaptionSize: TextUnit = 12.sp
    public val roleCaptionLineHeight: TextUnit = 16.sp

    /** The overlapping theme colors on the row that opens them. */
    public val stackSwatch: Dp = 24.dp
    public val stackOverlap: Dp = 8.dp

    /** The live preview above the theme cards, and the small message rows an app sketches in it. */
    public val previewPadding: Dp = 10.dp
    public val previewGap: Dp = 8.dp
    public val previewRowPadding: Dp = 8.dp
    public val previewRowRadius: Dp = 12.dp
    public val previewRowGap: Dp = 10.dp
    public val previewAvatar: Dp = 30.dp
    public val previewDetailSize: TextUnit = 11.sp
    public val previewSubjectSize: TextUnit = 14.sp
    public val previewSubjectLineHeight: TextUnit = 18.sp

    /** The custom color sheet: the picked color beside its hex code, and a preview per theme. */
    public val customColorSize: Dp = 64.dp
    public val customPreviewPadding: Dp = 10.dp
    public val customPreviewRadius: Dp = 16.dp
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
