import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Locale-aware date formatting and the user's date-format preference. No UI. Published as
// com.otus:dateformat.
plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    explicitApi()
    compilerOptions { allWarningsAsErrors.set(true) }
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.otus.dateformat"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        withHostTest {}
        lint { ignoreTestSources = true }
    }
    sourceSets {
        commonMain.dependencies { api(libs.kotlinx.datetime) }
        commonTest.dependencies { implementation(kotlin("test")) }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
