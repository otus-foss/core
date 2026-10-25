import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// JSContact cards (RFC 9553) as Kotlin types that keep unknown and vendor properties. Independent
// of JMAP, so any transport can carry them. No UI. Published as com.otus:jscontact.
plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    explicitApi()
    compilerOptions { allWarningsAsErrors.set(true) }
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.otus.jscontact"
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
        commonMain.dependencies { api(libs.kotlinx.serialization.json) }
        commonTest.dependencies { implementation(kotlin("test")) }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
