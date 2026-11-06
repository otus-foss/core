// Account-scoped avatar lookup: JMAP contact photos, optional domain icons, and bounded caches.
// No UI; apps decode images. Published as com.otus:avatar. Compose resources only package the
// bundled Public Suffix List, so the code reads it without importing any Compose API.
plugins {
    id("otus.kmp-compose-library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    compilerOptions { optIn.add("org.jetbrains.compose.resources.ExperimentalResourceApi") }
    sourceSets {
        commonMain.dependencies {
            api(projects.jmap)
            implementation(projects.jmapContacts)
            implementation(projects.storage)
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.serialization.json)
            // The Compose compiler, which the resources plugin needs, requires the runtime.
            implementation(libs.compose.runtime)
            implementation(libs.compose.resources)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}

compose.resources { packageOfResClass = "com.otus.avatar.resources" }
