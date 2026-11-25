// Small values an app keeps on the device: key-value preferences, string maps replaced whole, one
// stored choice, and file-name-safe stored names. Android shared preferences and iOS user defaults
// implement them. A text cipher with its key in the Android Keystore or the iOS Keychain seals what
// must not be readable from the device's files. No UI.
plugins {
    id("otus.kmp-library")
}

kotlin {
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
