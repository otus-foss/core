import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// JMAP for Contacts (RFC 9610): address books and contact cards over the jmap client, with cards
// typed by jscontact. No UI and no app code, so Mail and Calendar can both use it. Published as
// com.otus:jmap-contacts.
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
        namespace = "com.otus.jmap.contacts"
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
            api(projects.jscontact)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
