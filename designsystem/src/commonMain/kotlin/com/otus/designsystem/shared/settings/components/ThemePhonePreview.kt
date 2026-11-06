package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_calendar_add_on
import com.otus.designsystem.resources.symbol_edit
import com.otus.designsystem.resources.symbol_filter_list
import com.otus.designsystem.resources.symbol_mark_email_read
import com.otus.designsystem.resources.symbol_person
import com.otus.designsystem.resources.symbol_search
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.pages.components.pageSideInset
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.shapes.strokeBorder
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.settings.ThemeTokens
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The lower half of a phone showing an inbox drawn in [palette], which need not be the app's
 * theme, so colors can be tried before they apply. It runs edge to edge as a page's backdrop
 * under the control panel, with a phone's metal frame and black bezel along its sides and rounded
 * bottom, so it reads as a device. A tap or a drag down grows it with [expansion] to most of the
 * screen, showing more messages, while [ThemePhonePanel] takes over the page's panel; a tap, a
 * drag up, or back shrinks it again.
 */
@Composable
public fun ThemePhonePreview(
    palette: OtusPalette,
    expansion: PhoneExpansion,
    modifier: Modifier = Modifier,
) {
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = expansion.expanded,
        onBackCompleted = expansion::collapse,
    )
    val windowHeight = LocalWindowInfo.current.containerSize.height
    PhoneColors(palette) {
        Column(
            modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    contentDescription = "Preview of the app in this theme"
                    onClick(if (expansion.expanded) "Shrink preview" else "Expand preview") {
                        expansion.toggle()
                        true
                    }
                }.onGloballyPositioned { phone ->
                    // Measured while shrunk, so the reach stays put as the phone grows.
                    if (expansion.progress.value == 0f) {
                        val bottom = phone.positionInWindow().y + phone.size.height
                        expansion.reach = (windowHeight * EXPANDED_SHARE - bottom).coerceAtLeast(0f)
                    }
                }.dragsPhone(expansion)
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = expansion::toggle)
                .edge(bottomRounded(ThemeTokens.phoneRadius), OtusColors.strokeBgDark, sides = true)
                .phoneBottom(palette)
                .padding(top = PanelTokens.bottomRadius)
                .pageSideInset()
                .padding(ThemeTokens.previewPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeTokens.previewGap),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap)) {
                SketchPill("People", Res.drawable.symbol_person, palette, selected = true)
                SketchPill("Date", Res.drawable.symbol_calendar_add_on, palette, selected = false)
            }
            Column(Modifier.growingRows(expansion)) {
                sampleMessages.forEachIndexed { index, it -> SketchMessage(it, palette, selected = index == 0) }
            }
            SketchBar(palette)
        }
    }
}

@Composable
private fun SketchPill(
    label: String,
    icon: DrawableResource,
    palette: OtusPalette,
    selected: Boolean,
) {
    Row(
        Modifier
            .height(ThemeTokens.customPillHeight)
            .background(if (selected) palette.selectedFill else palette.bgRegular, CircleShape)
            .strokeBorder(PanelTokens.borderWidth, palette.strokeBgDark, CircleShape)
            .padding(horizontal = ThemeTokens.customPillPadding),
        horizontalArrangement = Arrangement.spacedBy(ThemeTokens.previewIconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Symbol(icon, size = ThemeTokens.previewIcon)
        Text(label, fontSize = ThemeTokens.previewSubjectSize)
    }
}

private class SampleMessage(
    val initials: String,
    val from: String,
    val time: String,
    val subject: String,
)

private val sampleMessages =
    listOf(
        SampleMessage("GH", "Grace Hopper", "10:42", "Q4 launch plan"),
        SampleMessage("AB", "Acme Billing", "09:15", "Invoice 1042 for September"),
        SampleMessage("MK", "Mara König", "Sat", "Notes from Thursday"),
        SampleMessage("LT", "Linus Torvalds", "Fri", "Re: patch review"),
        SampleMessage("TR", "Travel desk", "Fri", "Your trip to Lisbon"),
        SampleMessage("AL", "Ada Lovelace", "Thu", "Notes on the engine"),
        SampleMessage("NS", "Newsletter", "Wed", "This week in design"),
        SampleMessage("KJ", "Katherine Johnson", "Tue", "Orbit numbers"),
        SampleMessage("PB", "Parcel service", "Mon", "Your parcel is on its way"),
        SampleMessage("RF", "Rosalind Franklin", "Oct 30", "Photo 51"),
    )

/**
 * Shows the first [SHRUNK_ROWS] messages, then as many more as the [expansion] has grown by; the
 * rows all have one height.
 */
private fun Modifier.growingRows(expansion: PhoneExpansion): Modifier =
    clipToBounds().layout { measurable, constraints ->
        val rows = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
        val shrunk = rows.height * SHRUNK_ROWS / sampleMessages.size
        val height = shrunk + (expansion.reach * expansion.progress.value).roundToInt()
        layout(rows.width, min(height, rows.height)) { rows.place(0, 0) }
    }

private const val SHRUNK_ROWS = 3

/** How much of the window's height the grown phone reaches down to. */
private const val EXPANDED_SHARE = 0.65f

@Composable
private fun SketchMessage(
    message: SampleMessage,
    palette: OtusPalette,
    selected: Boolean,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (selected) palette.accentDark else palette.bgRegular,
                SmoothCornerShape(ThemeTokens.previewRowRadius),
            ).padding(ThemeTokens.previewRowPadding),
        horizontalArrangement = Arrangement.spacedBy(ThemeTokens.previewRowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(ThemeTokens.previewAvatar).background(palette.bgDark, CircleShape), Alignment.Center) {
            Text(message.initials, fontSize = ThemeTokens.previewDetailSize)
        }
        Column(Modifier.weight(1f)) {
            Row {
                val detail = if (selected) palette.text1 else palette.text2
                Text(message.from, Modifier.weight(1f), color = detail, fontSize = ThemeTokens.previewDetailSize)
                Text(message.time, color = detail, fontSize = ThemeTokens.previewDetailSize)
            }
            Text(
                message.subject,
                fontSize = ThemeTokens.previewSubjectSize,
                lineHeight = ThemeTokens.previewSubjectLineHeight,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The action bar: a group of three actions beside the accent New mail button. */
@Composable
private fun SketchBar(palette: OtusPalette) {
    Row(
        Modifier.fillMaxWidth().height(ThemeTokens.previewBarHeight),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
    ) {
        Row(
            Modifier
                .fillMaxHeight()
                .background(palette.bgDark, CircleShape)
                .strokeBorder(PanelTokens.borderWidth, palette.strokeBgDark, CircleShape)
                .padding(horizontal = ThemeTokens.previewBarPadding),
            horizontalArrangement = Arrangement.spacedBy(ThemeTokens.previewBarPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(Res.drawable.symbol_search, Res.drawable.symbol_filter_list, Res.drawable.symbol_mark_email_read)
                .forEach { Symbol(it, size = ThemeTokens.previewIcon) }
        }
        Row(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(palette.accentRegular, CircleShape)
                .padding(horizontal = ThemeTokens.previewBarPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "New mail",
                Modifier.weight(1f),
                fontSize = ThemeTokens.previewSubjectSize,
                fontWeight = FontWeight.SemiBold,
            )
            Symbol(Res.drawable.symbol_edit, size = ThemeTokens.previewIcon)
        }
    }
}
