package com.otusfoss.core.designsystem.shared.people.components

/**
 * A person named in a [SummaryCard][com.otusfoss.core.designsystem.shared.rows.components.SummaryCard] or
 * [PersonText], with the avatar shown before the [name].
 */
public class NamedAvatar(
    public val name: String,
    public val avatar: ChipAvatar,
)
