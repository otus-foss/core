// How avatars look and how the user edits their own: the design-system avatar of a resolved avatar
// or style, and the own-avatar editor. Apps hand it each account's addresses, so it needs no
// account code. Published as com.otus:avatar-ui.
plugins {
    id("otus.kmp-compose-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.avatar)
            api(projects.designsystem)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidx.compose.ui.test)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}
