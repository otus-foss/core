package com.otus.settings.presentation

import com.otus.settings.domain.AnimationPreferenceStore

/** One stored value in memory, as each settings state's preference store keeps it. */
internal class MemoryPreferenceStore :
    AnimationPreferenceStore,
    AppIconPreferenceStore,
    ThemePreferenceStore {
    private var value: String? = null

    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }
}
