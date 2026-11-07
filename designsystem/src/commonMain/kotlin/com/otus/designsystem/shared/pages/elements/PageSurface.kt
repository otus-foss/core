package com.otus.designsystem.shared.pages.elements

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.colors.OtusColors

/** A page's opaque `bgRegular` backdrop, so a page drawn over another, such as a draft, hides it. */
@Composable
public fun PageSurface(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = OtusColors.bgRegular, content = content)
}
