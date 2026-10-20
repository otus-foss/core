package com.otus.designsystem.components

import androidx.compose.runtime.Composable

/** One row of a [DetailsTable]: a short [label], such as "From", and the [content] beside it. */
public class DetailLine(
    public val label: String,
    public val content: @Composable () -> Unit,
)
