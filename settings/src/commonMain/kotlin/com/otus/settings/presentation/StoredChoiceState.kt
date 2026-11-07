package com.otus.settings.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.storage.StoredChoice

/**
 * One choice of a settings page, such as the theme, kept in [store] between launches. [decode]
 * reads the saved text and gives null for text it does not know, which reads as [default].
 */
public open class StoredChoiceState<T>(
    private val store: StoredChoice,
    default: T,
    decode: (String) -> T?,
    private val encode: (T) -> String,
) {
    public var value: T by mutableStateOf(store.read()?.let(decode) ?: default)
        private set

    /** Saves [value] first, so a choice the store refuses never shows as made. */
    public fun select(value: T) {
        store.write(encode(value))
        this.value = value
    }
}
