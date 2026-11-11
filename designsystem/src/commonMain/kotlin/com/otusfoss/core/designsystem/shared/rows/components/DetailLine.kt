package com.otusfoss.core.designsystem.shared.rows.components

import androidx.compose.runtime.Composable

/** One row of [DetailRows]: a short [label], such as "From", and the [content] beside it. */
public class DetailLine(
    public val label: String,
    public val content: @Composable () -> Unit,
)
