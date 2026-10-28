package com.otus.designsystem.theme.tokens.people

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public object AvatarTokens {
    /** The default avatar size; initials keep the panel text size at this size. */
    public val size: Dp = 48.dp

    /** The avatar in the sheet that edits one. */
    public val editorSize: Dp = 96.dp

    /** A face featured above a list, such as the people the user writes to most. */
    public val featuredSize: Dp = 64.dp

    /** The width a featured face and its name take, so names line up under their faces. */
    public val featuredWidth: Dp = 80.dp

    /** A symbol fills this share of its avatar's width. */
    public const val SYMBOL_SCALE: Float = 0.5f

    /** How far two of the generated fills step from `bgLight`, toward `bgDark` and toward `text3`. */
    public const val FILL_STEP: Float = 0.4f
}
