// Shared settings pages: the settings list, Appearance & behavior with the date-format choice, and
// the theme page.
plugins {
    id("otus.kmp-compose-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.designsystem)
            api(projects.dateformat)
            api(projects.storage)
            implementation(libs.compose.resources)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidx.compose.ui.test)
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.navigation.event.compose)
        }
    }
}

compose.resources { packageOfResClass = "com.otus.settings.resources" }
