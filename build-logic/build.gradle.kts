// Convention plugins for Meta, Mail, and Calendar; see docs/quality.md#build-logic.
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.compilerGradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.kover.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)
}
