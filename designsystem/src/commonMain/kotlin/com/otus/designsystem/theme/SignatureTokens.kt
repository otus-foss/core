package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The signature editor, from the "Edit signature" boards. */
public object SignatureTokens {
    /** The address pill on the signature page, shorter than compose's to leave room for three buttons. */
    public val addressLabelMaxWidth: Dp = 110.dp

    /** The editor box around the signature. */
    public val editorRadius: Dp = 16.dp

    /** A selected image: its outline, the remove button, and the corner handle that resizes it. */
    public val selectionOutline: Dp = 2.dp
    public val removeSize: Dp = 32.dp
    public val handleSize: Dp = 16.dp

    /** The narrowest and widest an image can be dragged, and how close to a preset it snaps. */
    public val minImageWidth: Dp = 48.dp
    public val maxImageWidth: Dp = 300.dp
    public val snapDistance: Dp = 12.dp

    /** The list of preset sizes under a selected image. */
    public val sizeListWidth: Dp = 220.dp

    /** The two-option toggle at the top of the Copy signature sheet. */
    public val toggleHeight: Dp = 48.dp
    public val toggleInset: Dp = 4.dp

    /** The radio and checkbox marks in a pick list. */
    public val markSize: Dp = 22.dp
    public val markStroke: Dp = 2.dp
    public val markDot: Dp = 10.dp
}
