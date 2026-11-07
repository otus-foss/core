// The JSON conventions of RFC 8620 that JSContact (RFC 9553) reuses: UTCDate text and PatchObject
// paths. Only kotlinx.serialization, so jscontact stays free of networking.
plugins {
    id("otus.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies { api(libs.kotlinx.serialization.json) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
