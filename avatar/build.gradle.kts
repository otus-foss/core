// Account-scoped avatar lookup: JMAP contact photos, optional domain icons, and bounded caches.
// No UI; apps decode images and supply public-suffix data. Published as com.otus:avatar.
plugins {
    id("otus.kmp-library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
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
