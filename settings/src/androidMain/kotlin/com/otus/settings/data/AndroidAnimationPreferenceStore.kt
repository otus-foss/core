package com.otus.settings.data

import android.content.Context
import com.otus.settings.domain.AnimationPreferenceStore

/** Keeps the animation speed in the app's private shared preferences. */
public class AndroidAnimationPreferenceStore(
    context: Context,
) : AnimationPreferenceStore {
    private val preferences = context.applicationContext.getSharedPreferences("otus_animation", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(SPEED, null)

    override fun write(value: String) {
        preferences.edit().putString(SPEED, value).apply()
    }

    private companion object {
        const val SPEED = "speed"
    }
}
