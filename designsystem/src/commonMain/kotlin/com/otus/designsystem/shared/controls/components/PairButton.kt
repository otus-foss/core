package com.otus.designsystem.shared.controls.components

/** One half of a [ButtonPair]: its [label], what a tap does, and whether it takes taps. */
public class PairButton(
    public val label: String,
    public val onClick: () -> Unit,
    public val enabled: Boolean = true,
)
