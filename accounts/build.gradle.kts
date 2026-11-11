// JMAP account setup shared by the apps: verification, saved logins, and the setup page. Each app
// passes the JMAP service it needs.
plugins {
    id("otus.kmp-compose-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.jmap)
            api(projects.designsystem)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidx.compose.ui.test)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}

compose.resources { packageOfResClass = "com.otusfoss.core.accounts.resources" }
