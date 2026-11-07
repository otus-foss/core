package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.theme.colors.OtusColors

/**
 * A pill that acts, such as "+ Cc", with an optional [leading] icon, filled with [fill]. Screen
 * readers read [description].
 */
@Composable
public fun ButtonPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String = label,
    fill: Color = OtusColors.bgDark,
    leading: (@Composable () -> Unit)? = null,
) {
    ChipFrame(
        onClick,
        modifier.semantics { contentDescription = description },
        selected = null,
        enabled = true,
        style = ChipFrameStyle(fill),
    ) {
        leading?.invoke()
        ChipText(label)
    }
}
