package com.otus.designsystem.theme.tokens.overlays

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.tokens.layout.PageTokens

/**
 * The bottom action bar: a group of secondary icons beside a shell holding the primary action.
 * The bar height and collapsed action width come from the supplied spec; the other sizes were
 * measured from its U2 image and are app decisions, not verified Figma values.
 */
public object ActionBarTokens {
    /** Height of the group and the minimum height of the shell; large text may grow the shell. */
    public val height: Dp = 62.dp

    /** Width of the primary action while a context fills the rest of the shell. */
    public val collapsedActionWidth: Dp = 60.dp

    /** Distance from the screen edges, matching the context panel at the top of a page. */
    public val barInset: Dp = PageTokens.pageInset
    public val gap: Dp = 8.dp

    /** Half the collapsed action, so the action's end keeps its shape as it collapses to a circle. */
    public val radius: Dp = collapsedActionWidth / 2
    public val groupPadding: Dp = 7.dp
    public val slotGap: Dp = 2.dp
    public val symbolSize: Dp = 24.dp

    /** The dashed circle of an empty slot, inside its 48 dp target, and the plus inside it. */
    public val addSlotSize: Dp = 36.dp
    public val addSymbolSize: Dp = 20.dp

    /** Where the no-context label starts inside the action. */
    public val labelInset: Dp = 20.dp
    public val contextInset: Dp = 12.dp
    public val tokenSize: Dp = 36.dp
    public val tokenGap: Dp = 10.dp
    public val tokenOverlap: Dp = 12.dp

    /** How far two people's avatars overlap, closer than an avatar and a count so the names keep room. */
    public val pairOverlap: Dp = 20.dp
    public val tokenRing: Dp = 2.dp
    public val dateRadius: Dp = CornerTokens.chip
    public val titleSize: TextUnit = 18.sp
    public val titleLineHeight: TextUnit = 22.sp
    public val subtitleSize: TextUnit = 14.sp
    public val dateMonthSize: TextUnit = 9.sp
    public val dateDaySize: TextUnit = 16.sp

    /** Share of the width animation after which the context fades in, and before which the label fades out. */
    public const val CONTEXT_FADE_START: Float = 0.4f
}
