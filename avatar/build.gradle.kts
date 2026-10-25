import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Account-scoped avatar lookup: JMAP contact photos, optional domain icons, and bounded caches.
// No UI; apps decode images and supply public-suffix data. Published as com.otus:avatar.
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
        namespace = "com.otus.avatar"
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
        commonMain.dependencies {
            api(projects.jmap)
            implementation(projects.jmapContacts)
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
