package com.otusfoss.core.designsystem.theme.tokens.onboarding

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Sizes of the first-run steps: the progress segments, the owl, and the large first and last titles. */
public object OnboardingTokens {
    public val segmentHeight: Dp = 4.dp
    public val segmentGap: Dp = 4.dp
    public val progressGap: Dp = 12.dp
    public val owlHeight: Dp = 128.dp
    public val heroInset: Dp = 24.dp
    public val heroGap: Dp = 16.dp
    public val heroTitleSize: TextUnit = 34.sp
    public val heroTitleLineHeight: TextUnit = 40.sp
    public val stepTitleSize: TextUnit = 28.sp
    public val stepTitleLineHeight: TextUnit = 34.sp
    public val cardPadding: Dp = 16.dp

    /**
     * A preset's marks: the circle each icon sits in, its icon, the gap to its words, and the line
     * that strikes out what is off.
     */
    public val markSize: Dp = 52.dp
    public val markIcon: Dp = 26.dp
    public val markGap: Dp = 6.dp
    public val markStrike: Dp = 2.dp
    public const val MARK_STRIKE_INSET: Float = 0.24f
}
