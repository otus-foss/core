// Keeps JMAP push subscriptions (RFC 8620, section 7.2) pointed at each login's push endpoint.
// No UI and no platform push code; apps connect their push service. Published as com.otus:jmap-push.
plugins {
    id("otus.kmp-library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.jmap)
            api(projects.storage)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
