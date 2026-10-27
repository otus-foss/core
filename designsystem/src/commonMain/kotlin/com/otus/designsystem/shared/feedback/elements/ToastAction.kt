package com.otus.designsystem.shared.feedback.elements

/** The button a toast can offer, such as Undo. A toast with an action always runs a timer. */
public class ToastAction(
    public val label: String,
    public val onAction: () -> Unit,
)
