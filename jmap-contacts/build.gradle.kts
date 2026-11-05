// JMAP for Contacts (RFC 9610): address books and contact cards over the jmap client, with cards
// typed by jscontact. No UI and no app code, so Mail and Calendar can both use it. Published as
// com.otus:jmap-contacts.
plugins {
    id("otus.kmp-library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.jmap)
            api(projects.jscontact)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies { implementation(libs.robolectric) }
    }
}
