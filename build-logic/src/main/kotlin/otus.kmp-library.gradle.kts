// A Meta library module without UI; see OtusLibrary.kt.
plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

otusLibrary(compose = false)
