package com.otus.settings.data

import com.otus.settings.presentation.AnimationPreferenceStore
import platform.Foundation.NSUserDefaults

/** Keeps the animation speed in the app's user defaults. */
public class IosAnimationPreferenceStore(
    private val preferences: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : AnimationPreferenceStore {
    override fun read(): String? = preferences.stringForKey(SPEED)

    override fun write(value: String) {
        preferences.setObject(value, forKey = SPEED)
    }

    private companion object {
        const val SPEED = "otus.animation_speed"
    }
}
