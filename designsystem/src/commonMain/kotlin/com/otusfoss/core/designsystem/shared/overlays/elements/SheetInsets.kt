package com.otusfoss.core.designsystem.shared.overlays.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens

/** A sheet's side inset, for a part that does not run edge to edge, such as a caption or a field. */
public fun Modifier.sheetInset(): Modifier = padding(horizontal = SheetTokens.inset)

/** A sheet body's inset at the sides and above the sheet's bottom edge. */
public fun Modifier.sheetBodyInset(): Modifier =
    padding(start = SheetTokens.inset, end = SheetTokens.inset, bottom = SheetTokens.inset)
