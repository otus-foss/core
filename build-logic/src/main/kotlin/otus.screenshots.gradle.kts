// Screenshot tests compare against the PNGs in the screenshots folder beside the tests:
// src/test/screenshots in an app, src/androidHostTest/screenshots in a library. Pass
// -Proborazzi.record to write new ones instead; see Meta's docs/designsystem.md#screenshot-tests.
val testFolder = if (pluginManager.hasPlugin("com.android.application")) "test" else "androidHostTest"
val screenshots = layout.projectDirectory.dir("src/$testFolder/screenshots")
val recordScreenshots = providers.gradleProperty("roborazzi.record").isPresent

tasks.withType<Test>().configureEach {
    inputs
        .dir(screenshots)
        .withPropertyName("screenshots")
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .optional()
    systemProperty(if (recordScreenshots) "roborazzi.test.record" else "roborazzi.test.verify", "true")
    if (recordScreenshots) {
        outputs.upToDateWhen { false }
        outputs.doNotCacheIf("Recording writes baselines outside the test outputs") { true }
    }
}
