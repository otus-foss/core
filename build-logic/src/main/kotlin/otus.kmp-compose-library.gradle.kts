// A Core library module with Compose UI or Compose resources; see OtusLibrary.kt.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

otusLibrary(compose = true)
