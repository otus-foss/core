// Locale-aware date formatting and the user's date-format preference. No UI.
plugins {
    id("otus.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies { api(libs.kotlinx.datetime) }
        commonTest.dependencies { implementation(kotlin("test")) }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
