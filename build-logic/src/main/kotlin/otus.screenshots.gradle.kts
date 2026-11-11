// Screenshot tests compare against the PNGs in the screenshots folder beside the tests:
// src/test/screenshots in an app, src/androidHostTest/screenshots in a library. Pass
// -Proborazzi.record to write new ones instead; see Core's docs/designsystem.md#screenshot-tests.
// Decided when tasks are configured, so the order of the plugins block does not matter.
val screenshots =
    provider {
        val testFolder = if (pluginManager.hasPlugin("com.android.application")) "test" else "androidHostTest"
        layout.projectDirectory.dir("src/$testFolder/screenshots")
    }
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
