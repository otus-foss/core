package com.otusfoss.core.designsystem.shared.dates.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.toSize
import com.otusfoss.core.designsystem.theme.tokens.selection.CalendarTokens
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.floor

/** Live week coordinates keep held range selection accurate as rows scroll and recycle. */
internal class CalendarDragTargets(
    val allowedDates: ClosedRange<LocalDate>,
) {
    var viewport: LayoutCoordinates? = null
    val rows = mutableMapOf<LocalDate, LayoutCoordinates>()

    fun dateAt(
        position: Offset,
        rtl: Boolean,
    ): LocalDate? {
        val container = viewport?.takeIf { it.isAttached } ?: return null
        return rows.entries.firstNotNullOfOrNull { (week, row) ->
            dateInRow(week, row, container, position, rtl)?.takeIf { it in allowedDates }
        }
    }
}

private fun dateInRow(
    week: LocalDate,
    row: LayoutCoordinates,
    container: LayoutCoordinates,
    position: Offset,
    rtl: Boolean,
): LocalDate? {
    val local = row.takeIf { it.isAttached }?.localPositionOf(container, position)
    val size = row.size
    if (local == null || !Rect(Offset.Zero, size.toSize()).contains(local)) return null
    val column = floor(local.x / size.width * CalendarTokens.DAYS_PER_WEEK).toInt()
    val logicalColumn = if (rtl) CalendarTokens.DAYS_PER_WEEK - 1 - column else column
    return week.plus(logicalColumn, DateTimeUnit.DAY)
}
