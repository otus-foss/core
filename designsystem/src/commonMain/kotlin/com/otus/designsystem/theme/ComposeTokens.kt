package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The fields of a message being written. No Figma frame covers compose yet, so these sizes are
 * app decisions that follow the settings rows' hairlines and the page's text inset.
 */
public object ComposeTokens {
    /** Space between a field's label and its value. */
    public val labelGap: Dp = 12.dp

    /** The `bgLight` line under each header field. */
    public val hairlineWidth: Dp = 1.dp
}
