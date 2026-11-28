package com.otusfoss.core.designsystem.theme.tokens.content

import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens

/** A file shown as a picture, beside its name. */
public object FileKindTokens {
    /** The size of the type written on the page, and its distance from the icon's bottom, as shares of the icon's size. */
    public const val LABEL_SIZE: Float = 0.13f
    public const val LABEL_INSET: Float = 0.14f

    /** The corner of an image shown in place of the kind's page symbol. */
    public val pictureRadius: Dp = CornerTokens.small
}
