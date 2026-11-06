package com.otus.designsystem.shared.selection.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * Something an [OrderedChoiceSheet] offers, saved as [key]. Chosen, it reads as its full [label];
 * offered, it is listed under [group] as [name], [depth] levels in, such as a folder under its parent.
 */
public data class OrderedChoice<out T>(
    val value: T,
    val key: String,
    val label: String,
    val icon: DrawableResource,
    val group: String,
    val name: String = label,
    val depth: Int = 0,
)
