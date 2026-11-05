import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * What every Otus multiplatform module shares: the iOS targets, the Android SDKs and JVM target, and
 * warnings as errors. Compose modules package Android resources; [hostTestResources] also gives them
 * to the host tests.
 */
internal fun Project.otusMultiplatform(
    namespace: String,
    androidResources: Boolean,
    hostTestResources: Boolean,
) {
    val catalog = libs
    extensions.configure<KotlinMultiplatformExtension> {
        compilerOptions { allWarningsAsErrors.set(true) }
        iosArm64()
        iosSimulatorArm64()
        extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
            this.namespace = namespace
            compileSdk = catalog.sdk("compileSdk")
            minSdk = catalog.sdk("minSdk")
            compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            androidResources { enable = androidResources }
            withHostTest { isIncludeAndroidResources = hostTestResources }
            lint { ignoreTestSources = true }
        }
    }
}
