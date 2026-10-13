import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Shared settings pages: the settings list, Appearance & behavior with the date-format choice, and the theme page.
// Published as com.otus:settings.
plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    explicitApi()
    compilerOptions { allWarningsAsErrors.set(true) }
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.otus.settings"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        androidResources { enable = true }
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.designsystem)
            api(projects.dateformat)
            implementation(libs.compose.resources)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

compose.resources { packageOfResClass = "com.otus.settings.resources" }
