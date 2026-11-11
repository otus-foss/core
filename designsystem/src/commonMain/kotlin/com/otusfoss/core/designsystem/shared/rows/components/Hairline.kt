package com.otusfoss.core.designsystem.shared.rows.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens

/**
 * A hairline along the top of this element, [inset] from both sides, separating it from the one
 * above. It is drawn only while [shown] is true.
 */
internal fun Modifier.topHairline(
    color: Color,
    inset: Dp = 0.dp,
    shown: () -> Boolean = { true },
): Modifier =
    drawBehind {
        if (shown()) {
            drawLine(
                color,
                Offset(inset.toPx(), 0f),
                Offset(size.width - inset.toPx(), 0f),
                SettingTokens.dividerWidth.toPx(),
            )
        }
    }
