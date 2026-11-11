package com.otusfoss.core.designsystem.shared.onboarding.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * One thing a preset decides, as an [icon] in a circle over its [label] and the [value] the
 * preset gives it, such as "Icons" and "DuckDuckGo". One that is not [on] is dimmed and struck
 * through. Screen readers hear the label and the value.
 */
public class PresetMark(
    public val icon: DrawableResource,
    public val on: Boolean,
    public val label: String,
    public val value: String,
)

/**
 * What a [PresetCard] shows of a preset: its [title], the [marks] of what it decides, and a
 * [note] beside the check, such as "Edited" once its settings were changed.
 */
public class PresetChoice(
    public val title: String,
    public val marks: List<PresetMark>,
    public val note: String? = null,
)
