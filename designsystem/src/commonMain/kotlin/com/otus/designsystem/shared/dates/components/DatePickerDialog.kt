package com.otus.designsystem.shared.dates.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.selection.CalendarTokens
import com.otus.designsystem.theme.typography.panelTextStyle

@Composable
internal fun DatePickerDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalDensity provides density) {
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding(), Alignment.BottomCenter) {
                val pickerHeight =
                    maxOf(
                        CalendarTokens.pickerMinHeight,
                        maxHeight * CalendarTokens.PICKER_HEIGHT_FRACTION,
                    ).coerceAtMost(maxHeight)
                Box(
                    Modifier.matchParentSize().clickable(
                        remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
                )
                PickerSurface(pickerHeight, content)
            }
        }
    }
}

@Composable
private fun PickerSurface(
    height: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        Modifier
            .widthIn(max = SheetTokens.maxWidth)
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(topStart = SheetTokens.radius, topEnd = SheetTokens.radius),
        color = OtusColors.bgDark,
        contentColor = OtusColors.text1,
        border = BorderStroke(PanelTokens.borderWidth, OtusColors.strokeBgDark),
    ) {
        ProvideTextStyle(panelTextStyle()) {
            Column(
                Modifier.padding(SheetTokens.inset),
                verticalArrangement = Arrangement.spacedBy(SheetTokens.gap),
                content = content,
            )
        }
    }
}
