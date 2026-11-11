package com.otusfoss.core.designsystem.shared.controls.elements

import org.jetbrains.compose.resources.DrawableResource

/**
 * Something the user can do, as every surface shows it: a [label], an optional [symbol], whether
 * it is [enabled], and what [onClick] runs. Bars, menus, swipes, toasts, strips, banners, button
 * pairs, and sheets take it; a surface that needs more, such as a bar action's menu, wraps it.
 */
public class UiAction(
    public val label: String,
    public val symbol: DrawableResource? = null,
    public val enabled: Boolean = true,
    public val onClick: () -> Unit,
)
