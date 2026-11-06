package com.otus.designsystem.theme.typography

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * The text sizes by role. [panelTextStyle] sets [body]; components pick a role for other text
 * instead of a size of their own.
 */
public object TypeScale {
    /** A page title. */
    public val title: TextUnit = 20.sp
    public val titleLineHeight: TextUnit = 26.sp

    /** Body text, the size of [panelTextStyle]. */
    public val body: TextUnit = 16.sp
    public val bodyLineHeight: TextUnit = 20.sp

    /** A secondary line, or a count beside a label such as unread mail in a chip. */
    public val label: TextUnit = 14.sp

    /** A detail line under a title, such as a menu option's explanation or a date. */
    public val detail: TextUnit = 12.sp

    /** Text inside a badge, such as a "+N" count. */
    public val badge: TextUnit = 11.sp
}
