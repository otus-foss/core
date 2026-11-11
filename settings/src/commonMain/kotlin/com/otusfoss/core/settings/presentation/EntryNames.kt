package com.otusfoss.core.settings.presentation

import kotlin.enums.enumEntries

/** The entry of [E] called [name], ignoring case, or null when no entry is. */
internal inline fun <reified E : Enum<E>> entryNamed(name: String): E? =
    enumEntries<E>().firstOrNull { it.name.equals(name, ignoreCase = true) }
