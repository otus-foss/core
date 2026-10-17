package com.otus.settings.domain

/** Keeps the chosen animation speed's name between launches, such as in platform preferences. */
public interface AnimationPreferenceStore {
    public fun read(): String?

    public fun write(value: String)
}
