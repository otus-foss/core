// JSContact cards (RFC 9553) as Kotlin types that keep unknown and vendor properties. Independent
// of JMAP, so any transport can carry them. No UI. Published as com.otus:jscontact.
plugins {
    id("otus.kmp-library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies { api(projects.jsonTypes) }
        commonTest.dependencies { implementation(kotlin("test")) }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
