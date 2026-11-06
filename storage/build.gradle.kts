// Small values an app keeps on the device: key-value preferences, string maps replaced whole,
// one stored choice, and file-name-safe stored names. Android shared preferences and iOS user
// defaults implement them. No UI. Published as com.otus:storage.
plugins {
    id("otus.kmp-library")
}

kotlin {
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
