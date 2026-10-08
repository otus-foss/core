package com.otus.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.otus.designsystem.elements.ActionButton
import com.otus.designsystem.elements.AnimatedText
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_left
import com.otus.designsystem.theme.CalendarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle

/** Joined capsule navigation. The caller owns the displayed month and allowed range. */
@Composable
public fun MonthNavigation(
    label: MonthNavigationLabel,
    onPrevious: () -> Unit,
    onChooseMonth: () -> Unit,
    onNext: () -> Unit,
    previousEnabled: Boolean = true,
    nextEnabled: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = OtusColors.bgDark,
        border = BorderStroke(PanelTokens.borderWidth, OtusColors.strokeBgDark),
    ) {
        // The shared surface supplies the overlapped portions of the Figma arrow segments.
        // Keep hit targets adjacent so the center button never steals an arrow press.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton("Previous month", onPrevious, enabled = previousEnabled, outlined = false) {
                Symbol(Res.drawable.symbol_chevron_left, mirrorInRtl = true)
            }
            ActionButton(
                "${label.month} ${label.year}",
                onChooseMonth,
                Modifier.weight(1f).semantics { text = AnnotatedString("${label.month} ${label.year}") },
                primary = true,
                labelContent = {
                    Row(
                        Modifier.fillMaxWidth().clearAndSetSemantics {},
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
                    ) {
                        AnimatedText(
                            label.month,
                            label.chronologicalOrder,
                            Modifier.weight(CalendarTokens.MONTH_COLUMN_WEIGHT),
                            style = panelTextStyle(),
                            textAlign = TextAlign.Center,
                        )
                        AnimatedText(
                            label.year,
                            label.chronologicalOrder,
                            Modifier.weight(1f),
                            style = panelTextStyle(),
                            textAlign = TextAlign.Center,
                        )
                    }
                },
            )
            IconButton("Next month", onNext, enabled = nextEnabled, outlined = false) {
                Box(Modifier.rotate(CalendarTokens.HALF_TURN)) {
                    Symbol(Res.drawable.symbol_chevron_left, mirrorInRtl = true)
                }
            }
        }
    }
}
