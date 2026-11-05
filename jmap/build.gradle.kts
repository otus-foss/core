// Provider-independent JMAP client (RFC 8620) and login storage. No UI and no app code, so Mail and
// Calendar can both depend on it. Every public declaration is explicit. Published as com.otus:jmap.
plugins {
    id("otus.kmp-library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        // Real network for the opt-in server integration test.
        getByName("androidHostTest").dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.robolectric)
        }
    }
}
